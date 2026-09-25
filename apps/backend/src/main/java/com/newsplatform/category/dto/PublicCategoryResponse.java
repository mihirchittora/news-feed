package com.newsplatform.category.dto;

import com.newsplatform.category.entity.Category;
import java.util.UUID;
import java.util.List;

public record PublicCategoryResponse(UUID id, String name, String slug, String description, List<PublicCategoryResponse> children) {
    public static PublicCategoryResponse from(Category category) { return from(category, List.of()); }
    public static PublicCategoryResponse from(Category category, List<PublicCategoryResponse> children) {
        return new PublicCategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription(), children);
    }
}
