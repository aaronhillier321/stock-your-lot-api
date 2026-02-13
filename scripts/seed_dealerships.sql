-- Seed 10 dealerships with realistic US data.
-- Run against your DB (e.g. via Cloud SQL Proxy). Idempotent: uses INSERT ... ON CONFLICT DO NOTHING on id.

INSERT INTO dealerships (id, name, address_line1, address_line2, city, state, postal_code, phone, created_at, updated_at)
VALUES
    ('a1000001-0000-0000-0000-000000000001', 'Sunrise Auto Group', '4200 West Main Street', NULL, 'Phoenix', 'AZ', '85001', '(602) 555-0101', NOW(), NOW()),
    ('a1000002-0000-0000-0000-000000000002', 'Riverside Chevrolet', '1500 Commerce Drive', 'Suite 100', 'Riverside', 'CA', '92501', '(951) 555-0202', NOW(), NOW()),
    ('a1000003-0000-0000-0000-000000000003', 'Lone Star Ford', '2800 Interstate 35', NULL, 'Austin', 'TX', '78701', '(512) 555-0303', NOW(), NOW()),
    ('a1000004-0000-0000-0000-000000000004', 'Mountain View Honda', '8900 State Highway 9', NULL, 'Denver', 'CO', '80202', '(303) 555-0404', NOW(), NOW()),
    ('a1000005-0000-0000-0000-000000000005', 'Lakefront Toyota', '2100 East Lake Shore Drive', NULL, 'Chicago', 'IL', '60601', '(312) 555-0505', NOW(), NOW()),
    ('a1000006-0000-0000-0000-000000000006', 'Peachtree Nissan', '4500 Peachtree Industrial Blvd', NULL, 'Atlanta', 'GA', '30301', '(404) 555-0606', NOW(), NOW()),
    ('a1000007-0000-0000-0000-000000000007', 'Desert Valley Chrysler', '3200 North Scottsdale Road', NULL, 'Scottsdale', 'AZ', '85251', '(480) 555-0707', NOW(), NOW()),
    ('a1000008-0000-0000-0000-000000000008', 'Pacific Coast Hyundai', '1800 Pacific Coast Highway', NULL, 'Long Beach', 'CA', '90802', '(562) 555-0808', NOW(), NOW()),
    ('a1000009-0000-0000-0000-000000000009', 'Metro Kia', '5600 Northwest Expressway', NULL, 'Oklahoma City', 'OK', '73132', '(405) 555-0909', NOW(), NOW()),
    ('a100000a-0000-0000-0000-00000000000a', 'Green Valley Subaru', '7700 South Las Vegas Blvd', 'Building B', 'Las Vegas', 'NV', '89123', '(702) 555-1010', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
