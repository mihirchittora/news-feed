package com.newsplatform.category.dto;

import com.newsplatform.category.entity.Category;
import java.util.UUID;

public record PublicCategoryResponse(UUID id, String name, String slug, String description) {
    public static PublicCategoryResponse from(Category category) { return new PublicCategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getDescription()); }
}
