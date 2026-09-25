package com.newsplatform.story.dto;

import com.newsplatform.category.dto.PublicCategoryResponse;
import com.newsplatform.story.entity.Story;
import com.newsplatform.tag.dto.PublicTagResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StorySummaryResponse(UUID id, String title, String slug, String summary, PublicCategoryResponse category, List<PublicTagResponse> tags, List<MediaResponse> media, Instant publishedAt, String authorName) {
    public static StorySummaryResponse from(Story story) {
        return new StorySummaryResponse(story.getId(), story.getTitle(), story.getSlug(), story.getSummary(), story.getCategory() == null ? null : PublicCategoryResponse.from(story.getCategory()), story.getTags().stream().sorted(java.util.Comparator.comparing(tag -> tag.getName().toLowerCase())).map(PublicTagResponse::from).toList(), story.getMedia().stream().map(MediaResponse::from).toList(), story.getPublishedAt(), story.getAuthor().getName());
    }
}
