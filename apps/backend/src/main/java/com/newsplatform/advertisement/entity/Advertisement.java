package com.newsplatform.advertisement.entity;

import com.newsplatform.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "advertisements")
public class Advertisement {
    @Id @UuidGenerator @Column(nullable = false, updatable = false) private UUID id;
    @Column(nullable = false, length = 240) private String title;
    @Column(name = "advertiser_name", nullable = false, length = 180) private String advertiserName;
    @Column(columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(name = "media_type", nullable = false, length = 20) private AdvertisementMediaType mediaType;
    @Column(name = "media_storage_key", nullable = false, length = 300) private String mediaStorageKey;
    @Column(name = "media_url", nullable = false, length = 500) private String mediaUrl;
    @Column(name = "thumbnail_url", length = 500) private String thumbnailUrl;
    @Column(name = "mime_type", nullable = false, length = 120) private String mimeType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    @Column(name = "destination_url", length = 1000) private String destinationUrl;
    @Column(name = "start_at", nullable = false) private Instant startAt;
    @Column(name = "end_at", nullable = false) private Instant endAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private AdvertisementStatus status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private User createdBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("placementType ASC")
    private List<AdvertisementPlacement> placements = new ArrayList<>();

    protected Advertisement() { }

    public Advertisement(String title, String advertiserName, String description, String destinationUrl,
                          Instant startAt, Instant endAt, User createdBy) {
        this.title = title;
        this.advertiserName = advertiserName;
        this.description = description;
        this.destinationUrl = destinationUrl;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdBy = createdBy;
        this.status = AdvertisementStatus.DRAFT;
    }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getAdvertiserName() { return advertiserName; }
    public String getDescription() { return description; }
    public AdvertisementMediaType getMediaType() { return mediaType; }
    public String getMediaStorageKey() { return mediaStorageKey; }
    public String getMediaUrl() { return mediaUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }
    public String getDestinationUrl() { return destinationUrl; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public AdvertisementStatus getStatus() { return status; }
    public User getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<AdvertisementPlacement> getPlacements() { return placements; }

    public void setMedia(AdvertisementMediaType mediaType, String mediaStorageKey, String mediaUrl,
                         String thumbnailUrl, String mimeType, long fileSize) {
        this.mediaType = mediaType;
        this.mediaStorageKey = mediaStorageKey;
        this.mediaUrl = mediaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
    }

    public void update(String title, String advertiserName, String description, String destinationUrl,
                       Instant startAt, Instant endAt) {
        this.title = title;
        this.advertiserName = advertiserName;
        this.description = description;
        this.destinationUrl = destinationUrl;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void replacePlacements(List<AdvertisementPlacement> next) {
        // Keep unchanged rows attached to the entity. Recreating an identical placement
        // during an update can make Hibernate insert before orphan removal and violate the
        // unique placement indexes.
        placements.removeIf(existing -> next.stream().noneMatch(candidate -> samePlacement(existing, candidate)));
        next.stream()
                .filter(candidate -> placements.stream().noneMatch(existing -> samePlacement(existing, candidate)))
                .forEach(candidate -> {
                    candidate.setAdvertisement(this);
                    placements.add(candidate);
                });
    }

    private boolean samePlacement(AdvertisementPlacement left, AdvertisementPlacement right) {
        UUID leftCategoryId = left.getCategory() == null ? null : left.getCategory().getId();
        UUID rightCategoryId = right.getCategory() == null ? null : right.getCategory().getId();
        return left.getPlacementType() == right.getPlacementType()
                && java.util.Objects.equals(leftCategoryId, rightCategoryId);
    }

    public void setStatus(AdvertisementStatus status) { this.status = status; }

    public AdvertisementStatus statusAt(Instant now) {
        if (status == AdvertisementStatus.DRAFT) return AdvertisementStatus.DRAFT;
        if (status == AdvertisementStatus.PAUSED || status == AdvertisementStatus.EXPIRED) {
            if (now.compareTo(endAt) >= 0) return AdvertisementStatus.EXPIRED;
            return status;
        }
        if (now.compareTo(endAt) >= 0) return AdvertisementStatus.EXPIRED;
        if (now.isBefore(startAt)) return AdvertisementStatus.SCHEDULED;
        return AdvertisementStatus.ACTIVE;
    }
}
