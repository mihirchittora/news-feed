CREATE TABLE advertisements (
    id UUID PRIMARY KEY,
    title VARCHAR(240) NOT NULL,
    advertiser_name VARCHAR(180) NOT NULL,
    description TEXT,
    media_type VARCHAR(20) NOT NULL,
    media_storage_key VARCHAR(300) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    mime_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    destination_url VARCHAR(1000),
    start_at TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_ad_title_not_blank CHECK (length(trim(title)) > 0),
    CONSTRAINT ck_advertiser_name_not_blank CHECK (length(trim(advertiser_name)) > 0),
    CONSTRAINT ck_ad_media_type CHECK (media_type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT ck_ad_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'ACTIVE', 'PAUSED', 'EXPIRED')),
    CONSTRAINT ck_ad_dates CHECK (start_at < end_at),
    CONSTRAINT ck_ad_file_size CHECK (file_size > 0),
    CONSTRAINT fk_ad_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE advertisement_placements (
    id UUID PRIMARY KEY,
    advertisement_id UUID NOT NULL,
    placement_type VARCHAR(30) NOT NULL,
    category_id UUID,
    CONSTRAINT ck_ad_placement_type CHECK (placement_type IN ('HOME_BANNER', 'HOME_FEED', 'CATEGORY_FEED', 'NEWSPAPER')),
    CONSTRAINT ck_ad_placement_category CHECK (
        (placement_type = 'CATEGORY_FEED' AND category_id IS NOT NULL)
        OR (placement_type <> 'CATEGORY_FEED' AND category_id IS NULL)
    ),
    CONSTRAINT fk_ad_placement_ad FOREIGN KEY (advertisement_id) REFERENCES advertisements(id) ON DELETE CASCADE,
    CONSTRAINT fk_ad_placement_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX uk_ad_placement_non_category ON advertisement_placements (advertisement_id, placement_type) WHERE category_id IS NULL;
CREATE UNIQUE INDEX uk_ad_placement_category ON advertisement_placements (advertisement_id, placement_type, category_id) WHERE category_id IS NOT NULL;
CREATE INDEX idx_ad_eligibility ON advertisements (status, start_at, end_at);
CREATE INDEX idx_ad_placement_type ON advertisement_placements (placement_type);
CREATE INDEX idx_ad_placement_category ON advertisement_placements (category_id);
CREATE INDEX idx_ad_placement_ad ON advertisement_placements (advertisement_id);
