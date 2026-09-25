package com.newsplatform.story.dto;

import com.newsplatform.story.entity.Story;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminStoryResponse(UUID id, String title, String slug, String summary, String body, String status, UUID categoryId, String categoryName, List<UUID> tagIds, List<MediaResponse> media, UUID authorId, String authorName, Instant publishedAt, Instant createdAt, Instant updatedAt, boolean isBreaking, Instant breakingStartedAt, Instant breakingUntil, boolean breakingActive) {
    public static AdminStoryResponse from(Story story) {
        return new AdminStoryResponse(story.getId(), story.getTitle(), story.getSlug(), story.getSummary(), story.getBody(), story.getStatus().name(), story.getCategory() == null ? null : story.getCategory().getId(), story.getCategory() == null ? null : story.getCategory().getName(), story.getTags().stream().map(tag -> tag.getId()).toList(), story.getMedia().stream().map(MediaResponse::from).toList(), story.getAuthor().getId(), story.getAuthor().getName(), story.getPublishedAt(), story.getCreatedAt(), story.getUpdatedAt(), story.isBreaking(), story.getBreakingStartedAt(), story.getBreakingUntil(), story.isActivelyBreaking(Instant.now()));
    }
}
