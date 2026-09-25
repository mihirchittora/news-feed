package com.newsplatform.engagement.entity;

import com.newsplatform.story.entity.Story;
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
@Table(name = "story_likes")
public class StoryLike {
    @EmbeddedId
    private StoryLikeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("storyId")
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StoryLike() {
    }

    public StoryLike(User user, Story story) {
        this.id = new StoryLikeId(user.getId(), story.getId());
        this.user = user;
        this.story = story;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public StoryLikeId getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
