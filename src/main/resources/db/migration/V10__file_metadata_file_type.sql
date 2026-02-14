-- File type: BILL_OF_SALE, CONDITION_REPORT, or MISCELLANEOUS
ALTER TABLE file_metadata ADD COLUMN file_type VARCHAR(50) NOT NULL DEFAULT 'MISCELLANEOUS';
ALTER TABLE file_metadata ADD CONSTRAINT chk_file_metadata_file_type
    CHECK (file_type IN ('BILL_OF_SALE', 'CONDITION_REPORT', 'MISCELLANEOUS'));
CREATE INDEX idx_file_metadata_file_type ON file_metadata (file_type);
