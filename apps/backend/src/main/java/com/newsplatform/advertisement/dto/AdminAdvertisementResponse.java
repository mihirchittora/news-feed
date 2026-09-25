package com.newsplatform.advertisement.dto;

import com.newsplatform.advertisement.entity.Advertisement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminAdvertisementResponse(UUID id, String title, String advertiserName, String description,
                                         String mediaType, String mediaUrl, String thumbnailUrl, String mimeType,
                                         long fileSize, String destinationUrl, Instant startAt, Instant endAt,
                                         String status, List<AdvertisementPlacementResponse> placements,
                                         UUID createdBy, Instant createdAt, Instant updatedAt) {
    public static AdminAdvertisementResponse from(Advertisement advertisement, Instant now) {
        return new AdminAdvertisementResponse(advertisement.getId(), advertisement.getTitle(), advertisement.getAdvertiserName(),
                advertisement.getDescription(), advertisement.getMediaType().name(), advertisement.getMediaUrl(),
                advertisement.getThumbnailUrl(), advertisement.getMimeType(), advertisement.getFileSize(),
                advertisement.getDestinationUrl(), advertisement.getStartAt(), advertisement.getEndAt(),
                advertisement.statusAt(now).name(), advertisement.getPlacements().stream().map(AdvertisementPlacementResponse::from).toList(),
                advertisement.getCreatedBy().getId(), advertisement.getCreatedAt(), advertisement.getUpdatedAt());
    }
}
