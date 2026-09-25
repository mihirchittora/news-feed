package com.newsplatform.engagement.dto;

import jakarta.validation.constraints.Size;

public record ModerationRequest(@Size(max = 500, message = "Reason must be 500 characters or fewer") String reason) {
}
