package com.newsplatform.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id @UuidGenerator @Column(nullable = false, updatable = false)
    private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, length = 140) private String slug;
    @Column(length = 500) private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private CategoryStatus status;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected Category() { }
    public Category(String name, String slug, String description, CategoryStatus status, int displayOrder) {
        this.name = name; this.slug = slug; this.description = description; this.status = status; this.displayOrder = displayOrder;
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public CategoryStatus getStatus() { return status; }
    public int getDisplayOrder() { return displayOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void update(String name, String slug, String description, CategoryStatus status, int displayOrder) {
        this.name = name; this.slug = slug; this.description = description; this.status = status; this.displayOrder = displayOrder;
    }
}
