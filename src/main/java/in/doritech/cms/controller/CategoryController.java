
package in.doritech.cms.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.doritech.cms.request.CategoryRequest;
import in.doritech.cms.service.CategoryService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/categories")
public class CategoryController {

	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@PostMapping("/createCategory")
	public ApiResponse createCategory(@RequestBody CategoryRequest request) {
		return categoryService.createCategory(request);
	}

	@GetMapping("/getCategoryById/{id}")
	public ApiResponse getCategoryById(@PathVariable Long id) {
		return categoryService.getCategoryById(id);
	}

	@GetMapping("/getAllCategories")
	public ApiResponse getAllCategories() {
		return categoryService.getAllCategories();
	}

	@GetMapping("/getAllCategoriesWithPagination")
	public ApiResponse getAllCategoriesWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return categoryService.getAllCategoriesWithPagination(page, size);
	}

	@PutMapping("/updateCategory/{id}")
	public ApiResponse updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
		return categoryService.updateCategory(id, request);
	}

	@DeleteMapping("/deleteCategory/{id}")
	public ApiResponse deleteCategory(@PathVariable Long id) {
		return categoryService.deleteCategory(id);
	}
}
