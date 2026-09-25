package com.newsplatform.advertisement.dto;

import java.util.List;

public record AdminAdvertisementListResponse(List<AdminAdvertisementResponse> items, int page, int limit, boolean hasMore) { }
