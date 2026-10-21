package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.request.CategoryRequest;
import in.doritech.cms.util.ApiResponse;

public interface CategoryService {

	ApiResponse createCategory(CategoryRequest request);

	ApiResponse getCategoryById(Long id);

	List<ApiResponse> getAllCategories();

	ApiResponse updateCategory(Long id, CategoryRequest request);

	ApiResponse deleteCategory(Long id);
}