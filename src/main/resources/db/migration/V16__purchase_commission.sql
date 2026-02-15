-- Commission amounts applied to a purchase (one-to-many: one sale can credit multiple users).
-- Amount is stored flat; percentage-based rules are converted to dollar amount at sale time.
CREATE TABLE purchase_commission (
    id           UUID PRIMARY KEY,
    purchase_id  UUID NOT NULL,
    user_id      UUID NOT NULL,
    rule_id      UUID NULL,
    amount       DECIMAL(12, 2) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_purchase_commission_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_commission_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_commission_rule FOREIGN KEY (rule_id) REFERENCES commission_rules (id) ON DELETE SET NULL
);

CREATE INDEX idx_purchase_commission_purchase_id ON purchase_commission (purchase_id);
CREATE INDEX idx_purchase_commission_user_id ON purchase_commission (user_id);
CREATE INDEX idx_purchase_commission_rule_id ON purchase_commission (rule_id);
