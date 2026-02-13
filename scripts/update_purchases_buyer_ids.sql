-- Set buyer_id on all purchases to a random existing user.
-- Run only if you have at least one user in the users table.
-- Example: psql "host=127.0.0.1 port=5433 user=postgres dbname=stock-your-lot" -f scripts/update_purchases_buyer_ids.sql

UPDATE purchases p
SET buyer_id = (
  SELECT id FROM users
  ORDER BY random()
  LIMIT 1
);

-- Optional: show how many purchases each user has
-- SELECT u.email, COUNT(p.id) AS purchase_count
-- FROM users u
-- LEFT JOIN purchases p ON p.buyer_id = u.id
-- GROUP BY u.id, u.email
-- ORDER BY purchase_count DESC;
