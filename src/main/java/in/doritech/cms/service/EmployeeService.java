package in.doritech.cms.service;

import org.springframework.web.multipart.MultipartFile;

import in.doritech.cms.request.EmployeeRequest;
import in.doritech.cms.util.ApiResponse;

public interface EmployeeService {

	ApiResponse createEmployee(EmployeeRequest request);

	ApiResponse getEmployeeById(Long id);

	ApiResponse getAllEmployees();

	ApiResponse updateEmployee(Long id, EmployeeRequest request);

	ApiResponse deleteEmployee(Long id);

	ApiResponse getAllEmployeesWithPagination(int page, int size);

	ApiResponse createEmployee(EmployeeRequest request, MultipartFile profilePicture);

	ApiResponse updateEmployee(Long id, EmployeeRequest request, MultipartFile profilePicture);
}