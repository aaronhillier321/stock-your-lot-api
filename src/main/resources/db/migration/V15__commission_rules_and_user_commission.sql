-- Commission rules: reusable definition (flat amount or percentage).
CREATE TABLE commission_rules (
    id                UUID PRIMARY KEY,
    amount            DECIMAL(12, 4) NOT NULL,
    commission_type   VARCHAR(20) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_commission_rules_type CHECK (commission_type IN ('PERCENT', 'FLAT'))
);

-- User commission: links users to rules with effective period, level, and optional sales cap.
-- Expired on: (1) the Xth sale when number_of_sales = X, or (2) first sale after end_date.
CREATE TABLE user_commission (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL,
    rule_id          UUID NOT NULL,
    start_date       DATE NOT NULL,
    end_date         DATE NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    level            INTEGER NOT NULL DEFAULT 1,
    number_of_sales  INTEGER NULL,
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_user_commission_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_commission_rule FOREIGN KEY (rule_id) REFERENCES commission_rules (id) ON DELETE CASCADE,
    CONSTRAINT chk_user_commission_status CHECK (status IN ('ACTIVE', 'EXPIRED'))
);

CREATE INDEX idx_user_commission_user_id ON user_commission (user_id);
CREATE INDEX idx_user_commission_rule_id ON user_commission (rule_id);
CREATE INDEX idx_user_commission_status ON user_commission (status);
CREATE INDEX idx_user_commission_user_status ON user_commission (user_id, status);
