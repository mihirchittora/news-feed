ALTER TABLE categories
    ADD COLUMN parent_id UUID,
    ADD CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE RESTRICT,
    ADD CONSTRAINT ck_categories_not_self_parent CHECK (parent_id IS NULL OR parent_id <> id);

DROP INDEX uk_categories_name_lower;
CREATE UNIQUE INDEX uk_categories_parent_name_lower
    ON categories (COALESCE(parent_id, '00000000-0000-0000-0000-000000000000'::uuid), lower(name));

CREATE INDEX idx_categories_parent ON categories (parent_id);
CREATE INDEX idx_categories_status_order ON categories (status, parent_id, display_order, name);
