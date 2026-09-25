package com.newsplatform.newspaper.dto;

import com.newsplatform.newspaper.entity.NewspaperEdition;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PublicNewspaperResponse(UUID id, String title, String edition, LocalDate editionDate,
                                      String coverImageUrl, Instant publishedAt) {
    public static PublicNewspaperResponse from(NewspaperEdition edition, String publicUrl) {
        String cover = edition.getCoverImageStorageKey() == null ? null : publicUrl + "/api/v1/newspapers/" + edition.getId() + "/cover";
        return new PublicNewspaperResponse(edition.getId(), edition.getTitle(), edition.getEdition(), edition.getEditionDate(), cover, edition.getPublishedAt());
    }
}
