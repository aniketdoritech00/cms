
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

import in.doritech.cms.request.EmployeeRequest;
import in.doritech.cms.service.EmployeeService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@PostMapping("/createEmployee")
	public ApiResponse createEmployee(@RequestBody EmployeeRequest request) {
		return employeeService.createEmployee(request);
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
}
