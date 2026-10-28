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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Blog;
import in.doritech.cms.entity.Category;
import in.doritech.cms.entity.Employee;
import in.doritech.cms.entity.MediaFile;
import in.doritech.cms.repository.BlogRepository;
import in.doritech.cms.repository.CategoryRepository;
import in.doritech.cms.repository.EmployeeRepository;
import in.doritech.cms.repository.MediaFileRepository;
import in.doritech.cms.request.BlogRequest;
import in.doritech.cms.response.BlogResponse;
import in.doritech.cms.response.CategoryResponse;
import in.doritech.cms.response.MediaFileResponse;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.security.CurrentUserService;
import in.doritech.cms.service.BlogService;
import in.doritech.cms.util.ApiResponse;

@Service
@Transactional
public class BlogServiceImpl implements BlogService {

	private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

	private final BlogRepository blogRepository;
	private final CategoryRepository categoryRepository;
	private final MediaFileRepository mediaFileRepository;
	private final EmployeeRepository employeeRepository;

	public BlogServiceImpl(BlogRepository blogRepository, CategoryRepository categoryRepository,
			MediaFileRepository mediaFileRepository, EmployeeRepository employeeRepository) {

		this.blogRepository = blogRepository;
		this.categoryRepository = categoryRepository;
		this.mediaFileRepository = mediaFileRepository;
		this.employeeRepository = employeeRepository;
	}

