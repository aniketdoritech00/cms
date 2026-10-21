package in.doritech.cms.service;

import java.util.List;

import in.doritech.cms.util.ApiResponse;

public interface ContentAuditLogService {

	ApiResponse getAuditLogById(Long id);

	List<ApiResponse> getAllAuditLogs();

	List<ApiResponse> getAuditLogsByContent(String contentType, Long contentId);

	List<ApiResponse> getAuditLogsByEmployee(Long employeeId);
}