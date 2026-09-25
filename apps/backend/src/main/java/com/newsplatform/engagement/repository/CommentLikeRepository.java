package com.newsplatform.engagement.repository;

import com.newsplatform.engagement.entity.CommentLike;
import com.newsplatform.engagement.entity.CommentLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLikeId> {
    @Modifying
    @Query(value = "INSERT INTO comment_likes (user_id, comment_id, created_at) VALUES (:userId, :commentId, now()) ON CONFLICT (user_id, comment_id) DO NOTHING", nativeQuery = true)
    int insertIfAbsent(@Param("userId") UUID userId, @Param("commentId") UUID commentId);

    @Modifying
    @Query("delete from CommentLike like where like.id.userId = :userId and like.id.commentId = :commentId")
    int deleteForUserAndComment(@Param("userId") UUID userId, @Param("commentId") UUID commentId);

    @Query("select like.id.commentId as commentId, count(like.id.commentId) as count from CommentLike like where like.id.commentId in :commentIds group by like.id.commentId")
    List<CommentCountProjection> countByCommentIds(@Param("commentIds") Collection<UUID> commentIds);

    @Query("select like.id.commentId from CommentLike like where like.id.userId = :userId and like.id.commentId in :commentIds")
    Set<UUID> findLikedCommentIds(@Param("userId") UUID userId, @Param("commentIds") Collection<UUID> commentIds);

    interface CommentCountProjection {
        UUID getCommentId();
        long getCount();
    }
}
