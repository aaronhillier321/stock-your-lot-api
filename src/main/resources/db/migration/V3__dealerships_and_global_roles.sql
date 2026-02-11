-- Dealerships table
CREATE TABLE dealerships (
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city          VARCHAR(100),
    state         VARCHAR(50),
    postal_code   VARCHAR(20),
    phone         VARCHAR(50),
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);

-- Default dealership for signups and assignments
INSERT INTO dealerships (id, name, address_line1, city, state, postal_code, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Default Dealership', NULL, NULL, NULL, NULL, NOW(), NOW());

-- Dealership membership: user + dealership + role at that dealership (ASSOCIATE or ADMIN)
CREATE TABLE dealership_users (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL,
    dealership_id UUID NOT NULL,
    role          VARCHAR(20) NOT NULL DEFAULT 'ASSOCIATE',
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_dealership_users_user_dealership UNIQUE (user_id, dealership_id),
    CONSTRAINT chk_dealership_users_role CHECK (role IN ('ASSOCIATE', 'ADMIN')),
    CONSTRAINT fk_dealership_users_user      FOREIGN KEY (user_id)      REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_dealership_users_dealership FOREIGN KEY (dealership_id) REFERENCES dealerships (id) ON DELETE CASCADE
);

CREATE INDEX idx_dealership_users_user_id ON dealership_users (user_id);
CREATE INDEX idx_dealership_users_dealership_id ON dealership_users (dealership_id);

-- Replace old roles with new global roles: USER (default), SALES_ASSOCIATE, SALES_ADMIN
DELETE FROM user_roles;
DELETE FROM roles WHERE name IN ('ADMIN', 'DEALER', 'ASSOCIATE');

INSERT INTO roles (id, name, created_at, updated_at) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'USER',           NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000002', 'SALES_ASSOCIATE', NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000003', 'SALES_ADMIN',    NOW(), NOW());

-- Give every existing user the default global role USER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'USER';
