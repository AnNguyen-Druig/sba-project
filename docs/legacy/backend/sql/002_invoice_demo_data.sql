-- Optional fixtures for a disposable development database. Run once after 001.
-- Fixed account UUIDs match the invoice-demo profile; no passwords are stored here.
BEGIN;
INSERT INTO public.branches (id, owner_id, manager_id, name) VALUES
('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
 '10000000-0000-0000-0000-000000000002', 'Invoice review branch');
INSERT INTO public.contracts
(id, version, branch_id, room_id, rental_mode, status, start_date, end_date, monthly_rent, payment_day)
VALUES
('30000000-0000-0000-0000-000000000001', 0, '20000000-0000-0000-0000-000000000001',
 '40000000-0000-0000-0000-000000000001', 'WHOLE_ROOM', 'ACTIVE',
 DATE '2026-01-01', DATE '2026-12-31', 3000000, 1);
INSERT INTO public.contract_tenants (id, contract_id, tenant_id, joined_date, representative) VALUES
('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
 '60000000-0000-0000-0000-000000000001', DATE '2026-01-01', true),
('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
 '60000000-0000-0000-0000-000000000002', DATE '2026-01-01', false),
('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001',
 '60000000-0000-0000-0000-000000000003', DATE '2026-01-01', false);
COMMIT;
