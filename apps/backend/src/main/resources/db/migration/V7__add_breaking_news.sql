ALTER TABLE stories
    ADD COLUMN is_breaking BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN breaking_started_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN breaking_until TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_stories_active_breaking
    ON stories (breaking_started_at DESC, breaking_until, id DESC)
    WHERE status = 'PUBLISHED' AND is_breaking = TRUE;
