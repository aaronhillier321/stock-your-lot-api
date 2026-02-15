-- Required display name for a commission rule.
ALTER TABLE commission_rules ADD COLUMN rule_name VARCHAR(100) NOT NULL DEFAULT '';
