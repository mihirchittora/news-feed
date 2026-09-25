package com.newsplatform.advertisement.dto;

import java.util.List;
import java.util.UUID;

public record AdvertisementPlacementRequest(String type, List<UUID> categoryIds) { }
