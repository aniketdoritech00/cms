
package in.doritech.cms.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.MediaFile;
import in.doritech.cms.repository.BlogRepository;
import in.doritech.cms.repository.EmployeeRepository;
import in.doritech.cms.repository.MediaFileRepository;
import in.doritech.cms.repository.NewsRepository;
import in.doritech.cms.response.MediaFileResponse;
import in.doritech.cms.service.MediaFileService;
import in.doritech.cms.util.ApiResponse;

@Service
public class MediaFileServiceImpl implements MediaFileService {

	private final MediaFileRepository mediaFileRepository;

	private final BlogRepository blogRepository;

	private final NewsRepository newsRepository;

	private final EmployeeRepository employeeRepository;

	@Value("${file.upload.image-path}")
	private String uploadDirectory;

	public MediaFileServiceImpl(MediaFileRepository mediaFileRepository, BlogRepository blogRepository,
			NewsRepository newsRepository, EmployeeRepository employeeRepository) {
		this.mediaFileRepository = mediaFileRepository;
		this.blogRepository = blogRepository;
		this.newsRepository = newsRepository;
		this.employeeRepository = employeeRepository;
	}

	@Override
	@Transactional
	public ApiResponse uploadFile(MultipartFile file) {

		Path destination = null;

		try {
			if (file == null || file.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a file");
			}

			String originalFileName = file.getOriginalFilename();

			if (originalFileName == null || originalFileName.isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File name is required");
			}

			String storedFileName = UUID.randomUUID() + "_" + Paths.get(originalFileName).getFileName();

			Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();

			Files.createDirectories(uploadPath);

			destination = uploadPath.resolve(storedFileName).normalize();

			if (!destination.startsWith(uploadPath)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
			}

			file.transferTo(destination);

			MediaFile mediaFile = new MediaFile();

			mediaFile.setOriginalFilename(originalFileName);
			mediaFile.setStoredFilename(storedFileName);
			mediaFile.setContentType(file.getContentType());
			mediaFile.setFileSize(file.getSize());
			mediaFile.setFilePath(destination.toString());

			MediaFile saved = mediaFileRepository.save(mediaFile);

			return new ApiResponse("File uploaded successfully", convertToResponse(saved), HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			deleteFileQuietly(destination);
			throw e;

		} catch (IOException e) {
			deleteFileQuietly(destination);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file");

		} catch (Exception e) {
			deleteFileQuietly(destination);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file metadata");
		}
	}

	@Override
	public ApiResponse getMediaFileById(Long id) {

		try {
			MediaFile mediaFile = mediaFileRepository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media file not found: " + id));

			return new ApiResponse("Media file fetched successfully", convertToResponse(mediaFile),
					HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch media file");
		}
	}

	@Override
	public ApiResponse getAllMediaFiles() {

		try {
			List<MediaFileResponse> responses = new ArrayList<>();

			for (MediaFile mediaFile : mediaFileRepository.findAll()) {
				responses.add(convertToResponse(mediaFile));
			}

			return new ApiResponse("Media files fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch media files");
		}
	}

	@Override
	@Transactional
	public ApiResponse deleteMediaFile(Long id) {

		try {
			// 1. Find media file
			MediaFile mediaFile = mediaFileRepository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media file not found: " + id));

			// 2. Check employee mapping
			boolean mappedToEmployee = employeeRepository.existsByProfilePicture_MediaId(id);

			// 3. Check Blog mapping
			boolean mappedToBlog = blogRepository.existsByMediaFiles_MediaId(id);

			// 4. Check News mapping
			boolean mappedToNews = newsRepository.existsByMediaFiles_MediaId(id);

			// 5. Prevent deletion if mapped anywhere
			if (mappedToEmployee && mappedToBlog && mappedToNews) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to Employee, Blog and News records.");

			} else if (mappedToEmployee && mappedToBlog) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to Employee and Blog records.");

			} else if (mappedToEmployee && mappedToNews) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to Employee and News records.");

			} else if (mappedToBlog && mappedToNews) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to Blog and News records.");

			} else if (mappedToEmployee) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to an Employee profile picture. Please remove or replace the employee profile picture first.");

			} else if (mappedToBlog) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to a Blog record.");

			} else if (mappedToNews) {

				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"Cannot delete media file. It is mapped to a News record.");
			}

			Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();

			Path storedPath = Paths.get(mediaFile.getFilePath());

			Path path = storedPath.isAbsolute() ? storedPath.toAbsolutePath().normalize()
					: uploadPath.resolve(storedPath).normalize();

			if (!path.startsWith(uploadPath) || path.equals(uploadPath)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stored file path");
			}

			Files.deleteIfExists(path);

			mediaFileRepository.delete(mediaFile);
			mediaFileRepository.flush();

			return new ApiResponse("Media file deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Cannot delete media file because it is still referenced by another record.", e);

		} catch (IOException e) {

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete physical file", e);

		} catch (Exception e) {

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete media file", e);
		}
	}

	@Override
	public ResponseEntity<byte[]> getImageById(Long id) {

		// Find media record by ID
		MediaFile mediaFile = mediaFileRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found with ID: " + id));

		// Get saved file path from database
		String filePath = mediaFile.getFilePath();

		if (filePath == null || filePath.isBlank()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image file path not found");
		}

		Path path = Paths.get(filePath).toAbsolutePath().normalize();

		// Check whether file exists
		if (!Files.exists(path) || !Files.isRegularFile(path)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image file does not exist on disk");
		}

		try {
			// Read image as byte array
			byte[] imageBytes = Files.readAllBytes(path);

			// Get image content type
			MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

			String contentType = mediaFile.getContentType();

			if (contentType != null && !contentType.isBlank()) {
				try {
					mediaType = MediaType.parseMediaType(contentType);
				} catch (IllegalArgumentException e) {
					mediaType = MediaType.APPLICATION_OCTET_STREAM;
				}
			} else {
				String detectedType = Files.probeContentType(path);

				if (detectedType != null) {
					try {
						mediaType = MediaType.parseMediaType(detectedType);
					} catch (IllegalArgumentException e) {
						mediaType = MediaType.APPLICATION_OCTET_STREAM;
					}
				}
			}

			// Return image bytes
			return ResponseEntity.ok().contentType(mediaType)
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
					.body(imageBytes);

		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read image file");
		}
	}

	private MediaFileResponse convertToResponse(MediaFile mediaFile) {

		MediaFileResponse response = new MediaFileResponse();

		response.setMediaId(mediaFile.getMediaId());
		response.setOriginalFilename(mediaFile.getOriginalFilename());
		response.setStoredFilename(mediaFile.getStoredFilename());
		response.setContentType(mediaFile.getContentType());
		response.setFileSize(mediaFile.getFileSize());

		response.setImageUrl(null);

		if (mediaFile.getUploadedBy() != null) {
			response.setUploadedById(mediaFile.getUploadedBy().getEmployeeId());

			response.setUploadedByName(mediaFile.getUploadedBy().getEmployeeName());
		}

		response.setUploadedAt(mediaFile.getUploadedAt());

		return response;
	}

	private void deleteFileQuietly(Path path) {

		if (path != null) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException ignored) {
				// Keep the exceptionoriginal
			}
		}
	}
}
