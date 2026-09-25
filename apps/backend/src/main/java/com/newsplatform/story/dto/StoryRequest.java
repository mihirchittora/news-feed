package com.newsplatform.story.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record StoryRequest(@NotBlank @Size(max = 240) String title, @Size(max = 600) String summary,
                           String body, UUID categoryId, List<UUID> tagIds, List<UUID> mediaIds) { }
