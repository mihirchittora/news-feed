package com.newsplatform.dashboard.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public class DashboardRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public DashboardRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public ContentCounts content(Instant from, Instant to, Instant now) {
        String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE s.status = 'PUBLISHED' AND s.published_at >= :from AND s.published_at < :to) AS published_stories,
                    COUNT(*) FILTER (WHERE s.status = 'DRAFT') AS draft_stories,
                    COUNT(*) FILTER (WHERE s.status = 'UNPUBLISHED') AS unpublished_stories,
                    COUNT(*) FILTER (WHERE s.status = 'PUBLISHED' AND s.is_breaking = TRUE
                        AND s.breaking_started_at <= :now AND (s.breaking_until IS NULL OR s.breaking_until > :now)) AS active_breaking
                FROM stories s
                """;
        return jdbc.queryForObject(sql, params(from, to, now), (rs, rowNum) -> new ContentCounts(
                rs.getLong("published_stories"),
                rs.getLong("draft_stories"),
                rs.getLong("unpublished_stories"),
                rs.getLong("active_breaking")
        ));
    }

    public EngagementCounts engagement(Instant from, Instant to) {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM story_likes WHERE created_at >= :from AND created_at < :to) AS likes,
                    (SELECT COUNT(*) FROM comments WHERE created_at >= :from AND created_at < :to) AS comments,
                    (SELECT COUNT(*) FROM comment_likes WHERE created_at >= :from AND created_at < :to) AS comment_likes
                """;
        return jdbc.queryForObject(sql, params(from, to, null), (rs, rowNum) -> new EngagementCounts(
                rs.getLong("likes"), rs.getLong("comments"), rs.getLong("comment_likes")
        ));
    }

    public ModerationCounts moderation(Instant from, Instant to) {
        String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE c.status = 'HIDDEN') AS hidden_comments,
                    COUNT(*) FILTER (WHERE c.moderated_at >= :from AND c.moderated_at < :to) AS moderated_comments
                FROM comments c
                """;
        return jdbc.queryForObject(sql, params(from, to, null), (rs, rowNum) -> new ModerationCounts(
                rs.getLong("hidden_comments"), rs.getLong("moderated_comments")
        ));
    }

    public UserCounts users(Instant from, Instant to) {
        String sql = """
                SELECT
                    COUNT(DISTINCT u.id) AS total_registered_users,
                    COUNT(DISTINCT u.id) FILTER (WHERE u.created_at >= :from AND u.created_at < :to) AS new_users
                FROM users u
                JOIN user_roles ur ON ur.user_id = u.id
                JOIN roles r ON r.id = ur.role_id
                WHERE r.code = 'USER' AND u.status <> 'PENDING_SETUP'
                """;
        return jdbc.queryForObject(sql, params(from, to, null), (rs, rowNum) -> new UserCounts(
                rs.getLong("total_registered_users"), rs.getLong("new_users")
        ));
    }

    public StaffCounts staff() {
        String sql = """
                SELECT
                    COUNT(DISTINCT u.id) FILTER (WHERE u.status = 'ACTIVE') AS active_staff,
                    COUNT(DISTINCT u.id) FILTER (WHERE u.status = 'DISABLED') AS disabled_staff,
                    COUNT(DISTINCT u.id) FILTER (WHERE u.status = 'PENDING_SETUP') AS pending_setup_staff
                FROM users u
                JOIN user_roles ur ON ur.user_id = u.id
                JOIN roles r ON r.id = ur.role_id
                WHERE r.code <> 'USER'
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) -> new StaffCounts(
                rs.getLong("active_staff"), rs.getLong("disabled_staff"), rs.getLong("pending_setup_staff")
        ));
    }

    public List<CategoryCount> categoryBreakdown(Instant from, Instant to) {
        String sql = """
                SELECT COALESCE(parent.name, category.name, 'Uncategorized') AS category,
                       COUNT(*) AS published
                FROM stories s
                LEFT JOIN categories category ON category.id = s.category_id
                LEFT JOIN categories parent ON parent.id = category.parent_id
                WHERE s.status = 'PUBLISHED' AND s.published_at >= :from AND s.published_at < :to
                GROUP BY COALESCE(parent.name, category.name, 'Uncategorized')
                ORDER BY published DESC, category ASC
                """;
        return jdbc.query(sql, params(from, to, null), (rs, rowNum) -> new CategoryCount(
                rs.getString("category"), rs.getLong("published")
        ));
    }

    public List<TrendPoint> publishingTrend(Instant from, Instant to, String timezone) {
        String sql = """
                SELECT (s.published_at AT TIME ZONE :timezone)::date AS day, COUNT(*) AS published
                FROM stories s
                WHERE s.status = 'PUBLISHED' AND s.published_at >= :from AND s.published_at < :to
                GROUP BY day
                ORDER BY day ASC
                """;
        MapSqlParameterSource source = params(from, to, null).addValue("timezone", timezone);
        return jdbc.query(sql, source, (rs, rowNum) -> new TrendPoint(
                rs.getObject("day", LocalDate.class), rs.getLong("published")
        ));
    }

    public List<TopStoryRow> topStories(Instant from, Instant to) {
        String sql = """
                SELECT s.id,
                       s.title,
                       COALESCE(parent.name, category.name, 'Uncategorized') AS category,
                       s.published_at,
                       COALESCE(sl.like_count, 0) AS like_count,
                       COALESCE(cm.comment_count, 0) AS comment_count,
                       COALESCE(cl.comment_like_count, 0) AS comment_like_count
                FROM stories s
                LEFT JOIN categories category ON category.id = s.category_id
                LEFT JOIN categories parent ON parent.id = category.parent_id
                LEFT JOIN (
                    SELECT story_id, COUNT(*) AS like_count
                    FROM story_likes
                    WHERE created_at >= :from AND created_at < :to
                    GROUP BY story_id
                ) sl ON sl.story_id = s.id
                LEFT JOIN (
                    SELECT story_id, COUNT(*) AS comment_count
                    FROM comments
                    WHERE created_at >= :from AND created_at < :to
                    GROUP BY story_id
                ) cm ON cm.story_id = s.id
                LEFT JOIN (
                    SELECT c.story_id, COUNT(*) AS comment_like_count
                    FROM comment_likes cl
                    JOIN comments c ON c.id = cl.comment_id
                    WHERE cl.created_at >= :from AND cl.created_at < :to
                    GROUP BY c.story_id
                ) cl ON cl.story_id = s.id
                WHERE s.status = 'PUBLISHED'
                  AND (sl.story_id IS NOT NULL OR cm.story_id IS NOT NULL OR cl.story_id IS NOT NULL)
                ORDER BY (COALESCE(sl.like_count, 0) + COALESCE(cm.comment_count, 0) + COALESCE(cl.comment_like_count, 0)) DESC,
                         s.published_at DESC NULLS LAST, s.id DESC
                LIMIT 5
                """;
        return jdbc.query(sql, params(from, to, null), (rs, rowNum) -> new TopStoryRow(
                toUuid(rs.getObject("id")),
                rs.getString("title"),
                rs.getString("category"),
                readInstant(rs, "published_at"),
                rs.getLong("like_count"),
                rs.getLong("comment_count"),
                rs.getLong("comment_like_count")
        ));
    }

    public List<NewspaperRow> newspapers(LocalDate editionDate) {
        String sql = """
                SELECT id, edition, title, status, published_at
                FROM newspaper_editions
                WHERE edition_date = :editionDate
                ORDER BY CASE status WHEN 'PUBLISHED' THEN 0 WHEN 'DRAFT' THEN 1 ELSE 2 END, edition ASC, id ASC
                """;
        return jdbc.query(sql, new MapSqlParameterSource("editionDate", editionDate), (rs, rowNum) -> new NewspaperRow(
                toUuid(rs.getObject("id")),
                rs.getString("edition"),
                rs.getString("title"),
                rs.getString("status"),
                readInstant(rs, "published_at")
        ));
    }

    public AdCounts advertisements(Instant now) {
        String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE effective_status = 'ACTIVE') AS active,
                    COUNT(*) FILTER (WHERE effective_status = 'SCHEDULED') AS scheduled,
                    COUNT(*) FILTER (WHERE effective_status = 'PAUSED') AS paused,
                    COUNT(*) FILTER (WHERE effective_status = 'EXPIRED') AS expired
                FROM (
                    SELECT CASE
                        WHEN end_at <= :now THEN 'EXPIRED'
                        WHEN status = 'DRAFT' THEN 'DRAFT'
                        WHEN status = 'PAUSED' THEN 'PAUSED'
                        WHEN status = 'EXPIRED' THEN 'EXPIRED'
                        WHEN start_at > :now THEN 'SCHEDULED'
                        ELSE 'ACTIVE'
                    END AS effective_status
                    FROM advertisements
                ) current_ads
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource().addValue("now", jdbcInstant(now), Types.TIMESTAMP_WITH_TIMEZONE), (rs, rowNum) -> new AdCounts(
                rs.getLong("active"), rs.getLong("scheduled"), rs.getLong("paused"), rs.getLong("expired")
        ));
    }

    public List<ExpiringAdRow> expiringAdvertisements(Instant now, Instant threshold) {
        String sql = """
                SELECT a.id, a.advertiser_name, a.title, a.end_at,
                       COALESCE(string_agg(DISTINCT p.placement_type, ', ' ORDER BY p.placement_type), 'Unassigned') AS placement
                FROM advertisements a
                LEFT JOIN advertisement_placements p ON p.advertisement_id = a.id
                WHERE a.status = 'ACTIVE' AND a.start_at <= :now AND a.end_at > :now AND a.end_at <= :threshold
                GROUP BY a.id, a.advertiser_name, a.title, a.end_at
                ORDER BY a.end_at ASC, a.id ASC
                LIMIT 5
                """;
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("now", jdbcInstant(now), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("threshold", jdbcInstant(threshold), Types.TIMESTAMP_WITH_TIMEZONE), (rs, rowNum) -> new ExpiringAdRow(
                toUuid(rs.getObject("id")), rs.getString("advertiser_name"), rs.getString("title"),
                rs.getString("placement"), readInstant(rs, "end_at")
        ));
    }

    public long countExpiringAdvertisements(Instant now, Instant threshold) {
        String sql = """
                SELECT COUNT(*)
                FROM advertisements
                WHERE status = 'ACTIVE' AND start_at <= :now AND end_at > :now AND end_at <= :threshold
                """;
        Long count = jdbc.queryForObject(sql, new MapSqlParameterSource()
                .addValue("now", jdbcInstant(now), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("threshold", jdbcInstant(threshold), Types.TIMESTAMP_WITH_TIMEZONE), Long.class);
        return count == null ? 0 : count;
    }

    public List<AuditRow> recentActivity(Collection<String> actions) {
        if (actions.isEmpty()) return List.of();
        String sql = """
                SELECT a.id, a.action, a.target_type,
                       COALESCE(story_target.title, ad_target.title, newspaper_target.title,
                                comment_story.title, user_target.name, a.target_type) AS target_label,
                       actor.name AS actor_name, a.created_at
                FROM admin_audit_log a
                LEFT JOIN users actor ON actor.id = a.actor_user_id
                LEFT JOIN stories story_target ON a.target_type = 'STORY' AND story_target.id = a.target_id
                LEFT JOIN advertisements ad_target ON a.target_type = 'ADVERTISEMENT' AND ad_target.id = a.target_id
                LEFT JOIN newspaper_editions newspaper_target ON a.target_type = 'NEWSPAPER' AND newspaper_target.id = a.target_id
                LEFT JOIN comments comment_target ON a.target_type = 'COMMENT' AND comment_target.id = a.target_id
                LEFT JOIN stories comment_story ON comment_story.id = comment_target.story_id
                LEFT JOIN users user_target ON a.target_type = 'USER' AND user_target.id = a.target_id
                WHERE a.action IN (:actions)
                ORDER BY a.created_at DESC, a.id DESC
                LIMIT 10
                """;
        return jdbc.query(sql, new MapSqlParameterSource("actions", actions), (rs, rowNum) -> new AuditRow(
                toUuid(rs.getObject("id")), rs.getString("action"), rs.getString("target_type"),
                rs.getString("target_label"), rs.getString("actor_name"), readInstant(rs, "created_at")
        ));
    }

    private MapSqlParameterSource params(Instant from, Instant to, Instant now) {
        MapSqlParameterSource source = new MapSqlParameterSource();
        if (from != null) source.addValue("from", jdbcInstant(from), Types.TIMESTAMP_WITH_TIMEZONE);
        if (to != null) source.addValue("to", jdbcInstant(to), Types.TIMESTAMP_WITH_TIMEZONE);
        if (now != null) source.addValue("now", jdbcInstant(now), Types.TIMESTAMP_WITH_TIMEZONE);
        return source;
    }

    private static OffsetDateTime jdbcInstant(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }

    private static UUID toUuid(Object value) {
        if (value instanceof UUID uuid) return uuid;
        return value == null ? null : UUID.fromString(value.toString());
    }

    private static Instant readInstant(ResultSet resultSet, String column) throws SQLException {
        Object value = resultSet.getObject(column);
        if (value == null) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof OffsetDateTime offsetDateTime) return offsetDateTime.toInstant();
        if (value instanceof ZonedDateTime zonedDateTime) return zonedDateTime.toInstant();
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        return Instant.parse(value.toString());
    }

    public record ContentCounts(long publishedStories, long draftStories, long unpublishedStories, long activeBreakingNews) { }
    public record EngagementCounts(long likes, long comments, long commentLikes) { }
    public record ModerationCounts(long hiddenComments, long moderatedComments) { }
    public record UserCounts(long totalRegisteredUsers, long newUsers) { }
    public record StaffCounts(long activeStaff, long disabledStaff, long pendingSetupStaff) { }
    public record CategoryCount(String category, long published) { }
    public record TrendPoint(LocalDate date, long published) { }
    public record TopStoryRow(UUID storyId, String title, String category, Instant publishedAt, long likeCount, long commentCount, long commentLikeCount) { }
    public record NewspaperRow(UUID editionId, String editionName, String title, String status, Instant publishedAt) { }
    public record AdCounts(long active, long scheduled, long paused, long expired) { }
    public record ExpiringAdRow(UUID advertisementId, String advertiserName, String title, String placement, Instant endAt) { }
    public record AuditRow(UUID activityId, String action, String targetType, String targetLabel, String actorName, Instant createdAt) { }
}
