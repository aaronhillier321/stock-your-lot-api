-- File metadata for PDFs (and other files) stored in GCS, scoped by dealership and purchase.
CREATE TABLE file_metadata (
    id            UUID PRIMARY KEY,
    purchase_id   UUID NOT NULL REFERENCES purchases (id) ON DELETE CASCADE,
    dealership_id UUID NOT NULL REFERENCES dealerships (id) ON DELETE CASCADE,
    file_name     VARCHAR(255) NOT NULL,
    bucket        VARCHAR(255) NOT NULL,
    object_path   VARCHAR(512) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size_bytes    BIGINT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_file_metadata_purchase_id ON file_metadata (purchase_id);
CREATE INDEX idx_file_metadata_dealership_id ON file_metadata (dealership_id);
CREATE INDEX idx_file_metadata_created_at ON file_metadata (created_at);
