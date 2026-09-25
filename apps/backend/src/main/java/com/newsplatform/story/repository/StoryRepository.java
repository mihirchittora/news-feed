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

    @Query("select distinct s from Story s where (:search = '' or lower(s.title) like concat('%', :search, '%') or lower(coalesce(s.summary, '')) like concat('%', :search, '%')) and (:status is null or s.status = :status) and (:categoryId is null or s.category.id = :categoryId) and (:breaking is null or (:breaking = true and s.status = 'PUBLISHED' and s.breaking = true and s.breakingStartedAt <= CURRENT_TIMESTAMP and (s.breakingUntil is null or s.breakingUntil > CURRENT_TIMESTAMP)) or (:breaking = false and (s.status <> 'PUBLISHED' or s.breaking = false or s.breakingStartedAt is null or s.breakingStartedAt > CURRENT_TIMESTAMP or (s.breakingUntil is not null and s.breakingUntil <= CURRENT_TIMESTAMP)))) order by s.updatedAt desc")
    List<Story> findAdmin(@Param("search") String search, @Param("status") StoryStatus status, @Param("categoryId") UUID categoryId, @Param("breaking") Boolean breaking, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "tags", "media", "author"})
    Optional<Story> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"category", "tags", "media", "author"})
    Optional<Story> findWithDetailsBySlugAndStatus(String slug, StoryStatus status);

    @Query("select distinct s from Story s join fetch s.category c left join c.parent parent where s.status = 'PUBLISHED' and c.status = 'ACTIVE' and (parent is null or parent.status = 'ACTIVE') and (:categorySlug is null or c.slug = :categorySlug or parent.slug = :categorySlug) and (:tagSlug is null or exists (select filterStoryTag from Story storyFilter join storyFilter.tags filterStoryTag where storyFilter = s and filterStoryTag.slug = :tagSlug)) order by s.publishedAt desc, s.id desc")
    List<Story> findPublishedFeedInitial(@Param("categorySlug") String categorySlug, @Param("tagSlug") String tagSlug, Pageable pageable);

    @Query("select distinct s from Story s join fetch s.category c left join c.parent parent where s.status = 'PUBLISHED' and c.status = 'ACTIVE' and (parent is null or parent.status = 'ACTIVE') and (:categorySlug is null or c.slug = :categorySlug or parent.slug = :categorySlug) and (:tagSlug is null or exists (select filterStoryTag from Story storyFilter join storyFilter.tags filterStoryTag where storyFilter = s and filterStoryTag.slug = :tagSlug)) and (s.publishedAt < :cursorTime or (s.publishedAt = :cursorTime and s.id < :cursorId)) order by s.publishedAt desc, s.id desc")
    List<Story> findPublishedFeedAfter(@Param("categorySlug") String categorySlug, @Param("tagSlug") String tagSlug, @Param("cursorTime") Instant cursorTime, @Param("cursorId") UUID cursorId, Pageable pageable);

    @Query("select distinct s from Story s left join fetch s.category c left join fetch s.media where s.status = 'PUBLISHED' and s.breaking = true and s.breakingStartedAt <= :now and (s.breakingUntil is null or s.breakingUntil > :now) order by s.breakingStartedAt desc, s.id desc")
    List<Story> findActiveBreaking(@Param("now") Instant now, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("select s from Story s where s.breaking = true order by s.breakingStartedAt desc, s.id desc")
    List<Story> findBreakingForAdmin(Pageable pageable);
}
