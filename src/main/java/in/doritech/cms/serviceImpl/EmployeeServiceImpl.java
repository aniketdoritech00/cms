
package in.doritech.cms.serviceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Department;
import in.doritech.cms.entity.Employee;
import in.doritech.cms.entity.MediaFile;
import in.doritech.cms.entity.Role;
import in.doritech.cms.repository.DepartmentRepository;
import in.doritech.cms.repository.EmployeeRepository;
import in.doritech.cms.repository.MediaFileRepository;
import in.doritech.cms.repository.RoleRepository;
import in.doritech.cms.request.EmployeeRequest;
import in.doritech.cms.response.EmployeeResponse;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.response.RoleResponse;
import in.doritech.cms.security.CurrentUserService;
import in.doritech.cms.service.EmployeeService;
import in.doritech.cms.service.ParamService;
import in.doritech.cms.util.ApiResponse;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	private final EmployeeRepository employeeRepository;
	private final DepartmentRepository departmentRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final MediaFileRepository mediaFileRepository;
	private final ParamService paramService;

	public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
			RoleRepository roleRepository, PasswordEncoder passwordEncoder, MediaFileRepository mediaFileRepository,
			ParamService paramService) {

		this.employeeRepository = employeeRepository;
		this.departmentRepository = departmentRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.mediaFileRepository = mediaFileRepository;
		this.paramService = paramService;
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

			paramService.updateCodeValue(saved.getEmployeeCode());
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
	public ApiResponse getCurrentEmployee() {

		try {
			Long currentUserEmployeeId = CurrentUserService.getCurrentUserId();
			if (currentUserEmployeeId == null) {
				return new ApiResponse("Current employee id not found", null, HttpStatus.BAD_REQUEST.value());
			}
			Employee employee = employeeRepository.findById(currentUserEmployeeId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
							"Employee not found: " + currentUserEmployeeId));

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

		if (employee.getProfilePicture() != null) {
			response.setProfilePictureId(employee.getProfilePicture().getMediaId());
		}

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

	@Override
	@Transactional
	public ApiResponse createEmployee(EmployeeRequest request, MultipartFile profilePicture) {

		try {
			// Validate employee request
			if (request == null || request.getEmployeeCode() == null || request.getEmployeeCode().trim().isEmpty()
					|| request.getEmployeeName() == null || request.getEmployeeName().trim().isEmpty()
					|| request.getEmail() == null || request.getEmail().trim().isEmpty()
					|| request.getPassword() == null || request.getPassword().isEmpty()
					|| request.getDepartmentId() == null || request.getRoleIds() == null
					|| request.getRoleIds().isEmpty()) {

				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Employee code, name, email, password, department and roles are required");
			}

			// Check duplicate email
			if (employeeRepository.findByEmail(request.getEmail().trim()).isPresent()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee email already exists");
			}

			// Find department
			Department department = departmentRepository.findById(request.getDepartmentId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

			// Find roles
			Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));

			if (roles.size() != request.getRoleIds().size()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more role IDs are invalid");
			}

			// Create employee
			Employee employee = new Employee();

			employee.setEmployeeCode(request.getEmployeeCode().trim());
			employee.setEmployeeName(request.getEmployeeName().trim());
			employee.setEmail(request.getEmail().trim());
			if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {

				if (request.getPassword().length() < 4) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Password must contain at least 4 characters");
				}

				employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
			}

			employee.setMobileNumber(request.getMobileNumber());
			employee.setStatus("ACTIVE");
			employee.setDepartment(department);
			employee.setRoles(roles);

			// Step 1: Save employee first
			Employee savedEmployee = employeeRepository.save(employee);

			// Step 2: Save profile picture in media_files, if provided
			if (profilePicture != null && !profilePicture.isEmpty()) {

				MediaFile savedMedia = saveEmployeeProfilePicture(profilePicture, savedEmployee);

				// Step 3: Store the MediaFile reference in Employee
				savedEmployee.setProfilePicture(savedMedia);

				// Update employee with profile picture media ID
				savedEmployee = employeeRepository.save(savedEmployee);
			}

			paramService.updateCodeValue(savedEmployee.getEmployeeCode());

			return new ApiResponse("Employee created successfully", convertToResponse(savedEmployee),
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
	@Transactional
	public ApiResponse updateEmployee(Long id, EmployeeRequest request, MultipartFile profilePicture) {

		try {

			// Validate employee request
			if (request == null || request.getEmployeeCode() == null || request.getEmployeeCode().trim().isEmpty()
					|| request.getEmployeeName() == null || request.getEmployeeName().trim().isEmpty()
					|| request.getEmail() == null || request.getEmail().trim().isEmpty()
					|| request.getDepartmentId() == null || request.getRoleIds() == null
					|| request.getRoleIds().isEmpty()) {

				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Employee code, name, email, department and roles are required");
			}

			// Find existing employee
			Employee employee = employeeRepository.findById(id).orElseThrow(
					() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found with id: " + id));

			String employeeCode = request.getEmployeeCode().trim();
			String email = request.getEmail().trim();

			// Check duplicate employee code
			if (employeeRepository.existsByEmployeeCodeAndEmployeeIdNot(employeeCode, id)) {

				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists");
			}

			// Check duplicate email
			if (employeeRepository.findByEmail(email).filter(existing -> !existing.getEmployeeId().equals(id))
					.isPresent()) {

				throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee email already exists");
			}

			// Find department
			Department department = departmentRepository.findById(request.getDepartmentId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

			// Find roles
			Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));

			if (roles.size() != request.getRoleIds().size()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more role IDs are invalid");
			}

			// Update employee details
			employee.setEmployeeCode(employeeCode);
			employee.setEmployeeName(request.getEmployeeName().trim());
			employee.setEmail(email);
			employee.setMobileNumber(request.getMobileNumber());
			employee.setDepartment(department);
			employee.setRoles(roles);

			// Update password only when a new password is provided
			if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {

				if (request.getPassword().length() < 4) {
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
							"Password must contain at least 4 characters");
				}

				employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
			}

			// Update profile picture only when a new one is provided
			if (profilePicture != null && !profilePicture.isEmpty()) {

				MediaFile savedMedia = saveEmployeeProfilePicture(profilePicture, employee);

				employee.setProfilePicture(savedMedia);
			}

			// Save updated employee
			Employee updatedEmployee = employeeRepository.save(employee);

			return new ApiResponse("Employee updated successfully", convertToResponse(updatedEmployee),
					HttpStatus.OK.value());

		} catch (ResponseStatusException e) {

			throw e;

		} catch (DataIntegrityViolationException e) {

			throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code or email may already exist");

		} catch (Exception e) {

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update employee");
		}
	}

	private MediaFile saveEmployeeProfilePicture(MultipartFile file, Employee employee) {

		try {
			if (file == null || file.isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile picture cannot be empty");
			}

			if (file.getSize() > 5 * 1024 * 1024) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile picture must not exceed 5 MB");
			}

			String contentType = file.getContentType();

			if (contentType == null || !Set.of("image/jpeg", "image/png", "image/webp")
					.contains(contentType.toLowerCase(Locale.ROOT))) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG and WebP images are allowed");
			}

			String originalFilename = file.getOriginalFilename();

			if (originalFilename == null || originalFilename.trim().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image filename is required");
			}

			int dotIndex = originalFilename.lastIndexOf('.');

			if (dotIndex < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file extension is required");
			}

			String extension = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);

			if (!Set.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Only JPG, JPEG, PNG and WebP files are allowed");
			}

			String storedFilename = UUID.randomUUID() + extension;

			Path uploadDirectory = Paths.get("uploads", "employees").toAbsolutePath().normalize();

			Files.createDirectories(uploadDirectory);

			Path destination = uploadDirectory.resolve(storedFilename).normalize();

			if (!destination.startsWith(uploadDirectory)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path");
			}

			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destination);
			}

			MediaFile mediaFile = new MediaFile();
			mediaFile.setOriginalFilename(originalFilename);
			mediaFile.setStoredFilename(storedFilename);
			mediaFile.setFilePath(destination.toString());
			mediaFile.setContentType(contentType);
			mediaFile.setFileSize(file.getSize());
			mediaFile.setUploadedAt(LocalDateTime.now());
			mediaFile.setUploadedBy(employee);

			return mediaFileRepository.save(mediaFile);

		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store profile picture");
		}
	}

	@Override
	public ApiResponse changePassword(String currentPassword, String newPassword) {

		try {
			Long employeeId = CurrentUserService.getCurrentUserId();

			Employee employee = employeeRepository.findById(employeeId).orElse(null);

			if (employee == null) {
				return new ApiResponse("Employee not found", null, 404);
			}

			if (!passwordEncoder.matches(currentPassword, employee.getPasswordHash())) {
				return new ApiResponse("Current password is incorrect", null, 400);
			}

			if (passwordEncoder.matches(newPassword, employee.getPasswordHash())) {
				return new ApiResponse("New password must be different from current password", null, 400);
			}

			employee.setPasswordHash(passwordEncoder.encode(newPassword));
			employeeRepository.save(employee);

			return new ApiResponse("Password changed successfully", null, 200);

		} catch (Exception e) {
			return new ApiResponse("Unable to change password", null, 500);
		}
	}

}
