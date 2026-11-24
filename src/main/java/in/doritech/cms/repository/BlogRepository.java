package in.doritech.cms.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

//	Optional<Blog> findBySlug(String slug);

//	boolean existsBySlug(String slug);

	List<Blog> findByStatus(String status);

	List<Blog> findByAuthor_EmployeeId(Long employeeId);

	Page<Blog> findByStatus(String string, Pageable pageable);

	boolean existsByMediaFiles_MediaId(Long id);

	@Query(value = """
			SELECT COUNT(*)
			FROM blog_media
			WHERE media_id = :mediaId
			""", nativeQuery = true)
	Long countBlogsUsingMedia(@Param("mediaId") Long mediaId);
}