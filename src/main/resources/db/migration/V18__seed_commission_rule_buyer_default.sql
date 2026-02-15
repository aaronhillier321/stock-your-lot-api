-- Seed default commission rule for buyers: flat $100.
INSERT INTO commission_rules (id, rule_name, amount, commission_type, created_at, updated_at)
VALUES (gen_random_uuid(), 'BUYER_DEFAULT', 100, 'FLAT', NOW(), NOW());
