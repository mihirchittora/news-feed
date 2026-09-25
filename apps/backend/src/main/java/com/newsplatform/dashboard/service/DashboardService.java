package com.newsplatform.dashboard.service;

import com.newsplatform.common.error.RbacException;
import com.newsplatform.dashboard.dto.DashboardPeriodType;
import com.newsplatform.dashboard.dto.DashboardResponse;
import com.newsplatform.dashboard.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private static final Set<String> ADMIN_PERMISSIONS = Set.of(
            "STORY_VIEW_ADMIN", "STORY_CREATE", "STORY_EDIT", "STORY_DELETE", "STORY_PUBLISH",
            "BREAKING_NEWS_MANAGE", "CATEGORY_MANAGE", "TAG_MANAGE", "COMMENT_MODERATE",
            "NEWSPAPER_VIEW_ADMIN", "NEWSPAPER_UPLOAD", "NEWSPAPER_EDIT", "NEWSPAPER_PUBLISH", "NEWSPAPER_DELETE",
            "AD_VIEW_ADMIN", "AD_CREATE", "AD_EDIT", "AD_PUBLISH", "AD_PAUSE", "AD_DELETE",
            "STAFF_VIEW", "STAFF_CREATE", "STAFF_EDIT", "STAFF_DISABLE", "STAFF_ROLE_ASSIGN",
            "ROLE_VIEW", "ROLE_CREATE", "ROLE_EDIT", "ROLE_DELETE", "ROLE_PERMISSION_ASSIGN"
    );

    private final DashboardRepository repository;
    private final DashboardPeriodCalculator periodCalculator;
    private final long expiringSoonHours;

    public DashboardService(
            DashboardRepository repository,
            DashboardPeriodCalculator periodCalculator,
            @Value("${app.ads.expiring-soon-hours:24}") long expiringSoonHours
    ) {
        this.repository = repository;
        this.periodCalculator = periodCalculator;
        this.expiringSoonHours = Math.max(1, expiringSoonHours);
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(DashboardPeriodType requestedPeriod, Authentication authentication) {
        Set<String> permissions = permissions(authentication);
        if (!isStaff(permissions)) {
            throw new RbacException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to the administrative dashboard");
        }

        DashboardPeriodCalculator.PeriodWindow window = periodCalculator.calculate(
                requestedPeriod == null ? DashboardPeriodType.TODAY : requestedPeriod
        );
        Instant now = window.to();
        boolean canContent = can(permissions, "STORY_VIEW_ADMIN");
        boolean canBreaking = can(permissions, "BREAKING_NEWS_MANAGE");
        boolean canModerate = can(permissions, "COMMENT_MODERATE");
        boolean canNewspaper = can(permissions, "NEWSPAPER_VIEW_ADMIN");
        boolean canAds = can(permissions, "AD_VIEW_ADMIN");
        boolean canStaff = can(permissions, "STAFF_VIEW");

        DashboardRepository.ContentCounts contentCounts = canContent ? repository.content(window.from(), window.to(), now) : null;
        DashboardResponse.Content content = contentCounts == null ? null : new DashboardResponse.Content(
                contentCounts.publishedStories(), contentCounts.draftStories(), contentCounts.unpublishedStories(),
                canBreaking ? contentCounts.activeBreakingNews() : null
        );

        DashboardRepository.EngagementCounts engagementCounts = canContent ? repository.engagement(window.from(), window.to()) : null;
        DashboardResponse.Engagement engagement = engagementCounts == null ? null : new DashboardResponse.Engagement(
                engagementCounts.likes(), engagementCounts.comments(), engagementCounts.commentLikes()
        );

        DashboardRepository.ModerationCounts moderationCounts = canModerate ? repository.moderation(window.from(), window.to()) : null;
        DashboardResponse.Moderation moderation = moderationCounts == null ? null : new DashboardResponse.Moderation(
                moderationCounts.hiddenComments(), moderationCounts.moderatedComments()
        );

        DashboardRepository.UserCounts userCounts = canStaff ? repository.users(window.from(), window.to()) : null;
        DashboardResponse.Users users = userCounts == null ? null : new DashboardResponse.Users(
                userCounts.totalRegisteredUsers(), userCounts.newUsers()
        );

        DashboardRepository.StaffCounts staffCounts = canStaff ? repository.staff() : null;
        DashboardResponse.Staff staff = staffCounts == null ? null : new DashboardResponse.Staff(
                staffCounts.activeStaff(), staffCounts.disabledStaff(), staffCounts.pendingSetupStaff()
        );

        DashboardResponse.Newspaper newspaper = canNewspaper ? newspaper(window) : null;

        DashboardResponse.Advertising advertising = null;
        if (canAds) {
            DashboardRepository.AdCounts adCounts = repository.advertisements(now);
            long expiringSoon = repository.countExpiringAdvertisements(now, now.plus(Duration.ofHours(expiringSoonHours)));
            List<DashboardResponse.ExpiringAdvertisement> expiringAds = repository.expiringAdvertisements(
                            now, now.plus(Duration.ofHours(expiringSoonHours)))
                    .stream()
                    .map(row -> new DashboardResponse.ExpiringAdvertisement(row.advertisementId(), row.advertiserName(), row.title(), row.placement(), row.endAt()))
                    .toList();
            advertising = new DashboardResponse.Advertising(
                    adCounts.active(), adCounts.scheduled(), adCounts.paused(), adCounts.expired(), expiringSoon, expiringAds
            );
        }

        DashboardResponse.Attention attention = new DashboardResponse.Attention(attentionItems(content, moderation, newspaper, advertising, staff));
        List<DashboardResponse.TopStory> topStories = canContent ? repository.topStories(window.from(), window.to()).stream()
                .map(row -> new DashboardResponse.TopStory(row.storyId(), row.title(), row.category(), row.publishedAt(),
                        row.likeCount(), row.commentCount(), row.commentLikeCount(),
                        row.likeCount() + row.commentCount() + row.commentLikeCount()))
                .toList() : null;
        List<DashboardResponse.CategoryBreakdown> categoryBreakdown = canContent ? repository.categoryBreakdown(window.from(), window.to()).stream()
                .map(row -> new DashboardResponse.CategoryBreakdown(row.category(), row.published())).toList() : null;
        List<DashboardResponse.PublishingTrendPoint> publishingTrend = canContent
                ? publishingTrend(window.from(), window.to(), window.type(), window.timezone()) : null;

        Set<String> activityActions = activityActions(permissions);
        List<DashboardResponse.RecentActivity> recentActivity = repository.recentActivity(activityActions).stream()
                .map(row -> new DashboardResponse.RecentActivity(row.activityId(), row.action(), row.targetType(), row.targetLabel(),
                        row.actorName(), row.createdAt()))
                .toList();

        return new DashboardResponse(
                new DashboardResponse.Period(window.type(), window.from(), window.to(), window.timezone()),
                content, engagement, moderation, users, staff, newspaper, advertising, attention,
                topStories, categoryBreakdown, publishingTrend, recentActivity
        );
    }

    private DashboardResponse.Newspaper newspaper(DashboardPeriodCalculator.PeriodWindow window) {
        LocalDate today = window.to().atZone(java.time.ZoneId.of(window.timezone())).toLocalDate();
        List<DashboardResponse.Edition> editions = repository.newspapers(today).stream()
                .map(row -> new DashboardResponse.Edition(row.editionId(), row.editionName(), row.title(), row.status(), row.publishedAt()))
                .toList();
        long published = editions.stream().filter(edition -> "PUBLISHED".equals(edition.status())).count();
        long draft = editions.stream().filter(edition -> "DRAFT".equals(edition.status())).count();
        long unpublished = editions.stream().filter(edition -> "UNPUBLISHED".equals(edition.status())).count();
        return new DashboardResponse.Newspaper(published > 0, published, draft, unpublished, editions);
    }

    private List<DashboardResponse.PublishingTrendPoint> publishingTrend(
            Instant from, Instant to, DashboardPeriodType type, String timezone
    ) {
        List<DashboardResponse.PublishingTrendPoint> trend = repository.publishingTrend(from, to, timezone).stream()
                .map(row -> new DashboardResponse.PublishingTrendPoint(row.date(), row.published())).collect(Collectors.toCollection(ArrayList::new));
        if (type == DashboardPeriodType.TODAY) return trend;
        LocalDate start = from.atZone(java.time.ZoneId.of(timezone)).toLocalDate();
        LocalDate end = to.atZone(java.time.ZoneId.of(timezone)).toLocalDate();
        Set<LocalDate> present = trend.stream().map(DashboardResponse.PublishingTrendPoint::date).collect(Collectors.toSet());
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!present.contains(date)) trend.add(new DashboardResponse.PublishingTrendPoint(date, 0));
        }
        trend.sort(java.util.Comparator.comparing(DashboardResponse.PublishingTrendPoint::date));
        return trend;
    }

    private List<DashboardResponse.AttentionItem> attentionItems(
            DashboardResponse.Content content,
            DashboardResponse.Moderation moderation,
            DashboardResponse.Newspaper newspaper,
            DashboardResponse.Advertising advertising,
            DashboardResponse.Staff staff
    ) {
        List<DashboardResponse.AttentionItem> items = new ArrayList<>();
        if (content != null && content.draftStories() > 0) {
            items.add(new DashboardResponse.AttentionItem("draft-stories", "Draft stories", content.draftStories() + " stories are waiting for editorial work.", "INFO", "/admin/stories?status=DRAFT"));
        }
        if (moderation != null && moderation.hiddenComments() > 0) {
            items.add(new DashboardResponse.AttentionItem("hidden-comments", "Hidden comments", moderation.hiddenComments() + " comments are currently hidden.", "WARNING", "/admin/comments?status=HIDDEN"));
        }
        if (newspaper != null && !newspaper.todayPublished()) {
            items.add(new DashboardResponse.AttentionItem("newspaper-missing", "Today's newspaper", "No published edition exists for today.", "WARNING", "/admin/newspapers"));
        }
        if (advertising != null && advertising.expiringSoon() > 0) {
            items.add(new DashboardResponse.AttentionItem("ads-expiring", "Ads expiring soon", advertising.expiringSoon() + " active advertisements expire within the configured window.", "INFO", "/admin/ads?status=ACTIVE"));
        }
        if (staff != null && staff.pendingSetupStaff() > 0) {
            items.add(new DashboardResponse.AttentionItem("pending-staff", "Pending staff setup", staff.pendingSetupStaff() + " staff invitations still need setup.", "INFO", "/admin/staff"));
        }
        return items;
    }

    private Set<String> activityActions(Set<String> permissions) {
        LinkedHashSet<String> actions = new LinkedHashSet<>();
        if (can(permissions, "STORY_VIEW_ADMIN")) actions.addAll(actions("STORY_"));
        if (can(permissions, "BREAKING_NEWS_MANAGE")) actions.addAll(Set.of("BREAKING_NEWS_ENABLED", "BREAKING_NEWS_UPDATED", "BREAKING_NEWS_DISABLED"));
        if (can(permissions, "COMMENT_MODERATE")) actions.addAll(actions("COMMENT_"));
        if (can(permissions, "NEWSPAPER_VIEW_ADMIN")) actions.addAll(actions("NEWSPAPER_"));
        if (can(permissions, "AD_VIEW_ADMIN")) actions.addAll(actions("AD_"));
        if (can(permissions, "CATEGORY_MANAGE")) actions.addAll(actions("CATEGORY_"));
        if (can(permissions, "TAG_MANAGE")) actions.addAll(actions("TAG_"));
        if (can(permissions, "STAFF_VIEW")) actions.addAll(actions("STAFF_"));
        if (can(permissions, "ROLE_VIEW")) actions.addAll(actions("ROLE_"));
        return actions;
    }

    private Set<String> actions(String prefix) {
        return switch (prefix) {
            case "STORY_" -> Set.of("STORY_CREATED", "STORY_UPDATED", "STORY_PUBLISHED", "STORY_UNPUBLISHED", "STORY_DELETED");
            case "COMMENT_" -> Set.of("COMMENT_HIDDEN", "COMMENT_RESTORED", "COMMENT_DELETED_BY_MODERATOR");
            case "NEWSPAPER_" -> Set.of("NEWSPAPER_CREATED", "NEWSPAPER_UPDATED", "NEWSPAPER_DOCUMENT_REPLACED", "NEWSPAPER_PUBLISHED", "NEWSPAPER_UNPUBLISHED", "NEWSPAPER_DELETED");
            case "AD_" -> Set.of("AD_CREATED", "AD_UPDATED", "AD_PLACEMENT_CHANGED", "AD_PUBLISHED", "AD_PAUSED", "AD_RESUMED", "AD_DELETED");
            case "CATEGORY_" -> Set.of("CATEGORY_CREATED", "CATEGORY_UPDATED", "CATEGORY_REORDERED", "CATEGORY_DELETED");
            case "TAG_" -> Set.of("TAG_CREATED", "TAG_UPDATED", "TAG_DELETED");
            case "STAFF_" -> Set.of("STAFF_CREATED", "STAFF_UPDATED", "STAFF_DISABLED", "STAFF_ENABLED", "STAFF_ROLES_CHANGED");
            case "ROLE_" -> Set.of("ROLE_CREATED", "ROLE_UPDATED", "ROLE_PERMISSIONS_CHANGED", "ROLE_DELETED");
            default -> Set.of();
        };
    }

    private Set<String> permissions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return Set.of();
        return authentication.getAuthorities().stream().map(authority -> authority.getAuthority()).collect(Collectors.toSet());
    }

    private boolean isStaff(Set<String> permissions) {
        return permissions.contains("SUPER_ADMIN") || permissions.stream().anyMatch(ADMIN_PERMISSIONS::contains);
    }

    private boolean can(Set<String> permissions, String permission) {
        return permissions.contains("SUPER_ADMIN") || permissions.contains(permission);
    }
}
