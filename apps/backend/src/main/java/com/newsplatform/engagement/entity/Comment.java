package com.newsplatform.engagement.entity;

import com.newsplatform.story.entity.Story;
import com.newsplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "moderated_at")
    private Instant moderatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by")
    private User moderatedBy;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    protected Comment() {
    }

    public Comment(Story story, User user, String body) {
        this(story, user, body, null);
    }

    public Comment(Story story, User user, String body, Comment parentComment) {
        this.story = story;
        this.user = user;
        this.body = body;
        this.parentComment = parentComment;
        this.status = CommentStatus.VISIBLE;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Story getStory() { return story; }
    public User getUser() { return user; }
    public Comment getParentComment() { return parentComment; }
    public String getBody() { return body; }
    public CommentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public Instant getModeratedAt() { return moderatedAt; }
    public User getModeratedBy() { return moderatedBy; }
    public String getModerationReason() { return moderationReason; }

    public void updateBody(String body) {
        this.body = body;
    }

    public void hide(User moderator, String reason) {
        status = CommentStatus.HIDDEN;
        deletedAt = null;
        moderatedBy = moderator;
        moderatedAt = Instant.now();
        moderationReason = reason;
    }

    public void restore(User moderator) {
        status = CommentStatus.VISIBLE;
        deletedAt = null;
        moderatedBy = moderator;
        moderatedAt = Instant.now();
    }

    public void deleteByOwner() {
        status = CommentStatus.DELETED;
        deletedAt = Instant.now();
    }

    public void deleteByModerator(User moderator) {
        status = CommentStatus.DELETED;
        deletedAt = Instant.now();
        moderatedBy = moderator;
        moderatedAt = Instant.now();
    }
}
