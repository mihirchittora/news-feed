\echo '--- report-only data consistency check ---'

SELECT 'story media with broken story references' AS check_name, COUNT(*) AS findings
FROM story_media media
LEFT JOIN stories story ON story.id = media.story_id
WHERE media.story_id IS NOT NULL AND story.id IS NULL;

SELECT 'unattached story media uploads' AS check_name, COUNT(*) AS findings
FROM story_media
WHERE story_id IS NULL;

SELECT 'invalid category relationships' AS check_name, COUNT(*) AS findings
FROM categories child JOIN categories parent ON parent.id = child.parent_id
WHERE parent.parent_id IS NOT NULL OR child.parent_id = child.id;

SELECT 'orphaned story likes' AS check_name, COUNT(*) AS findings
FROM story_likes likes
LEFT JOIN stories story ON story.id = likes.story_id
LEFT JOIN users users ON users.id = likes.user_id
WHERE story.id IS NULL OR users.id IS NULL;

SELECT 'orphaned comments' AS check_name, COUNT(*) AS findings
FROM comments comment
LEFT JOIN stories story ON story.id = comment.story_id
LEFT JOIN users users ON users.id = comment.user_id
WHERE story.id IS NULL OR users.id IS NULL;

SELECT 'invalid user-role mappings' AS check_name, COUNT(*) AS findings
FROM user_roles mapping
LEFT JOIN users users ON users.id = mapping.user_id
LEFT JOIN roles roles ON roles.id = mapping.role_id
WHERE users.id IS NULL OR roles.id IS NULL;

SELECT 'invalid role-permission mappings' AS check_name, COUNT(*) AS findings
FROM role_permissions mapping
LEFT JOIN roles roles ON roles.id = mapping.role_id
LEFT JOIN permissions permissions ON permissions.id = mapping.permission_id
WHERE roles.id IS NULL OR permissions.id IS NULL;

SELECT 'published newspapers missing PDF' AS check_name, COUNT(*) AS findings
FROM newspaper_editions WHERE status = 'PUBLISHED' AND (pdf_storage_key IS NULL OR length(trim(pdf_storage_key)) = 0);

SELECT 'advertisements with invalid schedules' AS check_name, COUNT(*) AS findings
FROM advertisements WHERE start_at >= end_at;

SELECT 'advertisements with invalid placements' AS check_name, COUNT(*) AS findings
FROM advertisement_placements
WHERE (placement_type = 'CATEGORY_FEED' AND category_id IS NULL)
   OR (placement_type <> 'CATEGORY_FEED' AND category_id IS NOT NULL);

\echo 'Storage-reference checks for local files are performed by scripts/check-data-consistency.sh.'
