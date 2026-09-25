package com.newsplatform.engagement.dto;

import com.newsplatform.engagement.entity.Comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String body,
        AuthorResponse author,
        UUID parentCommentId,
        Instant createdAt,
        Instant updatedAt,
        boolean edited,
        long likeCount,
        boolean likedByCurrentUser,
        boolean ownedByCurrentUser
) {
    public static CommentResponse from(Comment comment, UUID currentUserId) {
        return from(comment, currentUserId, 0, false);
    }

    public static CommentResponse from(Comment comment, UUID currentUserId, long likeCount, boolean likedByCurrentUser) {
        return new CommentResponse(
                comment.getId(),
                comment.getBody(),
                new AuthorResponse(comment.getUser().getId(), comment.getUser().getName()),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getUpdatedAt() != null && !comment.getUpdatedAt().equals(comment.getCreatedAt()),
                likeCount,
                likedByCurrentUser,
                currentUserId != null && currentUserId.equals(comment.getUser().getId())
        );
    }

    public record AuthorResponse(UUID id, String name) {
    }
}
