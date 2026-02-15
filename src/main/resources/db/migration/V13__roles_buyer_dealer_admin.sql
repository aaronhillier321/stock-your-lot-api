-- Roles are now BUYER, DEALER, ADMIN. No default; role must be sent on register.
-- Remove existing role assignments and old roles.
DELETE FROM user_roles;
DELETE FROM roles WHERE name IN ('USER', 'SALES_ASSOCIATE', 'SALES_ADMIN', 'ADMIN', 'DEALER', 'ASSOCIATE');

-- Insert the three roles (fixed IDs for reference).
INSERT INTO roles (id, name, created_at, updated_at) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'BUYER',  NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000002', 'DEALER',  NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000003', 'ADMIN',   NOW(), NOW());

-- Assign BUYER to every existing user.
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.name = 'BUYER';
