package in.doritech.cms.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
//
//	Optional<News> findBySlug(String slug);
//
//	boolean existsBySlug(String slug);

	List<News> findByStatus(String status);

	List<News> findByAuthor_EmployeeId(Long employeeId);

	Page<News> findByStatus(String string, Pageable pageable);

	boolean existsByMediaFiles_MediaId(Long id);

	@Query(value = """
			SELECT COUNT(*)
			FROM news_media
			WHERE media_id = :mediaId
			""", nativeQuery = true)
	Long countNewsUsingMedia(@Param("mediaId") Long mediaId);
}