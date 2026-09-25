package com.newsplatform.engagement.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCommentRequest(@NotBlank(message = "Comment body is required") String body) {
}
