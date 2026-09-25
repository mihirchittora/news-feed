# Admin dashboard metrics

The operational dashboard is available at `/admin/dashboard` and is backed by `GET /api/v1/admin/dashboard?period=...`. It reads the existing PostgreSQL domain tables directly; it does not add a reporting database, cache, event pipeline, or tracking system.

## Periods and timezone

The API accepts `TODAY`, `LAST_7_DAYS`, and `LAST_30_DAYS`. `TODAY` is the start of the current calendar day through the current instant. The longer periods are calendar-day windows containing today plus the previous six or 29 days. Boundaries are calculated on the server using `APP_TIMEZONE` (default `Asia/Kolkata`), while database timestamps remain UTC. The response includes the selected timezone and UTC instants in `period.from` and `period.to`.

## Metric definitions

- **Published stories**: stories whose `published_at` falls inside the selected period.
- **Draft stories** and **unpublished stories**: current story state counts, independent of the selected period.
- **Active Breaking News**: published stories whose breaking window has started and has not expired. This is current state.
- **Likes**, **comments**, and **comment likes**: rows created inside the selected period. Comments count as engagement even if later hidden or deleted; moderation state is reported separately.
- **Most engaged stories**: the top five published stories with period activity, ordered by `story likes + comments + comment likes`. This is a transparent dashboard count and does not change public feed ordering.
- **Stories by category**: period publication counts grouped under the story's top-level category. Stories without a category are grouped as `Uncategorized`.
- **Publishing trend**: daily publication counts for the selected calendar window, using `APP_TIMEZONE` day boundaries.
- **Total registered users**: accounts with the `USER` role and a status other than `PENDING_SETUP`. Disabled normal users remain registered; staff invitations are excluded.
- **New users**: registered users created inside the selected period.
- **Newspaper status**: today's editions by `edition_date`; only `PUBLISHED` editions satisfy `todayPublished`. Multiple editions are returned individually and counted by status.
- **Advertisement status**: effective current status derived from stored status and `start_at`/`end_at`. An active ad is expiring soon when it is active and ends within `AD_EXPIRING_SOON_HOURS` (default 24).
- **Hidden comments**: current comments with `HIDDEN` status. **Moderated comments** use `moderated_at` inside the selected period.
- **Recent activity**: the latest ten existing audit events that belong to permission families visible to the current staff member.

## Permission behavior

The endpoint is available only to authenticated administrative staff. Sections are filtered on the server:

- `STORY_VIEW_ADMIN`: content, engagement, category breakdown, publishing trend, and top stories.
- `BREAKING_NEWS_MANAGE`: active Breaking News count.
- `COMMENT_MODERATE`: moderation counts and comment moderation activity.
- `NEWSPAPER_VIEW_ADMIN`: newspaper status and newspaper activity.
- `AD_VIEW_ADMIN`: advertisement status, expiry list, and ad activity.
- `STAFF_VIEW`: registered-user and staff summaries plus staff activity.
- `ROLE_VIEW`: role/permission activity.

Unauthorized sections are returned as `null`; the frontend does not render their cards. Ordinary `USER` accounts receive `403 FORBIDDEN`. Viewing the dashboard is not audited.
