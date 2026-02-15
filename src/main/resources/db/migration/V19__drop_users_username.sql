-- Username is now derived from first_name + last_name; remove stored column
ALTER TABLE users DROP CONSTRAINT IF EXISTS uq_users_username;
DROP INDEX IF EXISTS idx_users_username;
ALTER TABLE users DROP COLUMN IF EXISTS username;
