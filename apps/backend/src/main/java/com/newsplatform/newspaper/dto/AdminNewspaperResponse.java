package com.newsplatform.newspaper.dto;

import com.newsplatform.newspaper.entity.NewspaperEdition;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AdminNewspaperResponse(UUID id, String title, String edition, LocalDate editionDate, String status,
                                     Instant publishedAt, boolean hasDocument, boolean hasCover, String coverImageUrl,
                                     UUID uploadedBy, Instant createdAt, Instant updatedAt) {
    public static AdminNewspaperResponse from(NewspaperEdition edition, String publicUrl) {
        String cover = edition.getCoverImageStorageKey() == null ? null : publicUrl + "/api/v1/newspapers/" + edition.getId() + "/cover";
        return new AdminNewspaperResponse(edition.getId(), edition.getTitle(), edition.getEdition(), edition.getEditionDate(), edition.getStatus().name(),
                edition.getPublishedAt(), edition.getPdfStorageKey() != null, edition.getCoverImageStorageKey() != null, cover,
                edition.getUploadedBy().getId(), edition.getCreatedAt(), edition.getUpdatedAt());
    }
}
