-- Allow invites that are not tied to a specific dealership.
ALTER TABLE invites ALTER COLUMN dealership_id DROP NOT NULL;

