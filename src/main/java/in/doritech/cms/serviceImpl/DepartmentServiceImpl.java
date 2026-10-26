package in.doritech.cms.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Department;
import in.doritech.cms.repository.DepartmentRepository;
import in.doritech.cms.request.DepartmentRequest;
import in.doritech.cms.response.DepartmentResponse;
import in.doritech.cms.response.PageResponse;
import in.doritech.cms.service.DepartmentService;
import in.doritech.cms.util.ApiResponse;

@Service
public class DepartmentServiceImpl implements DepartmentService {

	private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

	private final DepartmentRepository departmentRepository;

	public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
		this.departmentRepository = departmentRepository;
	}

	@Override
	public ApiResponse createDepartment(DepartmentRequest request) {
		try {
			validateRequest(request);

			Department department = new Department();
			department.setDepartmentName(request.getDepartmentName().trim());
			department.setDescription(request.getDescription());

			Department saved = departmentRepository.save(department);

			return new ApiResponse("Department created successfully", convertToResponse(saved),
					HttpStatus.CREATED.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Department already exists or violates a database constraint");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create department");
		}
	}

	@Override
	public ApiResponse getDepartmentById(Long id) {
		try {
			Department department = findDepartmentById(id);

			return new ApiResponse("Department fetched successfully", convertToResponse(department),
					HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch department");
		}
	}

	@Override
	public ApiResponse getAllDepartments() {
		try {
			List<DepartmentResponse> responses = new ArrayList<>();

			for (Department department : departmentRepository.findAll()) {
				responses.add(convertToResponse(department));
			}

			return new ApiResponse("Departments fetched successfully", responses, HttpStatus.OK.value());

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch departments");
		}
	}

	@Override
	public ApiResponse updateDepartment(Long id, DepartmentRequest request) {
		try {
			validateRequest(request);

			Department department = findDepartmentById(id);

			department.setDepartmentName(request.getDepartmentName().trim());
			department.setDescription(request.getDescription());

			Department updated = departmentRepository.save(department);

			return new ApiResponse("Department updated successfully", convertToResponse(updated),
					HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Department conflicts with existing data");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update department");
		}
	}

	@Override
	public ApiResponse deleteDepartment(Long id) {
		try {
			Department department = findDepartmentById(id);

			departmentRepository.delete(department);

			departmentRepository.flush();

			return new ApiResponse("Department deleted successfully", null, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;

		} catch (DataIntegrityViolationException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Cannot delete this department because it is assigned to one or more employees. "
							+ "Reassign the employees to another department before deleting it.");

		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"An unexpected error occurred while deleting the department");
		}
	}

	private Department findDepartmentById(Long id) {
		if (id == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department ID is required");
		}

		return departmentRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found: " + id));
	}

	private void validateRequest(DepartmentRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}

		if (request.getDepartmentName() == null || request.getDepartmentName().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name is required");
		}

		if (request.getDepartmentName().trim().length() > 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department name must not exceed 100 characters");
		}
	}

	private DepartmentResponse convertToResponse(Department department) {

		DepartmentResponse response = new DepartmentResponse();

		response.setDepartmentId(department.getDepartmentId());
		response.setDepartmentName(department.getDepartmentName());
		response.setDescription(department.getDescription());

		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse getAllDepartmentsWithPagination(int page, int size) {
		try {
			// Validate pagination values
			if (page < 0 || size <= 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Page must be 0 or greater and size must be greater than 0");
			}

			// Create pagination request
			Pageable pageable = PageRequest.of(page, size);

			// Fetch departments page by page
			Page<Department> departmentPage = departmentRepository.findAll(pageable);

			// Convert entities into response DTOs
			List<DepartmentResponse> responses = departmentPage.getContent().stream().map(this::convertToResponse)
					.collect(Collectors.toList());

			// Create pagination response
			PageResponse<DepartmentResponse> pageResponse = new PageResponse<>();

			pageResponse.setContent(responses);
			pageResponse.setPageNumber(departmentPage.getNumber());
			pageResponse.setPageSize(departmentPage.getSize());
			pageResponse.setTotalElements(departmentPage.getTotalElements());
			pageResponse.setTotalPages(departmentPage.getTotalPages());
			pageResponse.setLastPage(departmentPage.isLast());

			return new ApiResponse("Departments fetched successfully", pageResponse, HttpStatus.OK.value());

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error("Failed to fetch departments with pagination", e);

			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch departments", e);
		}
	}

}