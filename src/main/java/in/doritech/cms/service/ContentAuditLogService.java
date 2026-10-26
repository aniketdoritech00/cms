package in.doritech.cms.service;

import in.doritech.cms.util.ApiResponse;

public interface ContentAuditLogService {

	ApiResponse getAuditLogById(Long id);

	ApiResponse getAllAuditLogs();

	ApiResponse getAuditLogsByContent(String contentType, Long contentId);

	ApiResponse getAuditLogsByEmployee(Long employeeId);
}