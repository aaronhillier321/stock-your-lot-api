-- Delete rows where any of the soon-to-be-required fields are null.
DELETE FROM purchases
WHERE vin IS NULL
   OR auction_platform IS NULL
   OR purchase_date IS NULL
   OR dealership_id IS NULL
   OR purchase_price IS NULL;

-- Require these fields.
ALTER TABLE purchases ALTER COLUMN vin SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN auction_platform SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN purchase_date SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN dealership_id SET NOT NULL;
ALTER TABLE purchases ALTER COLUMN purchase_price SET NOT NULL;
