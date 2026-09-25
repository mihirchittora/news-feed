package com.newsplatform.engagement.entity;

import com.newsplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "comment_likes")
public class CommentLike {
    @EmbeddedId
    private CommentLikeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("commentId")
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CommentLike() {
    }

    public CommentLike(User user, Comment comment) {
        this.id = new CommentLikeId(user.getId(), comment.getId());
        this.user = user;
        this.comment = comment;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public CommentLikeId getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
}
