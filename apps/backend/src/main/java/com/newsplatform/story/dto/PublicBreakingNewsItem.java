package com.newsplatform.story.dto;

import com.newsplatform.category.dto.PublicCategoryResponse;
import com.newsplatform.story.entity.Story;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicBreakingNewsItem(UUID id, String slug, String title, String summary,
                                     PublicCategoryResponse category, List<MediaResponse> media,
                                     Instant publishedAt, Instant breakingStartedAt, Instant breakingUntil) {
    public static PublicBreakingNewsItem from(Story story) {
        return new PublicBreakingNewsItem(
                story.getId(), story.getSlug(), story.getTitle(), story.getSummary(),
                story.getCategory() == null ? null : PublicCategoryResponse.from(story.getCategory()),
                story.getMedia().stream().map(MediaResponse::from).toList(), story.getPublishedAt(),
                story.getBreakingStartedAt(), story.getBreakingUntil()
        );
    }
}
