package com.newsplatform.engagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class StoryLikeId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "story_id", nullable = false)
    private UUID storyId;

    protected StoryLikeId() {
    }

    public StoryLikeId(UUID userId, UUID storyId) {
        this.userId = userId;
        this.storyId = storyId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getStoryId() {
        return storyId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof StoryLikeId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(storyId, that.storyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, storyId);
    }
}
