-- Invites are for a specific dealership
ALTER TABLE invites ADD COLUMN dealership_id UUID REFERENCES dealerships (id) ON DELETE CASCADE;
UPDATE invites SET dealership_id = 'c0000000-0000-0000-0000-000000000001' WHERE dealership_id IS NULL;
ALTER TABLE invites ALTER COLUMN dealership_id SET NOT NULL;
CREATE INDEX idx_invites_dealership_id ON invites (dealership_id);
