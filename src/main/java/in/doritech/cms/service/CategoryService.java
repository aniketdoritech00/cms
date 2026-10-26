package in.doritech.cms.service;

import in.doritech.cms.request.CategoryRequest;
import in.doritech.cms.util.ApiResponse;

public interface CategoryService {

	ApiResponse createCategory(CategoryRequest request);

	ApiResponse getCategoryById(Long id);

	ApiResponse getAllCategories();

	ApiResponse updateCategory(Long id, CategoryRequest request);

	ApiResponse deleteCategory(Long id);

	ApiResponse getAllCategoriesWithPagination(int page, int size);
}