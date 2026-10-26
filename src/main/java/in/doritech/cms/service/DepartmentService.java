package in.doritech.cms.service;

import in.doritech.cms.request.DepartmentRequest;
import in.doritech.cms.util.ApiResponse;

public interface DepartmentService {

	ApiResponse createDepartment(DepartmentRequest request);

	ApiResponse getDepartmentById(Long id);

	ApiResponse getAllDepartments();

	ApiResponse updateDepartment(Long id, DepartmentRequest request);

	ApiResponse deleteDepartment(Long id);

	ApiResponse getAllDepartmentsWithPagination(int page, int size);
}