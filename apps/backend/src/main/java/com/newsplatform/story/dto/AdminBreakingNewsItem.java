package com.newsplatform.story.dto;

import com.newsplatform.story.entity.Story;

import java.time.Instant;
import java.util.UUID;

public record AdminBreakingNewsItem(UUID id, String slug, String title, String status, String categoryName,
                                    Instant breakingStartedAt, Instant breakingUntil, boolean active) {
    public static AdminBreakingNewsItem from(Story story, Instant now) {
        return new AdminBreakingNewsItem(
                story.getId(), story.getSlug(), story.getTitle(), story.getStatus().name(),
                story.getCategory() == null ? null : story.getCategory().getName(),
                story.getBreakingStartedAt(), story.getBreakingUntil(), story.isActivelyBreaking(now)
        );
    }
}
