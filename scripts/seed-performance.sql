-- Deterministic, idempotent development-only seed. It never runs from the application.
-- Approximate dataset: 10,000 stories, 10 categories (5 parents + 5 children),
-- 100 tags, 100 users, 50,000 story likes, 20,000 comments, 20,000 comment likes,
-- 100 newspaper rows, 100 ads, and 1,000 audit rows.
BEGIN;

INSERT INTO users (id, name, email, password_hash, status, created_at, updated_at)
SELECT ('00000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
       'Performance User ' || i, 'perf-user-' || i || '@example.invalid',
       '$2a$10$7EqJtq98hPqEX7fNZaFWoO5q8M1fJrWmPqPzQ2Oe4jG7cX8Z0y8mK', 'ACTIVE', now(), now()
FROM generate_series(1, 100) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT ('00000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
       '00000000-0000-0000-0000-000000000001'::uuid
FROM generate_series(1, 100) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO categories (id, name, slug, description, status, display_order, created_at, updated_at, parent_id)
SELECT ('00000000-0000-0000-0002-' || lpad(i::text, 12, '0'))::uuid,
       CASE WHEN i <= 5 THEN 'Performance Parent ' || i ELSE 'Performance Child ' || i END,
       'performance-category-' || i, 'Deterministic performance seed category', 'ACTIVE', i, now(), now(),
       CASE WHEN i <= 5 THEN NULL ELSE ('00000000-0000-0000-0002-' || lpad(((i - 1) % 5 + 1)::text, 12, '0'))::uuid END
FROM generate_series(1, 10) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO tags (id, name, normalized_name, slug, created_at, updated_at)
SELECT ('00000000-0000-0000-0003-' || lpad(i::text, 12, '0'))::uuid,
       'Performance Tag ' || i, lower('performance-tag-' || i), 'performance-tag-' || i, now(), now()
FROM generate_series(1, 100) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO stories (id, title, slug, summary, body, category_id, status, author_id, published_at, created_at, updated_at, is_breaking, breaking_started_at, breaking_until)
SELECT ('00000000-0000-0000-0004-' || lpad(i::text, 12, '0'))::uuid,
       'Performance story ' || i, 'performance-story-' || i, 'Seeded story ' || i, '<p>Deterministic performance content.</p>',
       ('00000000-0000-0000-0002-' || lpad((6 + ((i - 1) % 5))::text, 12, '0'))::uuid, 'PUBLISHED',
       '00000000-0000-0000-0001-000000000001'::uuid, now() - (i || ' minutes')::interval,
       now() - (i || ' minutes')::interval, now(), false, NULL, NULL
FROM generate_series(1, 10000) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO story_tags (story_id, tag_id)
SELECT ('00000000-0000-0000-0004-' || lpad(story_no::text, 12, '0'))::uuid,
       ('00000000-0000-0000-0003-' || lpad(tag_no::text, 12, '0'))::uuid
FROM generate_series(1, 10000) AS stories(story_no)
CROSS JOIN LATERAL generate_series(1, 3) AS tag_series(tag_offset)
CROSS JOIN LATERAL (SELECT ((stories.story_no + tag_series.tag_offset - 2) % 100) + 1 AS tag_no) selected
ON CONFLICT DO NOTHING;

INSERT INTO story_likes (user_id, story_id, created_at)
SELECT ('00000000-0000-0000-0001-' || lpad((((i - 1) % 100) + 1)::text, 12, '0'))::uuid,
       ('00000000-0000-0000-0004-' || lpad((((i - 1) % 10000) + 1)::text, 12, '0'))::uuid, now()
FROM generate_series(1, 50000) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO comments (id, story_id, user_id, body, status, created_at, updated_at)
SELECT ('00000000-0000-0000-0005-' || lpad(i::text, 12, '0'))::uuid,
       ('00000000-0000-0000-0004-' || lpad((((i - 1) % 10000) + 1)::text, 12, '0'))::uuid,
       ('00000000-0000-0000-0001-' || lpad((((i - 1) % 100) + 1)::text, 12, '0'))::uuid,
       'Deterministic performance comment ' || i, 'VISIBLE', now(), now()
FROM generate_series(1, 20000) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO comment_likes (user_id, comment_id, created_at)
SELECT ('00000000-0000-0000-0001-' || lpad((((i - 1) % 100) + 1)::text, 12, '0'))::uuid,
       ('00000000-0000-0000-0005-' || lpad((((i - 1) % 20000) + 1)::text, 12, '0'))::uuid, now()
FROM generate_series(1, 20000) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO newspaper_editions (id, title, edition, edition_date, status, uploaded_by, created_at, updated_at)
SELECT ('00000000-0000-0000-0006-' || lpad(i::text, 12, '0'))::uuid, 'Performance newspaper ' || i, 'Perf-' || i,
       current_date - i, 'DRAFT', '00000000-0000-0000-0001-000000000001'::uuid, now(), now()
FROM generate_series(1, 100) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO advertisements (id, title, advertiser_name, media_type, media_storage_key, media_url, mime_type, file_size, start_at, end_at, status, created_by, created_at, updated_at)
SELECT ('00000000-0000-0000-0007-' || lpad(i::text, 12, '0'))::uuid, 'Performance ad ' || i, 'Performance advertiser', 'IMAGE',
       'performance/seed-' || i || '.jpg', 'http://localhost:8080/api/v1/media/performance/seed-' || i || '.jpg', 'image/jpeg', 1,
       now() - interval '1 day', now() + interval '1 day', 'ACTIVE', '00000000-0000-0000-0001-000000000001'::uuid, now(), now()
FROM generate_series(1, 100) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO advertisement_placements (id, advertisement_id, placement_type)
SELECT ('00000000-0000-0000-0008-' || lpad(i::text, 12, '0'))::uuid,
       ('00000000-0000-0000-0007-' || lpad(i::text, 12, '0'))::uuid, 'HOME_FEED'
FROM generate_series(1, 100) AS series(i)
ON CONFLICT DO NOTHING;

INSERT INTO admin_audit_log (id, actor_user_id, action, target_type, target_id, metadata, created_at)
SELECT ('00000000-0000-0000-0009-' || lpad(i::text, 12, '0'))::uuid,
       '00000000-0000-0000-0001-000000000001'::uuid, 'STORY_PUBLISHED', 'STORY',
       ('00000000-0000-0000-0004-' || lpad((((i - 1) % 10000) + 1)::text, 12, '0'))::uuid, '{}', now()
FROM generate_series(1, 1000) AS series(i)
ON CONFLICT DO NOTHING;

COMMIT;
