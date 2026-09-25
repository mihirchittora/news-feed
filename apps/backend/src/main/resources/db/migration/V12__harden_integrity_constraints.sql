ALTER TABLE story_media
    ADD CONSTRAINT ck_story_media_file_size_positive CHECK (file_size > 0),
    ADD CONSTRAINT ck_story_media_sort_order_nonnegative CHECK (sort_order >= 0);

ALTER TABLE comments
    ADD CONSTRAINT ck_comments_parent_not_self CHECK (parent_comment_id IS NULL OR parent_comment_id <> id);

CREATE INDEX idx_story_tags_tag_story ON story_tags (tag_id, story_id);
CREATE INDEX idx_stories_category_status_published ON stories (category_id, status, published_at DESC, id DESC);
CREATE INDEX idx_story_likes_created_story ON story_likes (created_at, story_id);
CREATE INDEX idx_comment_likes_created_at_comment ON comment_likes (created_at, comment_id);
CREATE INDEX idx_ad_placement_type_category_ad ON advertisement_placements (placement_type, category_id, advertisement_id);
