CREATE TABLE story_likes (
    user_id UUID NOT NULL,
    story_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_story_likes PRIMARY KEY (user_id, story_id),
    CONSTRAINT fk_story_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_story_likes_story FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE
);

CREATE INDEX idx_story_likes_story_id ON story_likes (story_id);
CREATE INDEX idx_story_likes_user_id ON story_likes (user_id);

CREATE TABLE comments (
    id UUID PRIMARY KEY,
    story_id UUID NOT NULL,
    user_id UUID NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    moderated_at TIMESTAMP WITH TIME ZONE,
    moderated_by UUID,
    moderation_reason VARCHAR(500),
    CONSTRAINT ck_comments_status CHECK (status IN ('VISIBLE', 'HIDDEN', 'DELETED')),
    CONSTRAINT ck_comments_body_not_blank CHECK (length(trim(body)) > 0),
    CONSTRAINT fk_comments_story FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_comments_moderated_by FOREIGN KEY (moderated_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_comments_story_id ON comments (story_id);
CREATE INDEX idx_comments_user_id ON comments (user_id);
CREATE INDEX idx_comments_status ON comments (status);
CREATE INDEX idx_comments_created_at ON comments (created_at);
CREATE INDEX idx_comments_story_status_created ON comments (story_id, status, created_at, id);
