CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_categories_slug UNIQUE (slug),
    CONSTRAINT ck_categories_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_categories_name_not_blank CHECK (length(trim(name)) > 0),
    CONSTRAINT ck_categories_slug_not_blank CHECK (length(trim(slug)) > 0)
);

CREATE UNIQUE INDEX uk_categories_name_lower ON categories (lower(name));

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    normalized_name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_tags_normalized_name UNIQUE (normalized_name),
    CONSTRAINT uk_tags_slug UNIQUE (slug),
    CONSTRAINT ck_tags_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE TABLE stories (
    id UUID PRIMARY KEY,
    title VARCHAR(240) NOT NULL,
    slug VARCHAR(260) NOT NULL,
    summary VARCHAR(600),
    body TEXT NOT NULL DEFAULT '',
    category_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author_id UUID NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_stories_slug UNIQUE (slug),
    CONSTRAINT ck_stories_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED')),
    CONSTRAINT fk_stories_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_stories_author FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_stories_status_published_at ON stories (status, published_at DESC, id DESC);
CREATE INDEX idx_stories_category ON stories (category_id);
CREATE INDEX idx_stories_slug ON stories (slug);

CREATE TABLE story_tags (
    story_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (story_id, tag_id),
    CONSTRAINT fk_story_tags_story FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,
    CONSTRAINT fk_story_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE RESTRICT
);

CREATE INDEX idx_story_tags_story ON story_tags (story_id);
CREATE INDEX idx_story_tags_tag ON story_tags (tag_id);

CREATE TABLE story_media (
    id UUID PRIMARY KEY,
    story_id UUID,
    type VARCHAR(20) NOT NULL,
    storage_key VARCHAR(300) NOT NULL,
    url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    mime_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    width INTEGER,
    height INTEGER,
    duration_seconds NUMERIC(12, 3),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_story_media_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_story_media_type CHECK (type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT fk_story_media_story FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE
);

CREATE INDEX idx_story_media_story ON story_media (story_id, sort_order);
