package com.newsplatform.story.dto;

import com.newsplatform.story.entity.StoryMedia;
import java.util.UUID;

public record MediaResponse(UUID id, String type, String url, String thumbnailUrl, String mimeType, long fileSize, Integer width, Integer height, Double durationSeconds, int sortOrder) {
    public static MediaResponse from(StoryMedia media) { return new MediaResponse(media.getId(), media.getType().name(), media.getUrl(), media.getThumbnailUrl(), media.getMimeType(), media.getFileSize(), media.getWidth(), media.getHeight(), media.getDurationSeconds(), media.getSortOrder()); }
}
