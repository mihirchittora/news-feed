package com.newsplatform.engagement.dto;

public record StoryEngagementResponse(long likeCount, long commentCount, boolean likedByCurrentUser) {
}
