package com.newsplatform.engagement.repository;

import com.newsplatform.engagement.entity.Comment;
import com.newsplatform.engagement.entity.CommentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("select comment from Comment comment join fetch comment.user left join fetch comment.parentComment where comment.story.id = :storyId and comment.status = com.newsplatform.engagement.entity.CommentStatus.VISIBLE and (comment.createdAt > :cursorTime or (comment.createdAt = :cursorTime and comment.id > :cursorId)) order by comment.createdAt asc, comment.id asc")
    List<Comment> findVisibleAfter(@Param("storyId") UUID storyId, @Param("cursorTime") Instant cursorTime, @Param("cursorId") UUID cursorId, Pageable pageable);

    @Query("select comment from Comment comment join fetch comment.user left join fetch comment.parentComment where comment.story.id = :storyId and comment.status = com.newsplatform.engagement.entity.CommentStatus.VISIBLE order by comment.createdAt asc, comment.id asc")
    List<Comment> findVisibleInitial(@Param("storyId") UUID storyId, Pageable pageable);

    @Query("select comment.story.id as storyId, count(comment.id) as count from Comment comment where comment.story.id in :storyIds and comment.status = com.newsplatform.engagement.entity.CommentStatus.VISIBLE group by comment.story.id")
    List<StoryCountProjection> countVisibleByStoryIds(@Param("storyIds") Collection<UUID> storyIds);

    @Query("select comment from Comment comment join fetch comment.user join fetch comment.story left join fetch comment.moderatedBy where (:status is null or comment.status = :status) and (:storyId is null or comment.story.id = :storyId) and (:query = '' or lower(comment.body) like concat('%', :query, '%') or lower(comment.user.name) like concat('%', :query, '%') or lower(comment.story.title) like concat('%', :query, '%')) order by comment.createdAt desc, comment.id desc")
    List<Comment> findAdmin(@Param("status") CommentStatus status, @Param("storyId") UUID storyId, @Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "story", "story.category", "moderatedBy", "parentComment"})
    java.util.Optional<Comment> findWithModerationDetailsById(UUID id);

    interface StoryCountProjection {
        UUID getStoryId();
        long getCount();
    }
}
