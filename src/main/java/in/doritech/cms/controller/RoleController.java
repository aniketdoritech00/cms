
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

import in.doritech.cms.request.RoleRequest;
import in.doritech.cms.service.RoleService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/roles")
@CrossOrigin(allowedHeaders = "*")
public class RoleController {

	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@PostMapping("/createRole")
	public ApiResponse createRole(@RequestBody RoleRequest request) {
		return roleService.createRole(request);
	}

	@GetMapping("/getRoleById/{id}")
	public ApiResponse getRoleById(@PathVariable Long id) {
		return roleService.getRoleById(id);
	}

	@GetMapping("/getAllRoles")
	public ApiResponse getAllRoles() {
		return roleService.getAllRoles();
	}

	@GetMapping("/getAllRolesWithPagination")
	public ApiResponse getAllRolesWithPagination(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return roleService.getAllRolesWithPagination(page, size);
	}

	@PutMapping("/updateRole/{id}")
	public ApiResponse updateRole(@PathVariable Long id, @RequestBody RoleRequest request) {
		System.out.println("Request is" + request.toString());
		return roleService.updateRole(id, request);
	}

	@DeleteMapping("/deleteRole/{id}")
	public ApiResponse deleteRole(@PathVariable Long id) {
		return roleService.deleteRole(id);
	}
}
