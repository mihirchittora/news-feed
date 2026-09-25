package com.newsplatform.engagement.dto;

import java.util.List;

public record AdminCommentPageResponse(List<AdminCommentResponse> items, int page, int limit, boolean hasMore) {
}
