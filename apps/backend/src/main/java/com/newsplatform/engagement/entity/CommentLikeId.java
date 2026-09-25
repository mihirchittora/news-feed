package com.newsplatform.engagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CommentLikeId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    protected CommentLikeId() {
    }

    public CommentLikeId(UUID userId, UUID commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }

    public UUID getUserId() { return userId; }
    public UUID getCommentId() { return commentId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CommentLikeId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(commentId, that.commentId);
    }

    @Override
    public int hashCode() { return Objects.hash(userId, commentId); }
}
