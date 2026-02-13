-- Purchases: entered by a buyer (user). buyer_id = who is logged in when creating.
CREATE TABLE purchases (
    id                   UUID PRIMARY KEY,
    buyer_id              UUID NOT NULL,
    dealership            VARCHAR(255),
    purchase_date         DATE,
    auction_platform      VARCHAR(100),
    vin                   VARCHAR(17),
    ymmt                  VARCHAR(255),
    miles                 INTEGER,
    purchase_price        DECIMAL(12, 2),
    vehicle_year          VARCHAR(10),
    vehicle_make          VARCHAR(100),
    vehicle_model         VARCHAR(100),
    vehicle_trim_level    VARCHAR(100),
    transport_quote       DECIMAL(12, 2),
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_purchases_buyer FOREIGN KEY (buyer_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_purchases_buyer_id ON purchases (buyer_id);
CREATE INDEX idx_purchases_created_at ON purchases (created_at);
