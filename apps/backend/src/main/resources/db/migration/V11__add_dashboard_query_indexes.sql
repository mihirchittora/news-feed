CREATE INDEX idx_stories_published_at ON stories (published_at DESC, id DESC);
CREATE INDEX idx_story_likes_created_at ON story_likes (created_at);
CREATE INDEX idx_comment_likes_created_at ON comment_likes (created_at);
CREATE INDEX idx_comments_moderated_at ON comments (moderated_at);
CREATE INDEX idx_users_created_at_status ON users (created_at, status);
CREATE INDEX idx_advertisements_end_at_status ON advertisements (end_at, status, start_at);
CREATE INDEX idx_admin_audit_created_at ON admin_audit_log (created_at DESC, id DESC);
