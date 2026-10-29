package in.doritech.cms.serviceImpl;

import java.util.ArrayList;
import java.util.List;
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
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Category;
import in.doritech.cms.repository.CategoryRepository;
import in.doritech.cms.request.CategoryRequest;
import in.doritech.cms.response.CategoryResponse;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.service.CategoryService;
import in.doritech.cms.util.ApiResponse;

@Service
public class CategoryServiceImpl implements CategoryService {

	private static final Logger log = LoggerFactory.getLogger(BlogServiceImpl.class);

	private final CategoryRepository categoryRepository;

	public CategoryServiceImpl(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Override
	public ApiResponse createCategory(CategoryRequest request) {
		try {
			validateRequest(request);

			Category category = new Category();
			category.setCategoryName(request.getCategoryName().trim());
//			category.setSlug(request.getSlug().trim());
			category.setDescription(request.getDescription());

			Category saved = categoryRepository.save(category);

			return new ApiResponse("Category created successfully", convertToResponse(saved),
					HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name or slug already exists");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create category");
		}
	}

	@Override
	public ApiResponse getCategoryById(Long id) {
		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));

			return new ApiResponse("Category fetched successfully", convertToResponse(category), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch category");
		}
	}

	@Override
	public ApiResponse getAllCategories() {
		try {
			List<CategoryResponse> responses = new ArrayList<>();

			for (Category category : categoryRepository.findAll()) {
				responses.add(convertToResponse(category));
			}

			return new ApiResponse("Categories fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch categories");
		}
	}

	@Override
	public ApiResponse updateCategory(Long id, CategoryRequest request) {
		try {
			validateRequest(request);

			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));

			category.setCategoryName(request.getCategoryName().trim());
//			category.setSlug(request.getSlug().trim());
			category.setDescription(request.getDescription());

			Category updated = categoryRepository.save(category);

			return new ApiResponse("Category updated successfully", convertToResponse(updated), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Category name or slug conflicts with existing data");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update category");
		}
	}

	@Override
	@Transactional
	public ApiResponse deleteCategory(Long id) {
		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found: " + id));

			Long newsCount = categoryRepository.countNewsMappings(id);

			Long blogCount = categoryRepository.countBlogMappings(id);

			if (newsCount > 0 || blogCount > 0) {

				String message;

				if (newsCount > 0 && blogCount > 0) {
					message = "Category is mapped with " + newsCount + " news article(s) and " + blogCount
							+ " blog(s). " + "Please remove the category mappings " + "before deleting this category.";

				} else if (newsCount > 0) {
					message = "Category is mapped with " + newsCount + " news article(s). "
							+ "Please remove the category mappings " + "before deleting this category.";

				} else {
					message = "Category is mapped with " + blogCount + " blog(s). "
							+ "Please remove the category mappings " + "before deleting this category.";
				}

				throw new ResponseStatusException(HttpStatus.CONFLICT, message);
			}

			categoryRepository.delete(category);

			return new ApiResponse("Category deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			log.error("Database constraint violation deleting category ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Category is currently in use and cannot be deleted");

		} catch (Exception e) {
			log.error("Failed to delete category ID: {}", id, e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete category");
		}
	}

	private void validateRequest(CategoryRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}

		if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name is required");
		}

		if (request.getCategoryName().trim().length() > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name must not exceed 100 characters");
		}

//		if (request.getSlug() == null || request.getSlug().trim().isEmpty()) {
//			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category slug is required");
//		}
	}

	private CategoryResponse convertToResponse(Category category) {
		CategoryResponse response = new CategoryResponse();

		response.setCategoryId(category.getCategoryId());
		response.setCategoryName(category.getCategoryName());
//		response.setSlug(category.getSlug());
		response.setDescription(category.getDescription());

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllCategoriesWithPagination(int page, int size) {
		try {
			// Validate page and size
			if (page < 0 || size <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Page must be 0 or greater and size must be greater than 0");
			}

			Pageable pageable = PageRequest.of(page, size);

			Page<Category> categoryPage = categoryRepository.findAll(pageable);

			List<CategoryResponse> responses = categoryPage.getContent().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			PageResponse<CategoryResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(categoryPage.getNumber());
			pageResponse.setPageSize(categoryPage.getSize());
			pageResponse.setTotalElements(categoryPage.getTotalElements());
			pageResponse.setTotalPages(categoryPage.getTotalPages());
			pageResponse.setLastPage(categoryPage.isLast());

			return new ApiResponse("Categories fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch categories with pagination", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch categories", e);
		}
	}
}
