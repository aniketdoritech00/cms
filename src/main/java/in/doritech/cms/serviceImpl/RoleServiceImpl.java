package in.doritech.cms.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Role;
import in.doritech.cms.repository.RoleRepository;
import in.doritech.cms.request.RoleRequest;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.response.RoleResponse;
import in.doritech.cms.service.RoleService;
import in.doritech.cms.util.ApiResponse;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

	private final RoleRepository roleRepository;

	public RoleServiceImpl(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public ApiResponse createRole(RoleRequest request) {
		try {
			validateRequest(request);

			String roleName = request.getRoleName().trim();

			Role role = new Role();
			role.setRoleName(roleName);
			role.setDescription(normalizeDescription(request.getDescription()));

			Role saved = roleRepository.save(role);

			return new ApiResponse("Role created successfully", convertToResponse(saved), HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Role name already exists or violates a database constraint");

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create role");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getRoleById(Long id) {
		try {
			Role role = findRoleById(id);

			return new ApiResponse("Role fetched successfully", convertToResponse(role), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch role");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllRoles() {
		try {
			List<RoleResponse> responses = new ArrayList<>();

			for (Role role : roleRepository.findAll()) {
				responses.add(convertToResponse(role));
			}

			return new ApiResponse("Roles fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch roles");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllRolesWithPagination(int page, int size) {
		try {
			if (page < 0 || size <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Page must be 0 or greater and size must be greater than 0");
			}

			Pageable pageable = PageRequest.of(page, size);

			Page<Role> rolePage = roleRepository.findAll(pageable);

			List<RoleResponse> responses = new ArrayList<>();

			for (Role role : rolePage.getContent()) {
				responses.add(convertToResponse(role));
			}

			PageResponse<RoleResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(rolePage.getNumber());
			pageResponse.setPageSize(rolePage.getSize());
			pageResponse.setTotalElements(rolePage.getTotalElements());
			pageResponse.setTotalPages(rolePage.getTotalPages());
			pageResponse.setLastPage(rolePage.isLast());

			return new ApiResponse("Roles fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch roles", e);
		}
	}

	@Override
	public ApiResponse updateRole(Long id, RoleRequest request) {
		try {
			validateRequest(request);

			Role role = findRoleById(id);

			String roleName = request.getRoleName().trim();

			boolean duplicateExists = roleRepository.existsByRoleNameAndRoleIdNot(roleName, id);

			if (duplicateExists) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name '" + roleName + "' already exists");
			}

			role.setRoleName(roleName);
			role.setDescription(normalizeDescription(request.getDescription()));

			Role updated = roleRepository.saveAndFlush(role);

			return new ApiResponse("Role updated successfully", convertToResponse(updated), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Role name already exists or conflicts with existing data");

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update role");
		}
	}

	@Override
	public ApiResponse deleteRole(Long id) {
		try {
			Role role = findRoleById(id);

			roleRepository.delete(role);
			roleRepository.flush();

			return new ApiResponse("Role deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"This role is assigned to one or more employees and cannot be deleted");

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete role");
		}
	}

	private Role findRoleById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role ID is required");
		}

		if (id <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role ID must be greater than zero");
		}

		return roleRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + id));
	}

	private void validateRequest(RoleRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}

		if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name is required");
		}

		if (request.getRoleName().trim().length() > 50) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role name must not exceed 50 characters");
		}
	}

	private String normalizeDescription(String description) {
		if (description == null || description.trim().isEmpty()) {
			return null;
		}

		return description.trim();
	}

	private RoleResponse convertToResponse(Role role) {
		RoleResponse response = new RoleResponse();

		response.setRoleId(role.getRoleId());
		response.setRoleName(role.getRoleName());
		response.setDescription(role.getDescription());

		return response;
	}
}