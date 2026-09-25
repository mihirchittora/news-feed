package com.newsplatform.story.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "story_media")
public class StoryMedia {
    @Id @UuidGenerator @Column(nullable = false, updatable = false) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "story_id") private Story story;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StoryMediaType type;
    @Column(name = "storage_key", nullable = false, length = 300) private String storageKey;
    @Column(nullable = false, length = 500) private String url;
    @Column(name = "thumbnail_url", length = 500) private String thumbnailUrl;
    @Column(name = "mime_type", nullable = false, length = 120) private String mimeType;
    @Column(name = "file_size", nullable = false) private long fileSize;
    private Integer width;
    private Integer height;
    @Column(name = "duration_seconds") private Double durationSeconds;
    @Column(name = "sort_order", nullable = false) private int sortOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected StoryMedia() { }
    public StoryMedia(StoryMediaType type, String storageKey, String url, String thumbnailUrl, String mimeType, long fileSize) {
        this.type = type; this.storageKey = storageKey; this.url = url; this.thumbnailUrl = thumbnailUrl; this.mimeType = mimeType; this.fileSize = fileSize;
    }
    @PrePersist void onCreate() { createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public Story getStory() { return story; }
    public StoryMediaType getType() { return type; }
    public String getStorageKey() { return storageKey; }
    public String getUrl() { return url; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getMimeType() { return mimeType; }
    public long getFileSize() { return fileSize; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public Double getDurationSeconds() { return durationSeconds; }
    public int getSortOrder() { return sortOrder; }
    public void setStory(Story story) { this.story = story; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
