package com.newsplatform.story.dto;

import java.util.List;

public record FeedResponse(List<StorySummaryResponse> items, String nextCursor, boolean hasMore) { }
