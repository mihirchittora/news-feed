package com.newsplatform.newspaper.entity;

import com.newsplatform.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "newspaper_editions")
public class NewspaperEdition {
    @Id @UuidGenerator @Column(nullable = false, updatable = false) private UUID id;
    @Column(nullable = false, length = 240) private String title;
    @Column(nullable = false, length = 120) private String edition;
    @Column(name = "edition_date", nullable = false) private LocalDate editionDate;
    @Column(name = "pdf_storage_key", length = 500) private String pdfStorageKey;
    @Column(name = "cover_image_storage_key", length = 500) private String coverImageStorageKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private NewspaperStatus status;
    @Column(name = "published_at") private Instant publishedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "uploaded_by", nullable = false) private User uploadedBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected NewspaperEdition() { }

    public NewspaperEdition(String title, String edition, LocalDate editionDate, User uploadedBy) {
        this.title = title;
        this.edition = edition;
        this.editionDate = editionDate;
        this.uploadedBy = uploadedBy;
        this.status = NewspaperStatus.DRAFT;
    }

    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getEdition() { return edition; }
    public LocalDate getEditionDate() { return editionDate; }
    public String getPdfStorageKey() { return pdfStorageKey; }
    public String getCoverImageStorageKey() { return coverImageStorageKey; }
    public NewspaperStatus getStatus() { return status; }
    public Instant getPublishedAt() { return publishedAt; }
    public User getUploadedBy() { return uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String title, String edition, LocalDate editionDate) {
        this.title = title; this.edition = edition; this.editionDate = editionDate;
    }
    public void setPdfStorageKey(String pdfStorageKey) { this.pdfStorageKey = pdfStorageKey; }
    public void setCoverImageStorageKey(String coverImageStorageKey) { this.coverImageStorageKey = coverImageStorageKey; }
    public void publish() { this.status = NewspaperStatus.PUBLISHED; this.publishedAt = Instant.now(); }
    public void unpublish() { this.status = NewspaperStatus.UNPUBLISHED; }
}
