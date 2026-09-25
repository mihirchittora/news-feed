ALTER TABLE comments
    ADD COLUMN parent_comment_id UUID;

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE;

CREATE INDEX idx_comments_parent_id ON comments (parent_comment_id);
CREATE INDEX idx_comments_story_parent_status_created
    ON comments (story_id, parent_comment_id, status, created_at, id);

CREATE TABLE comment_likes (
    user_id UUID NOT NULL,
    comment_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_comment_likes PRIMARY KEY (user_id, comment_id),
    CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE INDEX idx_comment_likes_comment_id ON comment_likes (comment_id);
CREATE INDEX idx_comment_likes_user_id ON comment_likes (user_id);
