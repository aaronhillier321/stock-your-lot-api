-- Premium (service fee) rules: reusable definition (flat amount or percentage).
CREATE TABLE premium_rules (
    id                UUID PRIMARY KEY,
    rule_name         VARCHAR(100) NOT NULL,
    amount            DECIMAL(12, 4) NOT NULL,
    premium_type      VARCHAR(20) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_premium_rules_type CHECK (premium_type IN ('PERCENT', 'FLAT'))
);

-- Dealer premium: links dealerships to rules with effective period, level, and optional sales cap.
CREATE TABLE dealer_premium (
    id               UUID PRIMARY KEY,
    dealership_id    UUID NOT NULL,
    rule_id          UUID NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    level            INTEGER NOT NULL DEFAULT 1,
    number_of_sales  INTEGER NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_dealer_premium_dealership FOREIGN KEY (dealership_id) REFERENCES dealerships (id) ON DELETE CASCADE,
    CONSTRAINT fk_dealer_premium_rule FOREIGN KEY (rule_id) REFERENCES premium_rules (id) ON DELETE CASCADE,
    CONSTRAINT chk_dealer_premium_status CHECK (status IN ('ACTIVE', 'EXPIRED'))
);

CREATE INDEX idx_dealer_premium_dealership_id ON dealer_premium (dealership_id);
CREATE INDEX idx_dealer_premium_rule_id ON dealer_premium (rule_id);
CREATE INDEX idx_dealer_premium_status ON dealer_premium (status);
CREATE INDEX idx_dealer_premium_dealership_status ON dealer_premium (dealership_id, status);

-- Premium amount applied to a purchase (one per purchase for the dealership's service fee).
CREATE TABLE purchase_premium (
    id             UUID PRIMARY KEY,
    purchase_id    UUID NOT NULL,
    dealership_id  UUID NOT NULL,
    rule_id        UUID NULL,
    amount         DECIMAL(12, 2) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_purchase_premium_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_premium_dealership FOREIGN KEY (dealership_id) REFERENCES dealerships (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_premium_rule FOREIGN KEY (rule_id) REFERENCES premium_rules (id) ON DELETE SET NULL
);

CREATE INDEX idx_purchase_premium_purchase_id ON purchase_premium (purchase_id);
CREATE INDEX idx_purchase_premium_dealership_id ON purchase_premium (dealership_id);
CREATE INDEX idx_purchase_premium_rule_id ON purchase_premium (rule_id);
