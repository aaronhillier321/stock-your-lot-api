-- Purchase lifecycle status.
ALTER TABLE purchases ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED';
ALTER TABLE purchases ADD CONSTRAINT chk_purchases_status CHECK (status IN ('CONFIRMED', 'IN_TRANSPORT', 'PENDING_PAYMENT', 'PAID'));
