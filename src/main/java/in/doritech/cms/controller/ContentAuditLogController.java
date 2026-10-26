
package in.doritech.cms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.doritech.cms.service.ContentAuditLogService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/audit-logs")
public class ContentAuditLogController {

	private final ContentAuditLogService auditLogService;

	public ContentAuditLogController(ContentAuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@GetMapping("/getAuditLogById/{id}")
	public ApiResponse getAuditLogById(@PathVariable Long id) {
		return auditLogService.getAuditLogById(id);
	}

	@GetMapping("/getAllAuditLogs")
	public ApiResponse getAllAuditLogs() {
		return auditLogService.getAllAuditLogs();
	}

	@GetMapping("/getAuditLogsByContent")
	public ApiResponse getAuditLogsByContent(@RequestParam String contentType, @RequestParam Long contentId) {
		return auditLogService.getAuditLogsByContent(contentType, contentId);
	}

	@GetMapping("/getAuditLogsByEmployee/{employeeId}")
	public ApiResponse getAuditLogsByEmployee(@PathVariable Long employeeId) {
		return auditLogService.getAuditLogsByEmployee(employeeId);
	}
}
