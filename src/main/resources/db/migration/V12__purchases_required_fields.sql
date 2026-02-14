-- Require VIN, auction platform, purchase date, dealership, and purchase price on purchases.
ALTER TABLE purchases ALTER COLUMN vin SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN auction_platform SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN purchase_date SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN dealership_id SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN purchase_price SET NOT NULL;
