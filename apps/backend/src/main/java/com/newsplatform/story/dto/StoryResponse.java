package com.newsplatform.story.dto;

import com.newsplatform.category.dto.PublicCategoryResponse;
import com.newsplatform.story.entity.Story;
import com.newsplatform.tag.dto.PublicTagResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StoryResponse(UUID id, String title, String slug, String summary, String body, PublicCategoryResponse category, String authorName, List<PublicTagResponse> tags, List<MediaResponse> media, Instant publishedAt) {
    public static StoryResponse from(Story story) {
        return new StoryResponse(story.getId(), story.getTitle(), story.getSlug(), story.getSummary(), story.getBody(), story.getCategory() == null ? null : PublicCategoryResponse.from(story.getCategory()), story.getAuthor().getName(), story.getTags().stream().sorted(java.util.Comparator.comparing(tag -> tag.getName().toLowerCase())).map(PublicTagResponse::from).toList(), story.getMedia().stream().map(MediaResponse::from).toList(), story.getPublishedAt());
    }
}
