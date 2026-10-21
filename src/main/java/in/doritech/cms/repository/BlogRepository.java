package in.doritech.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

	Optional<Blog> findBySlug(String slug);

	boolean existsBySlug(String slug);

	List<Blog> findByStatus(String status);

	List<Blog> findByAuthor_EmployeeId(Long employeeId);
}