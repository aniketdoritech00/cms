package in.doritech.cms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.doritech.cms.entity.Category;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryName(String categoryName);

    Optional<Category> findBySlug(String slug);

    boolean existsByCategoryName(String categoryName);

    boolean existsBySlug(String slug);
}