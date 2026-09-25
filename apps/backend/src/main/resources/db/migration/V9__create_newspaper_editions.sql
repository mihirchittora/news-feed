CREATE TABLE newspaper_editions (
    id UUID PRIMARY KEY,
    title VARCHAR(240) NOT NULL,
    edition VARCHAR(120) NOT NULL,
    edition_date DATE NOT NULL,
    pdf_storage_key VARCHAR(500),
    cover_image_storage_key VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP WITH TIME ZONE,
    uploaded_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_newspaper_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT ck_newspaper_edition_not_blank CHECK (length(trim(edition)) > 0),
    CONSTRAINT ck_newspaper_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'UNPUBLISHED')),
    CONSTRAINT fk_newspaper_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_newspaper_edition_date_edition
    ON newspaper_editions (edition_date, lower(edition));
CREATE INDEX idx_newspaper_edition_date ON newspaper_editions (edition_date DESC);
CREATE INDEX idx_newspaper_status_date ON newspaper_editions (status, edition_date DESC, published_at DESC);
CREATE INDEX idx_newspaper_edition ON newspaper_editions (edition);
