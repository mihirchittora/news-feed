package com.newsplatform.category.repository;

import com.newsplatform.category.entity.Category;
import com.newsplatform.category.entity.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByStatusOrderByDisplayOrderAscNameAsc(CategoryStatus status);
    List<Category> findAllByOrderByDisplayOrderAscNameAsc();
    java.util.Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
    boolean existsByParentId(UUID parentId);

    @Query("select count(c) > 0 from Category c where lower(c.name) = lower(:name) and ((:parentId is null and c.parent is null) or c.parent.id = :parentId)")
    boolean existsSiblingName(@Param("name") String name, @Param("parentId") UUID parentId);

    @Query("select count(c) > 0 from Category c where c.id <> :id and lower(c.name) = lower(:name) and ((:parentId is null and c.parent is null) or c.parent.id = :parentId)")
    boolean existsSiblingNameAndIdNot(@Param("name") String name, @Param("parentId") UUID parentId, @Param("id") UUID id);
}
