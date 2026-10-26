
package in.doritech.cms.serviceImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Department;
import in.doritech.cms.entity.Employee;
import in.doritech.cms.entity.Role;
import in.doritech.cms.repository.DepartmentRepository;
import in.doritech.cms.repository.EmployeeRepository;
import in.doritech.cms.repository.RoleRepository;
import in.doritech.cms.request.EmployeeRequest;
import in.doritech.cms.response.EmployeeResponse;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.response.RoleResponse;
import in.doritech.cms.service.EmployeeService;
import in.doritech.cms.util.ApiResponse;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
			RoleRepository roleRepository, PasswordEncoder passwordEncoder) {

		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public ApiResponse createEmployee(EmployeeRequest request) {
		try {
			if (request == null || request.getEmployeeCode() == null || request.getEmployeeCode().trim().isEmpty()
					|| request.getEmployeeName() == null || request.getEmployeeName().trim().isEmpty()
					|| request.getEmail() == null || request.getEmail().trim().isEmpty()
					|| request.getPassword() == null || request.getPassword().isEmpty()
					|| request.getDepartmentId() == null || request.getRoleIds() == null
					|| request.getRoleIds().isEmpty()) {

				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Employee code, name, email, password, department and roles are required");
			}

			if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee email already exists");
			}

			Department department = departmentRepository.findById(request.getDepartmentId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

			Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));

			if (roles.size() != request.getRoleIds().size()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more role IDs are invalid");
			}

			Employee employee = new Employee();

			employee.setEmployeeCode(request.getEmployeeCode().trim());
			employee.setEmployeeName(request.getEmployeeName().trim());
			employee.setEmail(request.getEmail().trim());
			employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
			employee.setMobileNumber(request.getMobileNumber());
			employee.setStatus("ACTIVE");
			employee.setDepartment(department);
			employee.setRoles(roles);

			Employee saved = employeeRepository.save(employee);

			return new ApiResponse("Employee created successfully", convertToResponse(saved),
					HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code or email may already exist");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create employee");
		}
	}

	@Override
	public ApiResponse getEmployeeById(Long id) {
		try {
			Employee employee = employeeRepository.findById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id));

			return new ApiResponse("Employee fetched successfully", convertToResponse(employee), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch employee");
		}
	}

	@Override
	public ApiResponse getAllEmployees() {
		try {
			List<EmployeeResponse> responses = new ArrayList<>();

			for (Employee employee : employeeRepository.findAll()) {
				responses.add(convertToResponse(employee));
			}

			return new ApiResponse("Employees fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch employees");
		}
	}

	@Override
	public ApiResponse updateEmployee(Long id, EmployeeRequest request) {
		try {
			if (request == null || request.getEmployeeName() == null || request.getEmployeeName().trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee name is required");
			}

			Employee employee = employeeRepository.findById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id));

			employee.setEmployeeName(request.getEmployeeName().trim());

			if (request.getEmployeeCode() != null && !request.getEmployeeCode().trim().isEmpty()) {
				employee.setEmployeeCode(request.getEmployeeCode().trim());
			}

			if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
				employee.setEmail(request.getEmail().trim());
			}

			if (request.getMobileNumber() != null) {
				employee.setMobileNumber(request.getMobileNumber());
			}

			if (request.getPassword() != null && !request.getPassword().isEmpty()) {
				employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
			}

			if (request.getDepartmentId() != null) {
				Department department = departmentRepository.findById(request.getDepartmentId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

				employee.setDepartment(department);
			}

			if (request.getRoleIds() != null) {
				Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));

				if (roles.size() != request.getRoleIds().size()) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more role IDs are invalid");
				}

				employee.setRoles(roles);
			}

			Employee updated = employeeRepository.save(employee);

			return new ApiResponse("Employee updated successfully", convertToResponse(updated), HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code or email may already exist");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update employee");
		}
	}

	@Override
	public ApiResponse deleteEmployee(Long id) {
		try {
			Employee employee = employeeRepository.findById(id)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id));

			employeeRepository.delete(employee);

			return new ApiResponse("Employee deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee is referenced by other records");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete employee");
		}
	}

	private EmployeeResponse convertToResponse(Employee employee) {
		EmployeeResponse response = new EmployeeResponse();

		response.setEmployeeId(employee.getEmployeeId());
		response.setEmployeeCode(employee.getEmployeeCode());
		response.setEmployeeName(employee.getEmployeeName());
		response.setEmail(employee.getEmail());
		response.setMobileNumber(employee.getMobileNumber());
		response.setStatus(employee.getStatus());

		if (employee.getDepartment() != null) {
			response.setDepartmentId(employee.getDepartment().getDepartmentId());
			response.setDepartmentName(employee.getDepartment().getDepartmentName());
		}

		Set<RoleResponse> roleResponses = new HashSet<>();

		if (employee.getRoles() != null) {
			for (Role role : employee.getRoles()) {
				RoleResponse roleResponse = new RoleResponse();
				roleResponse.setRoleId(role.getRoleId());
				roleResponse.setRoleName(role.getRoleName());
				roleResponse.setDescription(role.getDescription());

				roleResponses.add(roleResponse);
			}
		}

		response.setRoles(roleResponses);
		response.setCreatedAt(employee.getCreatedAt());
		response.setUpdatedAt(employee.getUpdatedAt());

		return response;
	}

	@Override
	public ApiResponse getAllEmployeesWithPagination(int page, int size) {
		try {
			Pageable pageable = PageRequest.of(page, size);

			Page<Employee> employeePage = employeeRepository.findAll(pageable);

			List<EmployeeResponse> responses = new ArrayList<>();

			for (Employee employee : employeePage.getContent()) {
				responses.add(convertToResponse(employee));
			}

			PageResponse<EmployeeResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(employeePage.getNumber());
			pageResponse.setPageSize(employeePage.getSize());
			pageResponse.setTotalElements(employeePage.getTotalElements());
			pageResponse.setTotalPages(employeePage.getTotalPages());
			pageResponse.setLastPage(employeePage.isLast());

			return new ApiResponse("Employees fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch employees", e);
		}
	}

}
