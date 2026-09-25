package com.newsplatform.advertisement.dto;

import com.newsplatform.advertisement.entity.Advertisement;

import java.util.UUID;

public record PublicAdvertisementResponse(UUID id, String title, String advertiserName, String description,
                                          String mediaType, String mediaUrl, String thumbnailUrl,
                                          String destinationUrl, String placementType) {
    public static PublicAdvertisementResponse from(Advertisement advertisement, String placementType) {
        return new PublicAdvertisementResponse(advertisement.getId(), advertisement.getTitle(), advertisement.getAdvertiserName(),
                advertisement.getDescription(), advertisement.getMediaType().name(), advertisement.getMediaUrl(),
                advertisement.getThumbnailUrl(), advertisement.getDestinationUrl(), placementType);
    }
}
