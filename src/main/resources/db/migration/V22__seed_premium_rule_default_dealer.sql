-- Seed default premium (service fee) rule for dealers: flat $300.
INSERT INTO premium_rules (id, rule_name, amount, premium_type, created_at, updated_at)
VALUES (gen_random_uuid(), 'DEFAULT_DEALER', 300, 'FLAT', NOW(), NOW());
