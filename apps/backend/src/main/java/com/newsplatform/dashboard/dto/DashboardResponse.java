package com.newsplatform.dashboard.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        Period period,
        Content content,
        Engagement engagement,
        Moderation moderation,
        Users users,
        Staff staff,
        Newspaper newspaper,
        Advertising advertising,
        Attention attention,
        List<TopStory> topStories,
        List<CategoryBreakdown> categoryBreakdown,
        List<PublishingTrendPoint> publishingTrend,
        List<RecentActivity> recentActivity
) {
    public record Period(
            DashboardPeriodType type,
            Instant from,
            Instant to,
            String timezone
    ) { }

    public record Content(
            long publishedStories,
            long draftStories,
            long unpublishedStories,
            Long activeBreakingNews
    ) { }

    public record Engagement(
            long likes,
            long comments,
            long commentLikes
    ) { }

    public record Moderation(
            long hiddenComments,
            long moderatedComments
    ) { }

    public record Users(
            long totalRegisteredUsers,
            long newUsers
    ) { }

    public record Staff(
            long activeStaff,
            long disabledStaff,
            long pendingSetupStaff
    ) { }

    public record Newspaper(
            boolean todayPublished,
            long publishedEditionCount,
            long draftEditionCount,
            long unpublishedEditionCount,
            List<Edition> editions
    ) { }

    public record Edition(
            UUID editionId,
            String editionName,
            String title,
            String status,
            Instant publishedAt
    ) { }

    public record Advertising(
            long active,
            long scheduled,
            long paused,
            long expired,
            long expiringSoon,
            List<ExpiringAdvertisement> expiringAds
    ) { }

    public record ExpiringAdvertisement(
            UUID advertisementId,
            String advertiserName,
            String title,
            String placement,
            Instant endAt
    ) { }

    public record Attention(List<AttentionItem> items) { }

    public record AttentionItem(
            String key,
            String label,
            String detail,
            String severity,
            String href
    ) { }

    public record TopStory(
            UUID storyId,
            String title,
            String category,
            Instant publishedAt,
            long likeCount,
            long commentCount,
            long commentLikeCount,
            long engagementCount
    ) { }

    public record CategoryBreakdown(String category, long published) { }

    public record PublishingTrendPoint(LocalDate date, long published) { }

    public record RecentActivity(
            UUID activityId,
            String action,
            String targetType,
            String targetLabel,
            String actorName,
            Instant createdAt
    ) { }
}
