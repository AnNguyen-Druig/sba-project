-- Read-only verification after all phase scripts. Demo assertions run only if demo exists.
BEGIN;
SET LOCAL search_path=rental,pg_catalog;
DO $$ DECLARE n integer; BEGIN
 SELECT count(*) INTO n FROM pg_tables WHERE schemaname='rental';
 IF n<>(CASE WHEN to_regclass('rental.threads') IS NOT NULL THEN 34 WHEN to_regclass('rental.contract_amendments') IS NOT NULL THEN 33 ELSE 32 END) THEN
  RAISE EXCEPTION 'Expected 32/33/34 tables by installed phase, got %',n; END IF;
 IF EXISTS(SELECT 1 FROM pg_tables WHERE schemaname='rental' AND tablename<>ALL(ARRAY[
 'users','roles','user_roles','owner_managers','branches','branch_policies','room_types','rooms','room_slots','appointments','bookings','contracts','contract_tenants',
 'resource_allocations','room_slot_occupancies','services','room_services','contract_services','room_service_charges','cost_allocation_proposals','furnitures','furniture_warranties',
 'report_requests','report_requests_services','report_requests_furnitures','report_records','invoices','payments','payment_webhook_events','invoice_items','notifications','audit_logs','contract_amendments','threads'])) THEN
  RAISE EXCEPTION 'Table outside approved 34-table list'; END IF;
 IF (SELECT count(*) FROM roles)<>5 OR EXISTS(SELECT 1 FROM roles WHERE code='GUEST') THEN RAISE EXCEPTION 'Role catalog mismatch'; END IF;
 IF EXISTS(SELECT 1 FROM pg_constraint WHERE connamespace='rental'::regnamespace AND NOT convalidated) THEN RAISE EXCEPTION 'Unvalidated constraint'; END IF;
 IF EXISTS(SELECT 1 FROM pg_constraint WHERE connamespace='rental'::regnamespace AND contype='f' AND confdeltype='c') THEN RAISE EXCEPTION 'Unexpected DELETE CASCADE'; END IF;
 IF NOT EXISTS(SELECT 1 FROM pg_indexes WHERE schemaname='rental' AND indexname='uq_member_month_invoice' AND indexdef LIKE '%WHERE%') THEN RAISE EXCEPTION 'Missing member/month partial unique index'; END IF;
 IF invoice_due_date('2027-02-01',31,3)<>'2027-03-03'::date OR invoice_due_date('2028-02-01',31,3)<>'2028-03-03'::date THEN RAISE EXCEPTION 'Month-end/leap-year due date calculation'; END IF;
 IF EXISTS(SELECT 1 FROM users WHERE id=md5('rental-demo:owner-a')::uuid) THEN
  IF NOT can_manage_branch(md5('rental-demo:manager-a')::uuid,md5('rental-demo:branch-a2')::uuid)
   OR can_manage_branch(md5('rental-demo:manager-b')::uuid,md5('rental-demo:branch-a')::uuid) THEN RAISE EXCEPTION 'Manager scope helper failed'; END IF;
  IF NOT can_read_invoice(md5('rental-demo:tenant-a')::uuid,md5('rental-demo:invoice-a')::uuid)
   OR can_read_invoice(md5('rental-demo:tenant-b')::uuid,md5('rental-demo:invoice-a')::uuid) THEN RAISE EXCEPTION 'Invoice ownership helper failed'; END IF;
  IF (SELECT amount FROM default_charge_allocation(md5('rental-demo:charge-nov')::uuid) WHERE contract_tenant_id=md5('rental-demo:member-a')::uuid)<>225000
   OR (SELECT amount FROM default_charge_allocation(md5('rental-demo:charge-nov')::uuid) WHERE contract_tenant_id=md5('rental-demo:member-b')::uuid)<>75000 THEN RAISE EXCEPTION '30:10 resident-day allocation failed'; END IF;
  IF (SELECT outstanding_amount FROM invoice_balances WHERE invoice_id=md5('rental-demo:invoice-a')::uuid)<>185000 THEN RAISE EXCEPTION 'Partial/third-party settlement failed'; END IF;
  IF (SELECT payment_status FROM invoice_balances WHERE invoice_id=md5('rental-demo:invoice-b')::uuid)<>'PAID'
   OR (SELECT payment_status FROM invoice_balances WHERE invoice_id=md5('rental-demo:invoice-a-last')::uuid)<>'PAID' THEN RAISE EXCEPTION 'Prepaid/overpayment settlement failed'; END IF;
  IF (SELECT unallocated_amount FROM prepaid_balances WHERE payment_id=md5('rental-demo:overpayment-b')::uuid)<>25000 THEN RAISE EXCEPTION 'Overpayment balance failed'; END IF;
  IF (SELECT last_remaining FROM prepaid_balances WHERE payment_id=md5('rental-demo:prepaid-b')::uuid)<>3000000 THEN RAISE EXCEPTION 'Unused last-month balance failed'; END IF;
  IF (SELECT count(*) FROM payments WHERE booking_id=md5('rental-demo:booking-a')::uuid)<>1 THEN RAISE EXCEPTION 'Booking transfer created a second receipt'; END IF;
  IF (SELECT sum(amount_received) FROM payments WHERE provider_account_key='DEMO')<>30200000 THEN RAISE EXCEPTION 'Demo cash total changed'; END IF;
  IF EXISTS(SELECT 1 FROM real_cash_by_branch WHERE branch_id IN (md5('rental-demo:branch-a')::uuid,md5('rental-demo:branch-a2')::uuid,md5('rental-demo:branch-b')::uuid)) THEN RAISE EXCEPTION 'Sandbox counted as real cash'; END IF;
  IF NOT EXISTS(SELECT 1 FROM audit_logs WHERE table_name='payments' AND entity_id=md5('rental-demo:hold-payment')::uuid AND action='UPDATE') THEN RAISE EXCEPTION 'Missing booking transfer audit'; END IF;
  IF NOT EXISTS(SELECT 1 FROM users WHERE id=md5('rental-demo:invited-user')::uuid AND status='INVITED' AND password_hash IS NULL AND email IS NULL AND phone IS NULL) THEN RAISE EXCEPTION 'Offline invited User missing'; END IF;
  IF can_read_invoice(md5('rental-demo:invited-user')::uuid,md5('rental-demo:invoice-a')::uuid) THEN RAISE EXCEPTION 'Invited profile acquired invoice access'; END IF;
  IF (SELECT count(DISTINCT owner_id) FROM contract_tenants WHERE tenant_user_id=md5('rental-demo:tenant-a')::uuid)<>2 THEN RAISE EXCEPTION 'Same User cannot rent across Owners'; END IF;
  IF NOT EXISTS(SELECT 1 FROM audit_logs WHERE table_name='users' AND entity_id=md5('rental-demo:owner-a')::uuid AND detail->'after'->>'owner_verification_status'='APPROVED') THEN RAISE EXCEPTION 'Owner decision not audited'; END IF;
  IF EXISTS(SELECT 1 FROM audit_logs WHERE table_name='users' AND (detail::text LIKE '%secret_hash%' OR detail::text LIKE '%refresh_token%' OR detail::text LIKE '%password_hash%' OR detail::text LIKE '%'||repeat('a',64)||'%' OR detail::text LIKE '%'||repeat('b',64)||'%')) THEN RAISE EXCEPTION 'Authentication secret leaked into audit'; END IF;
 END IF;
END $$;
SELECT 'PASS: metadata and applicable demo assertions' AS verification;
SELECT table_name,column_name,data_type,is_nullable FROM information_schema.columns WHERE table_schema='rental' ORDER BY table_name,ordinal_position;
SELECT tablename,indexname,indexdef FROM pg_indexes WHERE schemaname='rental' ORDER BY tablename,indexname;
SELECT conrelid::regclass AS table_name,conname,contype,pg_get_constraintdef(oid) AS definition FROM pg_constraint WHERE connamespace='rental'::regnamespace ORDER BY conrelid::regclass::text,conname;
SELECT * FROM invoice_balances ORDER BY billing_month,invoice_id;
SELECT * FROM prepaid_balances ORDER BY payment_id;
COMMIT;
