
package in.doritech.cms.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.request.EmployeeRequest;
import in.doritech.cms.service.EmployeeService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/employees")
@CrossOrigin(allowedHeaders = "*")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@PostMapping("/createEmployee")
	public ApiResponse createEmployee(@RequestBody EmployeeRequest request) {
		return employeeService.createEmployee(request);
	}

	@PostMapping(value = "/createEmployee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse createEmployee(@ModelAttribute EmployeeRequest request,
			@RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
		return employeeService.createEmployee(request, profilePicture);
	}

	@PutMapping(value = "/updateEmployee/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse updateEmployee(@PathVariable Long id, @ModelAttribute EmployeeRequest request,
			@RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
		return employeeService.updateEmployee(id, request, profilePicture);
	}

	@GetMapping("/getEmployeeById/{id}")
	public ApiResponse getEmployeeById(@PathVariable Long id) {
		return employeeService.getEmployeeById(id);
	}

	@GetMapping("/getAllEmployees")
	public ApiResponse getAllEmployees() {
		return employeeService.getAllEmployees();
	}

	@GetMapping("/getAllEmployeesWithPagination")
	public ApiResponse getAllEmployees(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return employeeService.getAllEmployeesWithPagination(page, size);
	}

	@PutMapping("/updateEmployee/{id}")
	public ApiResponse updateEmployee(@PathVariable Long id, @RequestBody EmployeeRequest request) {
		return employeeService.updateEmployee(id, request);
	}

	@DeleteMapping("/deleteEmployee/{id}")
	public ApiResponse deleteEmployee(@PathVariable Long id) {
		return employeeService.deleteEmployee(id);
	}

	@PutMapping("/changePassword")
	public ApiResponse changePassword(@RequestParam String currentPassword, @RequestParam String newPassword) {
		return employeeService.changePassword(currentPassword, newPassword);
	}
}
