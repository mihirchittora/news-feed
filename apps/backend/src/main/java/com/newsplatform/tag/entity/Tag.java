package com.newsplatform.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tags")
public class Tag {
    @Id @UuidGenerator @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(name = "normalized_name", nullable = false, length = 120) private String normalizedName;
    @Column(nullable = false, length = 140) private String slug;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Tag() { }
    public Tag(String name, String normalizedName, String slug) { this.name = name; this.normalizedName = normalizedName; this.slug = slug; }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getNormalizedName() { return normalizedName; }
    public String getSlug() { return slug; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void update(String name, String normalizedName, String slug) { this.name = name; this.normalizedName = normalizedName; this.slug = slug; }
}