	@Override
	public ApiResponse createBlog(BlogRequest request) {
		try {
			validateRequest(request);

			Long currentUserId = CurrentUserService.getCurrentUserId();

			if (currentUserId == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
			}

			// Obtain an employee reference without immediately loading
			// all employee details.
			Employee author = employeeRepository.getReferenceById(currentUserId);

			Blog blog = new Blog();

			blog.setTitle(request.getTitle().trim());
//			blog.setSlug(request.getSlug().trim());
			blog.setSummary(request.getSummary());
			blog.setContent(request.getContent());
			blog.setStatus("DRAFT");
			blog.setAuthor(author);

			blog.setCategories(resolveCategories(request.getCategoryIds()));

			blog.setMediaFiles(resolveMediaFiles(request.getMediaIds()));

			Blog saved = blogRepository.save(blog);

			return new ApiResponse("Blog created successfully", convertToResponse(saved), HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			log.error("Database constraint violation while creating blog", e);

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Blog slug or associated data conflicts with existing records");
		} catch (Exception e) {
			log.error("Failed to create blog", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create blog");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getBlogById(Long id) {
		try {
			Blog blog = findBlogById(id);

			if (blog == null) {
				return new ApiResponse("Blog not found!", null, HttpStatus.NOT_FOUND.value());
			}

			return new ApiResponse("Blog fetched successfully", convertToResponse(blog), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch blog with ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch blog");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllBlogs() {
		try {
			List<BlogResponse> responses = blogRepository.findAll().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			return new ApiResponse("Blogs fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			log.error("Failed to fetch blogs", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch blogs");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getBlogsByStatus(String status) {
		try {
			if (status == null || status.trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
			}

			String normalizedStatus = status.trim().toUpperCase();

			if (!Set.of("DRAFT", "PUBLISHED").contains(normalizedStatus)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Invalid status. Allowed values: DRAFT, PUBLISHED");
			}

			List<BlogResponse> responses = blogRepository.findByStatus(normalizedStatus).stream()
					.map(this::convertToResponse).collect(Collectors.toList());

			return new ApiResponse("Blogs fetched by status successfully", responses, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch blogs by status: {}", status, e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch blogs by status");
		}
	}

	@Override
	public ApiResponse updateBlog(Long id, BlogRequest request) {
		try {
			validateRequest(request);

			Blog blog = findBlogById(id);

			blog.setTitle(request.getTitle().trim());
//			blog.setSlug(request.getSlug().trim());
			blog.setSummary(request.getSummary());
			blog.setContent(request.getContent());

			blog.setCategories(resolveCategories(request.getCategoryIds()));

			blog.setMediaFiles(resolveMediaFiles(request.getMediaIds()));

			Blog updated = blogRepository.save(blog);

			return new ApiResponse("Blog updated successfully", convertToResponse(updated), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			log.error("Database constraint violation updating blog ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Blog slug or associated data conflicts with existing records");
		} catch (Exception e) {
			log.error("Failed to update blog ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update blog");
		}
	}

	@Override
	public ApiResponse deleteBlog(Long id) {
		try {
			Blog blog = findBlogById(id);

			blogRepository.delete(blog);

			return new ApiResponse("Blog deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			log.error("Database constraint violation deleting blog ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.CONFLICT, "Blog is referenced by other records");
		} catch (Exception e) {
			log.error("Failed to delete blog ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete blog");
		}
	}

	private Blog findBlogById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog ID is required");
		}

		return blogRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found: " + id));
	}

	private void validateRequest(BlogRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}

		if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog title is required");
		}

		if (request.getTitle().trim().length() > 255) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog title must not exceed 255 characters");
		}

//		if (request.getSlug() == null || request.getSlug().trim().isEmpty()) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog slug is required");
//		}

		if (request.getSummary() != null && request.getSummary().length() > 1000) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog summary must not exceed 1000 characters");
		}

		if (request.getContent() == null || request.getContent().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Blog content is required");
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

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllBlogsWithPagination(int page, int size) {
		try {
			if (page < 0 || size <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Page must be 0 or greater and size must be greater than 0");
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

			Page<Blog> blogPage = blogRepository.findAll(pageable);

			List<BlogResponse> responses = blogPage.getContent().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			PageResponse<BlogResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(blogPage.getNumber());
			pageResponse.setPageSize(blogPage.getSize());
			pageResponse.setTotalElements(blogPage.getTotalElements());
			pageResponse.setTotalPages(blogPage.getTotalPages());
			pageResponse.setLastPage(blogPage.isLast());

			return new ApiResponse("Blogs fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch blogs with pagination", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch blogs");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllBlogsWithPaginationWithStatus(int page, int size) {
		try {
			if (page < 0 || size <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Page must be 0 or greater and size must be greater than 0");
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

			Page<Blog> blogPage = blogRepository.findByStatus("PUBLISHED", pageable);

			List<BlogResponse> responses = blogPage.getContent().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			PageResponse<BlogResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(blogPage.getNumber());
			pageResponse.setPageSize(blogPage.getSize());
			pageResponse.setTotalElements(blogPage.getTotalElements());
			pageResponse.setTotalPages(blogPage.getTotalPages());
			pageResponse.setLastPage(blogPage.isLast());

			return new ApiResponse("Blogs fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch blogs with pagination", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch blogs");
		}
	}

	@Override
	@Transactional
	public ApiResponse createBlogWithImages(BlogRequest request, List<MultipartFile> images) {

		try {
			validateRequest(request);

//			String slug = request.getSlug().trim();

//			// Check for duplicate slug before uploading images
//			if (blogRepository.existsBySlug(slug)) {
//				throw new ResponseStatusException(HttpStatus.CONFLICT,
//						"A blog with slug '" + slug + "' already exists");
//			}

			Long currentUserId = CurrentUserService.getCurrentUserId();

			if (currentUserId == null) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
			}

			Employee author = employeeRepository.getReferenceById(currentUserId);

			Blog blog = new Blog();
			blog.setTitle(request.getTitle().trim());
//			blog.setSlug(slug);
			blog.setSummary(request.getSummary());
			blog.setContent(request.getContent());
			blog.setStatus("DRAFT");
			blog.setAuthor(author);
			blog.setCategories(resolveCategories(request.getCategoryIds()));

			Set<MediaFile> mediaFiles = new HashSet<>();

			if (images != null && !images.isEmpty()) {
				for (MultipartFile image : images) {
					MediaFile savedMedia = saveBlogMediaFile(image, author);
					mediaFiles.add(savedMedia);
				}
			}

			blog.setMediaFiles(mediaFiles);

			Blog savedBlog = blogRepository.save(blog);

			BlogResponse blogResponse = convertToResponse(savedBlog);

			return new ApiResponse("Blog created successfully", blogResponse, HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			log.error("Database constraint violation while creating blog", e);

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"The blog could not be created because a unique or related database constraint was violated");

		} catch (Exception e) {
			log.error("Failed to create blog", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create blog");
		}
	}

	@Override
	@Transactional
	public ApiResponse updateBlogWithImages(Long id, BlogRequest request, List<MultipartFile> images) {

		try {
			// Validate request
			validateRequest(request);

			// Find existing blog
			Blog blog = blogRepository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found with id: " + id));

			// Get updated slug
//			String slug = request.getSlug().trim();

			// Check duplicate slug
//			if (blogRepository.existsBySlug(slug) && !slug.equals(blog.getSlug())) {
//
//				throw new ResponseStatusException(HttpStatus.CONFLICT,
//						"A blog with slug '" + slug + "' already exists");
//			}

			// Update blog fields
			blog.setTitle(request.getTitle().trim());
//			blog.setSlug(slug);
			blog.setSummary(request.getSummary());
			blog.setContent(request.getContent());

			// Update categories
			blog.setCategories(resolveCategories(request.getCategoryIds()));

			// Upload new images only when provided
			if (images != null && !images.isEmpty()) {

				Long currentUserId = CurrentUserService.getCurrentUserId();

				if (currentUserId == null) {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
				}

				Employee employee = employeeRepository.getReferenceById(currentUserId);

				// Keep existing images
				Set<MediaFile> mediaFiles = blog.getMediaFiles();

				if (mediaFiles == null) {
					mediaFiles = new HashSet<>();
				}

				// Save and associate new images
				for (MultipartFile image : images) {
					MediaFile savedMedia = saveBlogMediaFile(image, employee);

					mediaFiles.add(savedMedia);
				}

				blog.setMediaFiles(mediaFiles);
			}

			// Save updated blog
			Blog updatedBlog = blogRepository.save(blog);

			// Convert entity to response DTO
			BlogResponse blogResponse = convertToResponse(updatedBlog);

			return new ApiResponse("Blog updated successfully", blogResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			log.error("Database constraint violation while updating blog", e);

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"The blog could not be updated because a unique or related database constraint was violated");

		} catch (Exception e) {
			log.error("Failed to update blog", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update blog");
		}
	}

	// Save image to disk and metadata to database
	private MediaFile saveBlogMediaFile(MultipartFile file, Employee employee) {

		try {
			if (file == null || file.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file cannot be empty");
			}

			String contentType = file.getContentType();

			if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
			}

			String originalFilename = file.getOriginalFilename();

			if (originalFilename == null || originalFilename.trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image filename is required");
			}

			String storedFilename = UUID.randomUUID().toString();

			int dotIndex = originalFilename.lastIndexOf('.');

			if (dotIndex >= 0) {
				storedFilename += originalFilename.substring(dotIndex);
			}

			Path uploadDirectory = Paths.get("uploads", "blogs").toAbsolutePath().normalize();

			Files.createDirectories(uploadDirectory);

			Path destination = uploadDirectory.resolve(storedFilename).normalize();

			if (!destination.startsWith(uploadDirectory)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path");
			}

			Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

			MediaFile mediaFile = new MediaFile();

			mediaFile.setOriginalFilename(originalFilename);
			mediaFile.setStoredFilename(storedFilename);
			mediaFile.setFilePath(destination.toString());
			mediaFile.setContentType(contentType);
			mediaFile.setFileSize(file.getSize());
			mediaFile.setUploadedAt(LocalDateTime.now());
			mediaFile.setUploadedBy(employee);

			return mediaFileRepository.save(mediaFile);

		} catch (IOException e) {
			log.error("Failed to store blog image", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image");
		}
	}


	// Convert Blog entity to response DTO
	private BlogResponse convertToResponse(Blog blog) {

		BlogResponse response = new BlogResponse();

		response.setBlogId(blog.getBlogId());
		response.setTitle(blog.getTitle());
//		response.setSlug(blog.getSlug());
		response.setSummary(blog.getSummary());
		response.setContent(blog.getContent());
		response.setStatus(blog.getStatus());

		if (blog.getAuthor() != null) {
			response.setAuthorId(blog.getAuthor().getEmployeeId());

			// Keep the same employee-name getter as your existing code.
			response.setAuthorName(blog.getAuthor().getEmployeeName());
		}

		response.setCreatedAt(blog.getCreatedAt());
		response.setUpdatedAt(blog.getUpdatedAt());
		response.setPublishedAt(blog.getPublishedAt());

		Set<MediaFileResponse> mediaResponses = new HashSet<>();

		if (blog.getMediaFiles() != null) {
			for (MediaFile mediaFile : blog.getMediaFiles()) {
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

		response.setMediaFiles(mediaResponses);

		Set<CategoryResponse> categoryResponses = new HashSet<>();

		if (blog.getCategories() != null) {
			for (Category category : blog.getCategories()) {
				CategoryResponse categoryResponse = new CategoryResponse();

				categoryResponse.setCategoryId(category.getCategoryId());
				categoryResponse.setCategoryName(category.getCategoryName());
//				categoryResponse.setSlug(category.getSlug());
				categoryResponse.setDescription(category.getDescription());

				categoryResponses.add(categoryResponse);
			}
		}

		response.setCategories(categoryResponses);

		return response;
	}

	@Override
	public ApiResponse publishBlogs(List<Long> ids) {

		try {
			// Step 1: Remove duplicate IDs
			List<Long> distinctIds = ids.stream().distinct().toList();

			// Step 2: Find all existing blogs in one database query
			List<Blog> blogs = blogRepository.findAllById(distinctIds);

			// Step 3: Check if any blogs were found
			if (blogs.isEmpty()) {
				return new ApiResponse("No blogs found for the provided IDs", new ArrayList<>(), HttpStatus.OK.value());
			}

			// Step 4: Publish the existing blogs
			List<BlogResponse> responses = new ArrayList<>();

			for (Blog blog : blogs) {

				blog.setStatus("PUBLISHED");

				if (blog.getPublishedAt() == null) {
					blog.setPublishedAt(LocalDateTime.now());
				}

				Blog published = blogRepository.save(blog);

				responses.add(convertToResponse(published));
			}

			// Step 5: Return success
			return new ApiResponse("Blogs published successfully. Missing IDs were skipped.", responses,
					HttpStatus.OK.value());

		} catch (DataIntegrityViolationException e) {

			log.error("Database constraint violation publishing blogs", e);

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Unable to publish blogs because of a database constraint");

		} catch (Exception e) {

			log.error("Failed to publish blogs", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to publish blogs");
		}
	}
}
