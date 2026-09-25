package com.newsplatform.engagement.dto;

import com.newsplatform.engagement.entity.Comment;

import java.time.Instant;
import java.util.UUID;

public record AdminCommentResponse(
        UUID id,
        String body,
        AuthorResponse author,
        StoryContext story,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        Instant moderatedAt,
        ModeratorResponse moderatedBy,
        String moderationReason
) {
    public static AdminCommentResponse from(Comment comment) {
        return new AdminCommentResponse(
                comment.getId(),
                comment.getBody(),
                new AuthorResponse(comment.getUser().getId(), comment.getUser().getName()),
                new StoryContext(comment.getStory().getId(), comment.getStory().getTitle(), comment.getStory().getSlug()),
                comment.getStatus().name(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getDeletedAt(),
                comment.getModeratedAt(),
                comment.getModeratedBy() == null ? null : new ModeratorResponse(comment.getModeratedBy().getId(), comment.getModeratedBy().getName()),
                comment.getModerationReason()
        );
    }

    public record AuthorResponse(UUID id, String name) {
    }

    public record ModeratorResponse(UUID id, String name) {
    }

    public record StoryContext(UUID id, String title, String slug) {
    }
}
