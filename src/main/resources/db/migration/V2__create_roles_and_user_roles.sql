-- Roles table (Admin, Dealer, Associate)
CREATE TABLE roles (
    id         UUID PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_roles_name UNIQUE (name)
);

-- Many-to-many: users <-> roles
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles (user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

-- Seed the three roles
INSERT INTO roles (id, name, created_at, updated_at) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ADMIN',    NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000002', 'DEALER',   NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000003', 'ASSOCIATE', NOW(), NOW());

-- Migrate existing users: assign ASSOCIATE to users who had role 'USER' (or any existing role)
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'ASSOCIATE';

-- Remove old role column from users
ALTER TABLE users DROP COLUMN role;
