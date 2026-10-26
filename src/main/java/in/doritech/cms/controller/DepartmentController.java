
package in.doritech.cms.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.doritech.cms.request.DepartmentRequest;
import in.doritech.cms.service.DepartmentService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/departments")
@CrossOrigin(allowedHeaders = "*")
public class DepartmentController {

	private final DepartmentService departmentService;

	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@PostMapping("/createDepartment")
	public ApiResponse createDepartment(@RequestBody DepartmentRequest request) {
		return departmentService.createDepartment(request);
	}

	@GetMapping("/getDepartmentById/{id}")
	public ApiResponse getDepartmentById(@PathVariable Long id) {
		return departmentService.getDepartmentById(id);
	}

	@GetMapping("/getAllDepartments")
	public ApiResponse getAllDepartments() {
		return departmentService.getAllDepartments();
	}

	@PutMapping("/updateDepartment/{id}")
	public ApiResponse updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest request) {
		return departmentService.updateDepartment(id, request);
	}

	@DeleteMapping("/deleteDepartment/{id}")
	public ApiResponse deleteDepartment(@PathVariable Long id) {
		return departmentService.deleteDepartment(id);
	}

	@GetMapping("/getAllDepartmentsWithPagination")
	public ApiResponse getAllDepartmentsWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return departmentService.getAllDepartmentsWithPagination(page, size);
	}

}
