CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    code VARCHAR(80) NOT NULL,
    description VARCHAR(500),
    system_role BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_roles_code UNIQUE (code),
    CONSTRAINT uk_roles_name UNIQUE (name),
    CONSTRAINT ck_roles_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_roles_code CHECK (code = upper(code) AND code ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT ck_roles_name_not_blank CHECK (length(trim(name)) > 0)
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500) NOT NULL,
    category VARCHAR(80) NOT NULL,
    system_permission BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_permissions_code UNIQUE (code),
    CONSTRAINT ck_permissions_code CHECK (code = upper(code) AND code ~ '^[A-Z][A-Z0-9_]*$')
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE RESTRICT
);

CREATE TABLE staff_setup_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_staff_setup_tokens_hash UNIQUE (token_hash),
    CONSTRAINT fk_staff_setup_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE admin_audit_log (
    id UUID PRIMARY KEY,
    actor_user_id UUID,
    action VARCHAR(80) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id UUID,
    metadata TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_admin_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_user_roles_role ON user_roles (role_id);
CREATE INDEX idx_role_permissions_permission ON role_permissions (permission_id);
CREATE INDEX idx_staff_setup_tokens_user ON staff_setup_tokens (user_id);
CREATE INDEX idx_admin_audit_target ON admin_audit_log (target_type, target_id);

INSERT INTO roles (id, name, code, description, system_role, status, created_at, updated_at) VALUES
    ('00000000-0000-0000-0000-000000000001', 'User', 'USER', 'Normal registered platform user.', TRUE, 'ACTIVE', now(), now()),
    ('00000000-0000-0000-0000-000000000002', 'Super Administrator', 'SUPER_ADMIN', 'Protected bootstrap administrator with every administrative permission.', TRUE, 'ACTIVE', now(), now());

INSERT INTO permissions (id, code, name, description, category, system_permission, created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000001', 'STORY_VIEW_ADMIN', 'View Story Administration', 'Access story management screens.', 'Stories', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000002', 'STORY_CREATE', 'Create Stories', 'Create new stories.', 'Stories', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000003', 'STORY_EDIT', 'Edit Stories', 'Edit existing stories.', 'Stories', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000004', 'STORY_DELETE', 'Delete Stories', 'Delete stories from administration.', 'Stories', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000005', 'STORY_PUBLISH', 'Publish Stories', 'Publish or unpublish stories.', 'Stories', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000006', 'BREAKING_NEWS_MANAGE', 'Manage Breaking News', 'Mark or unmark stories as breaking news.', 'Breaking News', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000007', 'CATEGORY_MANAGE', 'Manage Categories', 'Create and manage story categories.', 'Categories & Tags', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000008', 'TAG_MANAGE', 'Manage Tags', 'Create and manage story tags.', 'Categories & Tags', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000009', 'COMMENT_MODERATE', 'Moderate Comments', 'Hide, restore, delete, and review reported comments.', 'Comments', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000010', 'NEWSPAPER_VIEW_ADMIN', 'View Newspaper Administration', 'Access newspaper administration.', 'Newspaper', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000011', 'NEWSPAPER_UPLOAD', 'Upload Newspaper', 'Upload newspaper editions.', 'Newspaper', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000012', 'NEWSPAPER_EDIT', 'Edit Newspaper', 'Edit newspaper editions.', 'Newspaper', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000013', 'NEWSPAPER_PUBLISH', 'Publish Newspaper', 'Publish or unpublish newspaper editions.', 'Newspaper', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000014', 'NEWSPAPER_DELETE', 'Delete Newspaper', 'Delete newspaper editions.', 'Newspaper', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000015', 'AD_VIEW_ADMIN', 'View Advertising Administration', 'Access advertising administration.', 'Advertising', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000016', 'AD_CREATE', 'Create Ads', 'Create advertising campaigns.', 'Advertising', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000017', 'AD_EDIT', 'Edit Ads', 'Edit advertising campaigns.', 'Advertising', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000018', 'AD_PUBLISH', 'Publish Ads', 'Publish advertising campaigns.', 'Advertising', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000019', 'AD_PAUSE', 'Pause Ads', 'Pause active advertising campaigns.', 'Advertising', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000020', 'AD_DELETE', 'Delete Ads', 'Delete advertising campaigns.', 'Advertising', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000021', 'STAFF_VIEW', 'View Staff', 'View staff users and their status.', 'Staff', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000022', 'STAFF_CREATE', 'Create Staff', 'Create staff users.', 'Staff', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000023', 'STAFF_EDIT', 'Edit Staff', 'Edit staff user details.', 'Staff', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000024', 'STAFF_DISABLE', 'Enable or Disable Staff', 'Enable or disable staff users.', 'Staff', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000025', 'STAFF_ROLE_ASSIGN', 'Assign Staff Roles', 'Assign roles to staff users.', 'Staff', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000026', 'ROLE_VIEW', 'View Roles', 'View roles and permissions.', 'Roles & Permissions', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000027', 'ROLE_CREATE', 'Create Roles', 'Create custom roles.', 'Roles & Permissions', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000028', 'ROLE_EDIT', 'Edit Roles', 'Edit custom roles.', 'Roles & Permissions', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000029', 'ROLE_DELETE', 'Delete Roles', 'Delete unused custom roles.', 'Roles & Permissions', TRUE, now(), now()),
    ('10000000-0000-0000-0000-000000000030', 'ROLE_PERMISSION_ASSIGN', 'Assign Role Permissions', 'Assign existing permissions to roles.', 'Roles & Permissions', TRUE, now(), now());

INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000002', id FROM permissions;

INSERT INTO user_roles (user_id, role_id)
SELECT id,
       CASE WHEN role = 'ADMIN' THEN '00000000-0000-0000-0000-000000000002'::uuid
            ELSE '00000000-0000-0000-0000-000000000001'::uuid END
FROM users;

ALTER TABLE users DROP CONSTRAINT ck_users_role;
ALTER TABLE users DROP COLUMN role;
ALTER TABLE users DROP CONSTRAINT ck_users_status;
ALTER TABLE users ADD CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'PENDING_SETUP'));
