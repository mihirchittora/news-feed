package com.newsplatform.story.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.rbac.service.AuditService;
import com.newsplatform.story.dto.AdminBreakingNewsItem;
import com.newsplatform.story.dto.AdminBreakingNewsListResponse;
import com.newsplatform.story.dto.AdminStoryResponse;
import com.newsplatform.story.dto.BreakingNewsRequest;
import com.newsplatform.story.dto.PublicBreakingNewsItem;
import com.newsplatform.story.dto.PublicBreakingNewsListResponse;
import com.newsplatform.story.entity.Story;
import com.newsplatform.story.entity.StoryStatus;
import com.newsplatform.story.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BreakingNewsService {
    private final StoryRepository storyRepository;
    private final AuditService auditService;
    private final Clock clock;
    private final long defaultDurationMinutes;

    public BreakingNewsService(StoryRepository storyRepository, AuditService auditService, Clock clock,
                              @Value("${app.breaking-news.default-duration-minutes:120}") long defaultDurationMinutes) {
        if (defaultDurationMinutes <= 0) throw new IllegalArgumentException("Breaking News default duration must be positive");
        this.storyRepository = storyRepository;
        this.auditService = auditService;
        this.clock = clock;
        this.defaultDurationMinutes = defaultDurationMinutes;
    }

    @Transactional(readOnly = true)
    public PublicBreakingNewsListResponse listPublic(int requestedLimit) {
        Instant now = clock.instant();
        int limit = Math.min(Math.max(requestedLimit, 1), 20);
        List<PublicBreakingNewsItem> items = storyRepository.findActiveBreaking(now, PageRequest.of(0, limit))
                .stream().map(PublicBreakingNewsItem::from).toList();
        return new PublicBreakingNewsListResponse(items);
    }

    @Transactional(readOnly = true)
    public AdminBreakingNewsListResponse listAdmin(String requestedStatus, int requestedLimit) {
        String status = requestedStatus == null || requestedStatus.isBlank() ? "ALL" : requestedStatus.trim().toUpperCase();
        if (!List.of("ALL", "ACTIVE", "EXPIRED").contains(status)) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "INVALID_BREAKING_STATUS", "Breaking News status must be ACTIVE, EXPIRED, or ALL");
        }
        Instant now = clock.instant();
        int limit = Math.min(Math.max(requestedLimit, 1), 100);
        List<AdminBreakingNewsItem> items = storyRepository.findBreakingForAdmin(PageRequest.of(0, limit)).stream()
                .map(story -> AdminBreakingNewsItem.from(story, now))
                .filter(item -> status.equals("ALL") || (status.equals("ACTIVE") == item.active()))
                .toList();
        return new AdminBreakingNewsListResponse(items);
    }

    @Transactional
    public AdminStoryResponse enable(UUID storyId, BreakingNewsRequest request, UUID actorId) {
        Story story = requireStory(storyId);
        if (story.getStatus() != StoryStatus.PUBLISHED) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "STORY_NOT_PUBLISHED", "Only published stories can be marked as Breaking News");
        }

        Instant now = clock.instant();
        boolean wasActive = story.isActivelyBreaking(now);
        Instant previousUntil = story.getBreakingUntil();
        boolean manuallyRemoved = request != null && Boolean.TRUE.equals(request.untilManuallyRemoved());
        Instant suppliedUntil = request == null ? null : request.breakingUntil();
        if (suppliedUntil != null) validateExpiry(suppliedUntil, now);

        if (!story.isBreaking() || !wasActive) {
            story.enableBreaking(now, manuallyRemoved ? null : suppliedUntil != null ? suppliedUntil : now.plus(defaultDurationMinutes, ChronoUnit.MINUTES));
        } else if (manuallyRemoved) {
            story.updateBreakingUntil(null);
        } else if (suppliedUntil != null) {
            story.updateBreakingUntil(suppliedUntil);
        }

        String action = wasActive ? "BREAKING_NEWS_UPDATED" : "BREAKING_NEWS_ENABLED";
        auditService.record(actorId, action, "STORY", storyId, breakingMetadata(previousUntil, story.getBreakingUntil()));
        return AdminStoryResponse.from(story);
    }

    @Transactional
    public AdminStoryResponse disable(UUID storyId, UUID actorId) {
        Story story = requireStory(storyId);
        if (story.isBreaking()) {
            story.clearBreaking();
            auditService.record(actorId, "BREAKING_NEWS_DISABLED", "STORY", storyId, Map.of());
        }
        return AdminStoryResponse.from(story);
    }

    private Story requireStory(UUID storyId) {
        return storyRepository.findWithDetailsById(storyId)
                .orElseThrow(() -> new RbacException(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", "Story not found"));
    }

    private void validateExpiry(Instant expiry, Instant now) {
        if (!expiry.isAfter(now)) {
            throw new RbacException(HttpStatus.BAD_REQUEST, "BREAKING_EXPIRY_INVALID", "Breaking News expiry must be in the future");
        }
    }

    private Map<String, String> breakingMetadata(Instant previousUntil, Instant newUntil) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (previousUntil != null) metadata.put("previousBreakingUntil", previousUntil.toString());
        if (newUntil != null) metadata.put("breakingUntil", newUntil.toString());
        return metadata;
    }
}
