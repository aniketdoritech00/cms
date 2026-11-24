package in.doritech.cms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	Optional<Category> findByCategoryName(String categoryName);

//    Optional<Category> findBySlug(String slug);

	boolean existsByCategoryName(String categoryName);

	@Query(value = """
			SELECT COUNT(*)
			FROM news_categories
			WHERE category_id = :categoryId
			""", nativeQuery = true)
	Long countNewsMappings(@Param("categoryId") Long categoryId);

	@Query(value = """
			SELECT COUNT(*)
			FROM blog_categories
			WHERE category_id = :categoryId
			""", nativeQuery = true)
	Long countBlogMappings(@Param("categoryId") Long categoryId);

//    boolean existsBySlug(String slug);
}