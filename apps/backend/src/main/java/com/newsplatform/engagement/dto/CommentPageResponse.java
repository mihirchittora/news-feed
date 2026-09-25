package com.newsplatform.engagement.dto;

import java.util.List;

public record CommentPageResponse(List<CommentResponse> items, String nextCursor, boolean hasMore) {
}
