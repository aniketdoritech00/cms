package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.request.EmployeeRequest;
import in.doritech.cms.util.ApiResponse;

public interface EmployeeService {

	ApiResponse createEmployee(EmployeeRequest request);

	ApiResponse getEmployeeById(Long id);

	List<ApiResponse> getAllEmployees();

	ApiResponse updateEmployee(Long id, EmployeeRequest request);

	ApiResponse deleteEmployee(Long id);
}