-- LOCAL ONLY, after Flyway V3. Fixed IDs match the invoice-demo authentication profile.
-- Database accounts are locked placeholders, NOT usable passwords or production users.
-- Run once in a fresh local database. Any duplicate/error rolls back the transaction.
BEGIN;
DO $local$
BEGIN
    IF current_database() NOT LIKE 'sba_uuid_%' THEN
        RAISE EXCEPTION 'Demo seed only supports local databases named sba_uuid_*';
    END IF;
END
$local$;
INSERT INTO public.accounts (id, username, password_hash, role, status, full_name)
VALUES
('10000000-0000-0000-0000-000000000001', 'invoice-demo-owner', '!', 'OWNER', 'LOCKED', 'Demo Owner'),
('10000000-0000-0000-0000-000000000002', 'invoice-demo-manager', '!', 'MANAGER', 'LOCKED', 'Demo Manager');

INSERT INTO public.branches (id, owner_id, code, name, status)
VALUES ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
        'INVOICE-DEMO', 'Invoice review branch', 'ACTIVE');
INSERT INTO public.manager_branch_assignments (id, branch_id, manager_id, is_active, assigned_at)
VALUES ('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000002', true, CURRENT_TIMESTAMP);

INSERT INTO public.contracts
(id, version, branch_id, room_id, rental_mode, status, start_date, end_date, monthly_rent, payment_day)
VALUES ('30000000-0000-0000-0000-000000000001', 0, '20000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001', 'WHOLE_ROOM', 'ACTIVE',
        DATE '2026-01-01', DATE '2026-12-31', 3000000, 1);
INSERT INTO public.contract_tenants (id, contract_id, tenant_id, joined_date, representative)
VALUES
('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
 '60000000-0000-0000-0000-000000000001', DATE '2026-01-01', true),
('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
 '60000000-0000-0000-0000-000000000002', DATE '2026-01-01', false),
('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001',
 '60000000-0000-0000-0000-000000000003', DATE '2026-01-01', false);
COMMIT;
