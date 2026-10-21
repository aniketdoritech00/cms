package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.request.DepartmentRequest;
import in.doritech.cms.util.ApiResponse;

public interface DepartmentService {

	ApiResponse createDepartment(DepartmentRequest request);

	ApiResponse getDepartmentById(Long id);

	List<ApiResponse> getAllDepartments();

	ApiResponse updateDepartment(Long id, DepartmentRequest request);

	ApiResponse deleteDepartment(Long id);
}