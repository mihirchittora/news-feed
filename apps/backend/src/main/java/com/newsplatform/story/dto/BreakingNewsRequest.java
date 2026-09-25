package com.newsplatform.story.dto;

import java.time.Instant;

public record BreakingNewsRequest(Instant breakingUntil, Boolean untilManuallyRemoved) {
}
