package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.request.RoleRequest;
import in.doritech.cms.util.ApiResponse;


public interface RoleService {

    ApiResponse createRole(RoleRequest request);

    ApiResponse getRoleById(Long id);

    List<ApiResponse> getAllRoles();

    ApiResponse updateRole(Long id, RoleRequest request);

    ApiResponse deleteRole(Long id);
}