package in.doritech.cms.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Category;
import in.doritech.cms.entity.Employee;
import in.doritech.cms.entity.MediaFile;
import in.doritech.cms.entity.News;
import in.doritech.cms.repository.CategoryRepository;
import in.doritech.cms.repository.EmployeeRepository;
import in.doritech.cms.repository.MediaFileRepository;
import in.doritech.cms.repository.NewsRepository;
import in.doritech.cms.request.NewsRequest;
import in.doritech.cms.response.CategoryResponse;
import in.doritech.cms.response.MediaFileResponse;
import in.doritech.cms.response.NewsResponse;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.security.CurrentUserService;
import in.doritech.cms.service.NewsService;
import in.doritech.cms.util.ApiResponse;

@Service
@Transactional
public class NewsServiceImpl implements NewsService {

	private static final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

	private final NewsRepository newsRepository;
	private final CategoryRepository categoryRepository;
	private final MediaFileRepository mediaFileRepository;
	private final EmployeeRepository employeeRepository;

	public NewsServiceImpl(NewsRepository newsRepository, CategoryRepository categoryRepository,
			MediaFileRepository mediaFileRepository, EmployeeRepository employeeRepository) {

		this.newsRepository = newsRepository;
		this.categoryRepository = categoryRepository;
		this.mediaFileRepository = mediaFileRepository;
		this.employeeRepository = employeeRepository;
	}

	@Override
	public ApiResponse createNews(NewsRequest request) {
		try {
			validateRequest(request);

			Long currentUserId = CurrentUserService.getCurrentUserId();

			if (currentUserId == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
			}

			// Obtain a lazy reference instead of loading the full employee.
			Employee author = employeeRepository.getReferenceById(currentUserId);

			News news = new News();
			news.setTitle(request.getTitle().trim());
			news.setSlug(request.getSlug().trim());
			news.setSummary(request.getSummary());
			news.setContent(request.getContent());
			news.setStatus("DRAFT");
			news.setAuthor(author);
			news.setCategories(resolveCategories(request.getCategoryIds()));
			news.setMediaFiles(resolveMediaFiles(request.getMediaIds()));

			News saved = newsRepository.save(news);

			return new ApiResponse("News created successfully", convertToResponse(saved), HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"News slug or associated data conflicts with existing records");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create news");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getNewsById(Long id) {
		try {
			News news = findNewsById(id);

			return new ApiResponse("News fetched successfully", convertToResponse(news), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch news");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllNews() {
		try {
			List<NewsResponse> responses = newsRepository.findAll().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			return new ApiResponse("News fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch news");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllNewsWithPagination(int page, int size) {
		System.out.println("Entered!");
		try {
			// Validate pagination values
			if (page < 0 || size <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Page must be 0 or greater and size must be greater than 0");
			}

			Pageable pageable = PageRequest.of(page, size);

			Page<News> newsPage = newsRepository.findAll(pageable);
			System.out.println("News details is" + newsPage.toString());

			List<NewsResponse> responses = newsPage.getContent().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			PageResponse<NewsResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(newsPage.getNumber());
			pageResponse.setPageSize(newsPage.getSize());
			pageResponse.setTotalElements(newsPage.getTotalElements());
			pageResponse.setTotalPages(newsPage.getTotalPages());
			pageResponse.setLastPage(newsPage.isLast());

			return new ApiResponse("News fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch news with pagination", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch news", e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getNewsByStatus(String status) {
		try {
			if (status == null || status.trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
			}

			String normalizedStatus = status.trim().toUpperCase();

			if (!Set.of("DRAFT", "PUBLISHED").contains(normalizedStatus)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Invalid status. Allowed values: DRAFT, PUBLISHED");
			}

			List<NewsResponse> responses = newsRepository.findByStatus(normalizedStatus).stream()
					.map(this::convertToResponse).collect(Collectors.toList());

			return new ApiResponse("News fetched by status successfully", responses, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch news by status");
		}
	}

	@Override
	public ApiResponse updateNews(Long id, NewsRequest request) {
		try {
			validateRequest(request);

			News news = findNewsById(id);

			news.setTitle(request.getTitle().trim());
			news.setSlug(request.getSlug().trim());
			news.setSummary(request.getSummary());
			news.setContent(request.getContent());

			news.setCategories(resolveCategories(request.getCategoryIds()));
			news.setMediaFiles(resolveMediaFiles(request.getMediaIds()));

			News updated = newsRepository.save(news);

			return new ApiResponse("News updated successfully", convertToResponse(updated), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"News slug or associated data conflicts with existing records");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update news");
		}
	}

	@Override
	public ApiResponse deleteNews(Long id) {
		try {
			News news = findNewsById(id);

			newsRepository.delete(news);

			return new ApiResponse("News deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"News is referenced by other records and cannot be deleted");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete news");
		}
	}

	@Override
	public ApiResponse publishNews(Long id) {
		try {
			News news = findNewsById(id);

			news.setStatus("PUBLISHED");

			if (news.getPublishedAt() == null) {
				news.setPublishedAt(LocalDateTime.now());
			}

			News published = newsRepository.save(news);

			return new ApiResponse("News published successfully", convertToResponse(published), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Unable to publish news because of a database constraint");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to publish news");
		}
	}

	private News findNewsById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "News ID is required");
		}

		return newsRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found: " + id));
	}

	private void validateRequest(NewsRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}

		if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "News title is required");
		}

		if (request.getTitle().trim().length() > 255) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "News title must not exceed 255 characters");
		}

		if (request.getSlug() == null || request.getSlug().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "News slug is required");
		}

