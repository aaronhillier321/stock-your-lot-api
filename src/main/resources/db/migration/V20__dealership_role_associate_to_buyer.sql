-- Rename dealership role ASSOCIATE to BUYER: drop constraint first, then update data, then re-add
ALTER TABLE dealership_users DROP CONSTRAINT chk_dealership_users_role;

UPDATE dealership_users SET role = 'BUYER' WHERE role = 'ASSOCIATE';

ALTER TABLE dealership_users ADD CONSTRAINT chk_dealership_users_role CHECK (role IN ('BUYER', 'ADMIN'));
ALTER TABLE dealership_users ALTER COLUMN role SET DEFAULT 'BUYER';
