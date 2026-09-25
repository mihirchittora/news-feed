package com.newsplatform.tag.repository;

import com.newsplatform.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findAllByOrderByNameAsc();
    List<Tag> findByNameContainingIgnoreCaseOrSlugContainingIgnoreCaseOrderByNameAsc(String name, String slug);
    boolean existsByNormalizedName(String normalizedName);
    boolean existsBySlug(String slug);
    boolean existsByNormalizedNameAndIdNot(String normalizedName, UUID id);
    boolean existsBySlugAndIdNot(String slug, UUID id);
}
