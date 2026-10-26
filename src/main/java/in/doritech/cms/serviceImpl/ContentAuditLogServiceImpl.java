
package in.doritech.cms.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.ContentAuditLog;
import in.doritech.cms.repository.ContentAuditLogRepository;
import in.doritech.cms.response.ContentAuditLogResponse;
import in.doritech.cms.service.ContentAuditLogService;
import in.doritech.cms.util.ApiResponse;

@Service
public class ContentAuditLogServiceImpl implements ContentAuditLogService {

    private final ContentAuditLogRepository auditLogRepository;

    public ContentAuditLogServiceImpl(
            ContentAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public ApiResponse getAuditLogById(Long id) {
        try {
            ContentAuditLog log = auditLogRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Audit log not found: " + id));

            return new ApiResponse(
                    "Audit log fetched successfully",
                    convertToResponse(log),
                    HttpStatus.OK.value());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch audit log");
        }
    }

    @Override
    public ApiResponse getAllAuditLogs() {
        try {
            List<ContentAuditLogResponse> responses = new ArrayList<>();

            for (ContentAuditLog log : auditLogRepository.findAll()) {
                responses.add(convertToResponse(log));
            }

            return new ApiResponse(
                    "Audit logs fetched successfully",
                    responses,
                    HttpStatus.OK.value());

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch audit logs");
        }
    }

    @Override
    public ApiResponse getAuditLogsByContent(
            String contentType, Long contentId) {
        try {
            if (contentType == null || contentType.trim().isEmpty()
                    || contentId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Content type and content ID are required");
            }

            List<ContentAuditLogResponse> responses = new ArrayList<>();

            List<ContentAuditLog> logs =
                    auditLogRepository.findByContentTypeAndContentId(
                            contentType, contentId);

            for (ContentAuditLog log : logs) {
                responses.add(convertToResponse(log));
            }

            return new ApiResponse(
                    "Content audit logs fetched successfully",
                    responses,
                    HttpStatus.OK.value());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch content audit logs");
        }
    }

    @Override
    public ApiResponse getAuditLogsByEmployee(Long employeeId) {
        try {
            if (employeeId == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Employee ID is required");
            }

            List<ContentAuditLogResponse> responses = new ArrayList<>();

            List<ContentAuditLog> logs =
                    auditLogRepository.findByEmployee_EmployeeId(employeeId);

            for (ContentAuditLog log : logs) {
                responses.add(convertToResponse(log));
            }

            return new ApiResponse(
                    "Employee audit logs fetched successfully",
                    responses,
                    HttpStatus.OK.value());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch employee audit logs");
        }
    }

    private ContentAuditLogResponse convertToResponse(ContentAuditLog log) {

        ContentAuditLogResponse response = new ContentAuditLogResponse();

        response.setAuditId(log.getAuditId());
        response.setContentType(log.getContentType());
        response.setContentId(log.getContentId());
        response.setAction(log.getAction());
        response.setDescription(log.getDescription());
        response.setCreatedAt(log.getCreatedAt());

        if (log.getEmployee() != null) {
            response.setEmployeeId(
                    log.getEmployee().getEmployeeId());

            response.setEmployeeName(
                    log.getEmployee().getEmployeeName());
        }

        return response;
    }
}
