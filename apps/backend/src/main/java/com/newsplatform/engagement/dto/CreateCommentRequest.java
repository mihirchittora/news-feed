package com.newsplatform.engagement.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateCommentRequest(@NotBlank(message = "Comment body is required") String body, UUID parentCommentId) {
    public CreateCommentRequest(String body) {
        this(body, null);
    }
}
