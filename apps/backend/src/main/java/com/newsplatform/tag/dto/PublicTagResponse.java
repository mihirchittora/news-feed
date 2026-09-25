package com.newsplatform.tag.dto;

import com.newsplatform.tag.entity.Tag;
import java.util.UUID;

public record PublicTagResponse(UUID id, String name, String slug) {
    public static PublicTagResponse from(Tag tag) { return new PublicTagResponse(tag.getId(), tag.getName(), tag.getSlug()); }
}
