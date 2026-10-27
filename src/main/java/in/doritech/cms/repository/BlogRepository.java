package in.doritech.cms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

	Optional<Blog> findBySlug(String slug);

	boolean existsBySlug(String slug);

	List<Blog> findByStatus(String status);

	List<Blog> findByAuthor_EmployeeId(Long employeeId);

	Page<Blog> findByStatus(String string, Pageable pageable);
}