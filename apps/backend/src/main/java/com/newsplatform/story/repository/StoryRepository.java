package com.newsplatform.story.repository;

import com.newsplatform.story.entity.Story;
import com.newsplatform.story.entity.StoryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoryRepository extends JpaRepository<Story, UUID> {
    boolean existsBySlug(String slug);
    boolean existsByCategoryId(UUID categoryId);
    boolean existsByTags_Id(UUID tagId);

    @Query("select distinct s from Story s where (:search = '' or lower(s.title) like concat('%', :search, '%') or lower(coalesce(s.summary, '')) like concat('%', :search, '%')) and (:status is null or s.status = :status) and (:categoryId is null or s.category.id = :categoryId) order by s.updatedAt desc")
    List<Story> findAdmin(@Param("search") String search, @Param("status") StoryStatus status, @Param("categoryId") UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags", "media", "author"})
    Optional<Story> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"category", "tags", "media", "author"})
    Optional<Story> findWithDetailsBySlugAndStatus(String slug, StoryStatus status);

    @Query("select distinct s from Story s join fetch s.category c where s.status = 'PUBLISHED' and (:categorySlug is null or c.slug = :categorySlug) and (:tagSlug is null or exists (select filterStoryTag from Story storyFilter join storyFilter.tags filterStoryTag where storyFilter = s and filterStoryTag.slug = :tagSlug)) order by s.publishedAt desc, s.id desc")
    List<Story> findPublishedFeedInitial(@Param("categorySlug") String categorySlug, @Param("tagSlug") String tagSlug, Pageable pageable);

    @Query("select distinct s from Story s join fetch s.category c where s.status = 'PUBLISHED' and (:categorySlug is null or c.slug = :categorySlug) and (:tagSlug is null or exists (select filterStoryTag from Story storyFilter join storyFilter.tags filterStoryTag where storyFilter = s and filterStoryTag.slug = :tagSlug)) and (s.publishedAt < :cursorTime or (s.publishedAt = :cursorTime and s.id < :cursorId)) order by s.publishedAt desc, s.id desc")
    List<Story> findPublishedFeedAfter(@Param("categorySlug") String categorySlug, @Param("tagSlug") String tagSlug, @Param("cursorTime") Instant cursorTime, @Param("cursorId") UUID cursorId, Pageable pageable);
}
