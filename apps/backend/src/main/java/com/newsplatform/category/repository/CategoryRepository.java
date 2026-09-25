package com.newsplatform.category.repository;

import com.newsplatform.category.entity.Category;
import com.newsplatform.category.entity.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByStatusOrderByDisplayOrderAscNameAsc(CategoryStatus status);
    List<Category> findAllByOrderByDisplayOrderAscNameAsc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsBySlug(String slug);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    boolean existsBySlugAndIdNot(String slug, UUID id);
}
