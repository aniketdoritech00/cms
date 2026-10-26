package in.doritech.cms.service;

import in.doritech.cms.request.RoleRequest;
import in.doritech.cms.util.ApiResponse;

public interface RoleService {

	ApiResponse createRole(RoleRequest request);

	ApiResponse getRoleById(Long id);

	ApiResponse getAllRoles();

	ApiResponse updateRole(Long id, RoleRequest request);

	ApiResponse deleteRole(Long id);

	ApiResponse getAllRolesWithPagination(int page, int size);
}