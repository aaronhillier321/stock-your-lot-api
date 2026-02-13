-- Add dealership FK; remove free-text dealership column.
ALTER TABLE purchases ADD COLUMN dealership_id UUID REFERENCES dealerships (id) ON DELETE SET NULL;
CREATE INDEX idx_purchases_dealership_id ON purchases (dealership_id);
ALTER TABLE purchases DROP COLUMN IF EXISTS dealership;
