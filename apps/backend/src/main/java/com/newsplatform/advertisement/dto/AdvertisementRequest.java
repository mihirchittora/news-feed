package com.newsplatform.advertisement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record AdvertisementRequest(
        @NotBlank @Size(max = 240) String title,
        @NotBlank @Size(max = 180) String advertiserName,
        @Size(max = 5000) String description,
        @Size(max = 1000) String destinationUrl,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @NotNull @Valid AdvertisementPlacementRequest placement,
        UUID mediaId
) { }
