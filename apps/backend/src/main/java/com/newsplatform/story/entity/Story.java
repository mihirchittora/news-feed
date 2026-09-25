package com.newsplatform.story.entity;

import com.newsplatform.category.entity.Category;
import com.newsplatform.tag.entity.Tag;
import com.newsplatform.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "stories")
public class Story {
    @Id @UuidGenerator @Column(nullable = false, updatable = false) private UUID id;
    @Column(nullable = false, length = 240) private String title;
    @Column(nullable = false, length = 260) private String slug;
    @Column(length = 600) private String summary;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") private Category category;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private StoryStatus status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "author_id", nullable = false) private User author;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "is_breaking", nullable = false) private boolean breaking;
    @Column(name = "breaking_started_at") private Instant breakingStartedAt;
    @Column(name = "breaking_until") private Instant breakingUntil;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "story_tags", joinColumns = @JoinColumn(name = "story_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, createdAt ASC") private List<StoryMedia> media = new ArrayList<>();

    protected Story() { }
    public Story(String title, String slug, String summary, String body, Category category, User author) {
        this.title = title; this.slug = slug; this.summary = summary; this.body = body; this.category = category; this.author = author; this.status = StoryStatus.DRAFT;
    }
    @PrePersist void onCreate() { Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getSummary() { return summary; }
    public String getBody() { return body; }
    public Category getCategory() { return category; }
    public StoryStatus getStatus() { return status; }
    public User getAuthor() { return author; }
    public Instant getPublishedAt() { return publishedAt; }
    public boolean isBreaking() { return breaking; }
    public Instant getBreakingStartedAt() { return breakingStartedAt; }
    public Instant getBreakingUntil() { return breakingUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<Tag> getTags() { return tags; }
    public List<StoryMedia> getMedia() { return media; }
    public void update(String title, String slug, String summary, String body, Category category, Set<Tag> tags, List<StoryMedia> media) {
        this.title = title; this.slug = slug; this.summary = summary; this.body = body; this.category = category;
        this.tags.clear(); this.tags.addAll(tags); this.media.clear();
        for (int i = 0; i < media.size(); i++) { StoryMedia item = media.get(i); item.setStory(this); item.setSortOrder(i); this.media.add(item); }
    }
    public void publish() { this.status = StoryStatus.PUBLISHED; if (publishedAt == null) publishedAt = Instant.now(); }
    public void unpublish() { this.status = StoryStatus.UNPUBLISHED; clearBreaking(); }
    public void enableBreaking(Instant startedAt, Instant until) { this.breaking = true; this.breakingStartedAt = startedAt; this.breakingUntil = until; }
    public void updateBreakingUntil(Instant until) { this.breakingUntil = until; }
    public void clearBreaking() { this.breaking = false; this.breakingStartedAt = null; this.breakingUntil = null; }
    public boolean isActivelyBreaking(Instant now) {
        return status == StoryStatus.PUBLISHED
                && breaking
                && breakingStartedAt != null
                && !breakingStartedAt.isAfter(now)
                && (breakingUntil == null || breakingUntil.isAfter(now));
    }
}
