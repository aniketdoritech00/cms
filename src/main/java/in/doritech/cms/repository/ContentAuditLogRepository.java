
package in.doritech.cms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.doritech.cms.entity.ContentAuditLog;

public interface ContentAuditLogRepository extends JpaRepository<ContentAuditLog, Long> {

	List<ContentAuditLog> findByContentTypeAndContentId(String contentType, Long contentId);

	List<ContentAuditLog> findByEmployee_EmployeeId(Long employeeId);
}
