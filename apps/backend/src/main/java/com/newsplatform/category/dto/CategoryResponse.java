package com.newsplatform.category.dto;

import com.newsplatform.category.entity.Category;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(UUID id, String name, String slug, String description, String status, int displayOrder, Instant createdAt, Instant updatedAt) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.getStatus().name(), category.getDisplayOrder(), category.getCreatedAt(), category.getUpdatedAt());
    }
}
