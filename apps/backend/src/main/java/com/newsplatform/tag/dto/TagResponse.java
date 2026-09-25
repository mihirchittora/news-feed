package com.newsplatform.tag.dto;

import com.newsplatform.tag.entity.Tag;
import java.time.Instant;
import java.util.UUID;

public record TagResponse(UUID id, String name, String slug, Instant createdAt, Instant updatedAt) {
    public static TagResponse from(Tag tag) { return new TagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.getCreatedAt(), tag.getUpdatedAt()); }
}
