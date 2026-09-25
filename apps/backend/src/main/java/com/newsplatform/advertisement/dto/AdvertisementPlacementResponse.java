package com.newsplatform.advertisement.dto;

import com.newsplatform.advertisement.entity.AdvertisementPlacement;

import java.util.UUID;

public record AdvertisementPlacementResponse(String type, UUID categoryId, String categoryName) {
    public static AdvertisementPlacementResponse from(AdvertisementPlacement placement) {
        return new AdvertisementPlacementResponse(placement.getPlacementType().name(),
                placement.getCategory() == null ? null : placement.getCategory().getId(),
                placement.getCategory() == null ? null : placement.getCategory().getName());
    }
}