		if (request.getSummary() != null && request.getSummary().length() > 1000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "News summary must not exceed 1000 characters");
		}

		if (request.getContent() == null || request.getContent().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "News content is required");
		}
	}

	private Set<Category> resolveCategories(Set<Long> categoryIds) {
		Set<Category> categories = new HashSet<>();

		if (categoryIds == null || categoryIds.isEmpty()) {
			return categories;
		}

		for (Long categoryId : categoryIds) {
			if (categoryId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID cannot be null");
			}

			Category category = categoryRepository.findById(categoryId).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + categoryId));

			categories.add(category);
		}

		return categories;
	}

	private Set<MediaFile> resolveMediaFiles(Set<Long> mediaIds) {
		Set<MediaFile> mediaFiles = new HashSet<>();

		if (mediaIds == null || mediaIds.isEmpty()) {
			return mediaFiles;
		}

		for (Long mediaId : mediaIds) {
			if (mediaId == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media ID cannot be null");
			}

			MediaFile mediaFile = mediaFileRepository.findById(mediaId).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media file not found: " + mediaId));

			mediaFiles.add(mediaFile);
		}

		return mediaFiles;
	}

	private NewsResponse convertToResponse(News news) {
		NewsResponse response = new NewsResponse();

		response.setNewsId(news.getNewsId());
		response.setTitle(news.getTitle());
		response.setSlug(news.getSlug());
		response.setSummary(news.getSummary());
		response.setContent(news.getContent());
		response.setStatus(news.getStatus());

		// Map author details without exposing employee credentials.
		if (news.getAuthor() != null) {
			response.setAuthorId(news.getAuthor().getEmployeeId());
			response.setAuthorName(news.getAuthor().getEmployeeName());
		}

		// Map categories.
		Set<CategoryResponse> categoryResponses = new HashSet<>();

		if (news.getCategories() != null) {
			for (Category category : news.getCategories()) {
				CategoryResponse categoryResponse = new CategoryResponse();

				categoryResponse.setCategoryId(category.getCategoryId());
				categoryResponse.setCategoryName(category.getCategoryName());
				categoryResponse.setSlug(category.getSlug());
				categoryResponse.setDescription(category.getDescription());

				categoryResponses.add(categoryResponse);
			}
		}

		response.setCategories(categoryResponses);

		// Map media files.
		Set<MediaFileResponse> mediaResponses = new HashSet<>();

		if (news.getMediaFiles() != null) {
			for (MediaFile mediaFile : news.getMediaFiles()) {
				MediaFileResponse mediaResponse = new MediaFileResponse();

				mediaResponse.setMediaId(mediaFile.getMediaId());
				mediaResponse.setOriginalFilename(mediaFile.getOriginalFilename());
				mediaResponse.setStoredFilename(mediaFile.getStoredFilename());
				mediaResponse.setContentType(mediaFile.getContentType());
				mediaResponse.setFileSize(mediaFile.getFileSize());

				if (mediaFile.getUploadedBy() != null) {
					mediaResponse.setUploadedById(mediaFile.getUploadedBy().getEmployeeId());
					mediaResponse.setUploadedByName(mediaFile.getUploadedBy().getEmployeeName());
				}

				mediaResponse.setUploadedAt(mediaFile.getUploadedAt());
				mediaResponse.setImageUrl(null);

				mediaResponses.add(mediaResponse);
			}
		}

		response.setMediaFiles(mediaResponses);

		response.setPublishedAt(news.getPublishedAt());
		response.setCreatedAt(news.getCreatedAt());
		response.setUpdatedAt(news.getUpdatedAt());

		return response;
	}

	@Override
	@Transactional
	public ApiResponse createNewsWithImages(NewsRequest request, List<MultipartFile> images) {

		try {

			validateRequest(request);

			Long currentUserId = CurrentUserService.getCurrentUserId();

			if (currentUserId == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
			}

			Employee author = employeeRepository.getReferenceById(currentUserId);

			News news = new News();

			news.setTitle(request.getTitle().trim());
			news.setSlug(request.getSlug().trim());
			news.setSummary(request.getSummary());
			news.setContent(request.getContent());
			news.setStatus("DRAFT");
			news.setAuthor(author);

			news.setCategories(resolveCategories(request.getCategoryIds()));

			Set<MediaFile> mediaFiles = new HashSet<>();

			// Save multiple images
			if (images != null && !images.isEmpty()) {

				for (MultipartFile image : images) {

					MediaFile savedMedia = saveMediaFile(image, author);

					mediaFiles.add(savedMedia);
				}
			}

			// Associate saved images with News
			news.setMediaFiles(mediaFiles);

			// Save News entity
			News savedNews = newsRepository.save(news);

			// Convert entity into response DTO
			NewsResponse newsResponse = convertToResponse(savedNews);

			return new ApiResponse("News created successfully", newsResponse, HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {

			throw e;

		} catch (DataIntegrityViolationException e) {

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"News slug or associated data conflicts with existing records");

		} catch (Exception e) {

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create news");
		}
	}
	
	@Override
	@Transactional
	public ApiResponse updateNewsWithImages(
	        Long id, NewsRequest request, List<MultipartFile> images) {

	    try {

	        validateRequest(request);

	        // Find existing news
	        News news = newsRepository.findById(id)
	                .orElseThrow(() -> new ResponseStatusException(
	                        HttpStatus.NOT_FOUND,
	                        "News not found with id: " + id
	                ));

	        String slug = request.getSlug().trim();

	        // Check duplicate slug
	        if (newsRepository.existsBySlug(slug)
	                && !slug.equals(news.getSlug())) {

	            throw new ResponseStatusException(
	                    HttpStatus.CONFLICT,
	                    "News slug already exists: " + slug
	            );
	        }

	        // Update news fields
	        news.setTitle(request.getTitle().trim());
	        news.setSlug(slug);
	        news.setSummary(request.getSummary());
	        news.setContent(request.getContent());

	        // Update categories
	        news.setCategories(resolveCategories(request.getCategoryIds()));

	        // Add new images if provided
	        if (images != null && !images.isEmpty()) {

	            Long currentUserId = CurrentUserService.getCurrentUserId();

	            if (currentUserId == null) {
	                throw new ResponseStatusException(
	                        HttpStatus.UNAUTHORIZED,
	                        "Authenticated user not found"
	                );
	            }

	            Employee employee =
	                    employeeRepository.getReferenceById(currentUserId);

	            Set<MediaFile> mediaFiles = news.getMediaFiles();

	            if (mediaFiles == null) {
	                mediaFiles = new HashSet<>();
	            }

	            for (MultipartFile image : images) {

	                MediaFile savedMedia = saveMediaFile(image, employee);

	                mediaFiles.add(savedMedia);
	            }

	            news.setMediaFiles(mediaFiles);
	        }

	        // Save updated news
	        News updatedNews = newsRepository.save(news);

	        // Convert entity to response
	        NewsResponse newsResponse = convertToResponse(updatedNews);

	        return new ApiResponse(
	                "News updated successfully",
	                newsResponse,
	                HttpStatus.OK.value()
	        );

	    } catch (ResponseStatusException e) {

	        throw e;

	    } catch (DataIntegrityViolationException e) {

	        throw new ResponseStatusException(
	                HttpStatus.CONFLICT,
	                "News slug or associated data conflicts with existing records"
	        );

	    } catch (Exception e) {

	        log.error("Failed to update news with id: " + id, e);

	        throw new ResponseStatusException(
	                HttpStatus.INTERNAL_SERVER_ERROR,
	                "Failed to update news"
	        );
	    }
	}

	private MediaFile saveMediaFile(MultipartFile file, Employee employee) {

		try {

			if (file == null || file.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
			}

			// Validate file type
			String contentType = file.getContentType();

			if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {

				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
			}

			// Get original filename
			String originalFilename = file.getOriginalFilename();

			if (originalFilename == null || originalFilename.trim().isEmpty()) {

				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image filename is required");
			}

			// Generate unique filename
			String storedFilename = UUID.randomUUID().toString();

			int dotIndex = originalFilename.lastIndexOf('.');

			if (dotIndex >= 0) {
				storedFilename += originalFilename.substring(dotIndex);
			}

			// Create upload directory
			Path uploadDirectory = Paths.get("uploads", "news");

			Files.createDirectories(uploadDirectory);

			// Save image on disk
			Path destination = uploadDirectory.resolve(storedFilename).normalize();

			if (!destination.startsWith(uploadDirectory.toAbsolutePath().normalize()) && destination.isAbsolute()) {

				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path");
			}

			Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

			// Create MediaFile entity
			MediaFile mediaFile = new MediaFile();

			mediaFile.setOriginalFilename(originalFilename);
			mediaFile.setStoredFilename(storedFilename);
			mediaFile.setFilePath(destination.toString());
			mediaFile.setContentType(contentType);
			mediaFile.setFileSize(file.getSize());
			mediaFile.setUploadedAt(LocalDateTime.now());
			mediaFile.setUploadedBy(employee);

			// Save MediaFile and generate mediaId
			return mediaFileRepository.save(mediaFile);

		} catch (IOException e) {

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
		}
	}
}