package in.doritech.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

	Optional<News> findBySlug(String slug);

	boolean existsBySlug(String slug);

	List<News> findByStatus(String status);

	List<News> findByAuthor_EmployeeId(Long employeeId);
}