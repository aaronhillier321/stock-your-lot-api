-- Status: PENDING (from extract flow, not yet linked to purchase) or ACTIVE (linked to purchase).
ALTER TABLE file_metadata ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE file_metadata ADD CONSTRAINT chk_file_metadata_status
    CHECK (status IN ('PENDING', 'ACTIVE'));

-- Token linking pending uploads to a future purchase (UI sends same UUID with both extracts and create purchase).
ALTER TABLE file_metadata ADD COLUMN upload_token VARCHAR(36) NULL;
CREATE INDEX idx_file_metadata_upload_token ON file_metadata (upload_token);
CREATE INDEX idx_file_metadata_status ON file_metadata (status);

-- Pending rows have no purchase/dealership until claimed.
ALTER TABLE file_metadata ALTER COLUMN purchase_id DROP NOT NULL;
ALTER TABLE file_metadata ALTER COLUMN dealership_id DROP NOT NULL;
