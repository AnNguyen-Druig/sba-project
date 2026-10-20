-- Development only, AFTER demo seed. Every test and helper is rolled back.
-- Each negative test must fail with an integrity error, NOT arbitrary SQL failure.
BEGIN;
SET LOCAL search_path=rental,pg_catalog;
CREATE OR REPLACE FUNCTION pg_temp.demo_id(label text) RETURNS uuid LANGUAGE sql IMMUTABLE AS $$ SELECT md5('rental-demo:'||label)::uuid $$;
SELECT set_config('rental.actor_id',pg_temp.demo_id('owner-a')::text,true);
SELECT set_config('rental.reason','Rolled-back negative test fixture',true);
SELECT set_config('rental.request_key','negative-test-request',true);
CREATE TEMP TABLE negative_results(label text PRIMARY KEY, result text NOT NULL, sqlstate text, message text) ON COMMIT DROP;
CREATE OR REPLACE FUNCTION pg_temp.expect_integrity_failure(label text, command text) RETURNS void LANGUAGE plpgsql AS $$
DECLARE rejected boolean:=false; state text; msg text;
BEGIN
 BEGIN
  EXECUTE command;
  SET CONSTRAINTS ALL IMMEDIATE;
 EXCEPTION WHEN integrity_constraint_violation THEN
  rejected:=true; GET STACKED DIAGNOSTICS state=RETURNED_SQLSTATE,msg=MESSAGE_TEXT;
 END;
 IF NOT rejected THEN RAISE EXCEPTION 'NEGATIVE TEST FAILED: % was accepted',label; END IF;
 SET CONSTRAINTS ALL DEFERRED;
 INSERT INTO negative_results VALUES(label,'PASS',state,msg);
END $$;
SELECT pg_temp.expect_integrity_failure('N01 duplicate user role',$q$INSERT INTO user_roles(user_id,role_id) SELECT user_id,role_id FROM user_roles LIMIT 1$q$);
SELECT pg_temp.expect_integrity_failure('N02 manager belongs to only one owner',$q$INSERT INTO owner_managers(owner_id,manager_id,assigned_by) VALUES(pg_temp.demo_id('owner-b'),pg_temp.demo_id('manager-a'),pg_temp.demo_id('owner-b'))$q$);
SELECT pg_temp.expect_integrity_failure('N03 branch manager wrong owner',$q$UPDATE branches SET manager_id=pg_temp.demo_id('manager-b') WHERE id=pg_temp.demo_id('branch-a')$q$);
SELECT pg_temp.expect_integrity_failure('N04 active branch requires manager',$q$UPDATE branches SET manager_id=NULL WHERE id=pg_temp.demo_id('branch-a')$q$);
SELECT pg_temp.expect_integrity_failure('N05 signed tenant identity cannot change',$q$UPDATE contract_tenants SET tenant_user_id=pg_temp.demo_id('tenant-b') WHERE id=pg_temp.demo_id('member-a')$q$);
SELECT pg_temp.expect_integrity_failure('N06 duplicate representative',$q$UPDATE contract_tenants SET role='REPRESENTATIVE' WHERE id=pg_temp.demo_id('member-b')$q$);
SELECT pg_temp.expect_integrity_failure('N07 missing representative',$q$UPDATE contract_tenants SET role='MEMBER' WHERE id=pg_temp.demo_id('member-a')$q$);
SELECT pg_temp.expect_integrity_failure('N08 double lease same slot',$q$INSERT INTO resource_allocations(room_id,room_slot_id,contract_id,contract_tenant_id,start_date,end_date,status) SELECT room_id,room_slot_id,contract_id,contract_tenant_id,start_date,end_date,status FROM resource_allocations WHERE contract_tenant_id=pg_temp.demo_id('member-a')$q$);
SELECT pg_temp.expect_integrity_failure('N09 whole-room hold conflicts with slot lease',$q$INSERT INTO resource_allocations(room_id,booking_id,start_date,end_date,expires_at,status) VALUES(pg_temp.demo_id('room-slot'),pg_temp.demo_id('booking-a'),'2026-11-01','2027-05-01',now()+interval '1 hour','HELD')$q$);
SELECT pg_temp.expect_integrity_failure('N10 overlapping occupancy',$q$INSERT INTO room_slot_occupancies(room_id,room_slot_id,contract_tenant_id,start_date,end_date) SELECT room_id,room_slot_id,contract_tenant_id,start_date,end_date FROM room_slot_occupancies LIMIT 1$q$);
SELECT pg_temp.expect_integrity_failure('N11 expired booking cannot revive',$q$UPDATE bookings SET status='CONFIRMED' WHERE id=pg_temp.demo_id('booking-expired')$q$);
SELECT pg_temp.expect_integrity_failure('N12 late payment cannot be allocated',$q$UPDATE payments SET allocated_amount=1000000 WHERE id=pg_temp.demo_id('late-payment')$q$);
SELECT pg_temp.expect_integrity_failure('N13 invoice one per member month',$q$INSERT INTO invoices(contract_tenant_id,billing_month,due_date,policy_snapshot,document_status,payment_environment,subtotal_amount,adjustment_amount,total_amount,created_by) SELECT contract_tenant_id,billing_month,due_date,policy_snapshot,'DRAFT',payment_environment,subtotal_amount,adjustment_amount,total_amount,created_by FROM invoices WHERE id=pg_temp.demo_id('invoice-a')$q$);
SELECT pg_temp.expect_integrity_failure('N14 issued invoice immutable',$q$UPDATE invoices SET total_amount=total_amount+1,subtotal_amount=subtotal_amount+1 WHERE id=pg_temp.demo_id('invoice-a')$q$);
SELECT pg_temp.expect_integrity_failure('N15 issued invoice item immutable',$q$UPDATE invoice_items SET description='unauthorized edit' WHERE invoice_id=pg_temp.demo_id('invoice-a') AND item_type='RENT'$q$);
SELECT pg_temp.expect_integrity_failure('N16 payment at most one direct target',$q$UPDATE payments SET invoice_id=pg_temp.demo_id('invoice-a') WHERE id=pg_temp.demo_id('hold-payment')$q$);
SELECT pg_temp.expect_integrity_failure('N17 receipt cannot be rewritten',$q$UPDATE payments SET amount_received=amount_received+100 WHERE id=pg_temp.demo_id('prepaid-a')$q$);
SELECT pg_temp.expect_integrity_failure('N18 webhook replay',$q$INSERT INTO payment_webhook_events(payment_id,provider,environment,provider_account_key,event_key,payload_hash,safe_payload,signature_verified,processing_status,processed_at) SELECT payment_id,provider,environment,provider_account_key,event_key,payload_hash,safe_payload,signature_verified,processing_status,processed_at FROM payment_webhook_events LIMIT 1$q$);
SELECT pg_temp.expect_integrity_failure('N19 spent prepaid cannot refund',$q$UPDATE payments SET prepaid_first_refunded=1000000 WHERE id=pg_temp.demo_id('hold-payment')$q$);
SELECT pg_temp.expect_integrity_failure('N20 prepaid wrong beneficiary',$q$INSERT INTO invoice_items(invoice_id,line_number,item_type,description,quantity,unit_price,amount,source_payment_id,prepaid_bucket,idempotency_key) VALUES(pg_temp.demo_id('invoice-a'),99,'PREPAID_APPLICATION','wrong member',1,100,100,pg_temp.demo_id('prepaid-b'),'FIRST_MONTH','negative-wrong-member')$q$);
SELECT pg_temp.expect_integrity_failure('N21 payment exceeds invoice debt',$q$UPDATE payments SET allocated_amount=100000 WHERE id=pg_temp.demo_id('overpayment-b')$q$);
SELECT pg_temp.expect_integrity_failure('N22 approved split immutable',$q$UPDATE cost_allocation_proposals SET allocation_details='[]' WHERE id=pg_temp.demo_id('proposal-nov')$q$);
SELECT pg_temp.expect_integrity_failure('N23 finalized utility immutable',$q$UPDATE room_service_charges SET unit_price=3100,total_amount=310000 WHERE id=pg_temp.demo_id('charge-nov')$q$);
SELECT pg_temp.expect_integrity_failure('N24 mandatory electricity cannot cancel',$q$UPDATE contract_services SET status='CANCELLED' WHERE room_service_id=pg_temp.demo_id('ELECTRIC-room')$q$);
SELECT pg_temp.expect_integrity_failure('N25 approved repair immutable',$q$UPDATE report_records SET amount=70000 WHERE id=pg_temp.demo_id('repair-tenant')$q$);
SELECT pg_temp.expect_integrity_failure('N26 audit append only',$q$UPDATE audit_logs SET reason='erase history' WHERE id=(SELECT id FROM audit_logs LIMIT 1)$q$);
SELECT pg_temp.expect_integrity_failure('N27 preserve payment history',$q$DELETE FROM payments WHERE id=pg_temp.demo_id('hold-payment')$q$);
SELECT pg_temp.expect_integrity_failure('N28 policy version immutable',$q$UPDATE branch_policies SET collection_day=10 WHERE id=pg_temp.demo_id('branch-a-policy')$q$);
SELECT pg_temp.expect_integrity_failure('N29 maintenance cannot overlap lease',$q$INSERT INTO resource_allocations(room_id,start_date,end_date,allocation_kind,block_reason,blocked_by,status) VALUES(pg_temp.demo_id('room-slot'),'2026-11-01','2026-11-02','MAINTENANCE_BLOCK','negative',pg_temp.demo_id('owner-a'),'BLOCKED')$q$);
SELECT pg_temp.expect_integrity_failure('N30 room capacity',$q$UPDATE rooms SET capacity=1 WHERE id=pg_temp.demo_id('room-slot')$q$);
SELECT pg_temp.expect_integrity_failure('N31 payment wrong environment',$q$INSERT INTO payments(branch_id,invoice_id,method,environment,status,amount_received,idempotency_key) VALUES(pg_temp.demo_id('branch-a'),pg_temp.demo_id('invoice-a'),'MANUAL_QR','REAL','PENDING',100,'negative-real-vs-sandbox')$q$);
SELECT pg_temp.expect_integrity_failure('N32 wrong manager cannot issue invoice',$q$UPDATE invoices SET issued_by=pg_temp.demo_id('manager-b') WHERE id=pg_temp.demo_id('invoice-a')$q$);
SELECT pg_temp.expect_integrity_failure('N33 service JSON total mismatch',$q$
 INSERT INTO room_service_charges(id,room_service_id,room_id,billing_month,start_date,end_date,segment_number,segment_reason,quantity,unit_price,total_amount,calculation_snapshot,status,finalized_by,finalized_at)
 VALUES(pg_temp.demo_id('negative-charge'),pg_temp.demo_id('ELECTRIC-room'),pg_temp.demo_id('room-slot'),'2026-12-01','2026-12-01','2027-01-01',1,'REGULAR',100,3000,300000,'{}','FINALIZED',pg_temp.demo_id('manager-a'),now());
 INSERT INTO cost_allocation_proposals(room_service_charge_id,method,allocation_details,calculation_snapshot,status,processed_by,processed_at)
 VALUES(pg_temp.demo_id('negative-charge'),'APPROVED_CUSTOM',jsonb_build_array(jsonb_build_object('contract_tenant_id',pg_temp.demo_id('member-a'),'amount',149999),jsonb_build_object('contract_tenant_id',pg_temp.demo_id('member-b'),'amount',150000)),'{}','APPROVED',pg_temp.demo_id('owner-a'),now());
$q$);
SELECT pg_temp.expect_integrity_failure('N34 no resident days cannot allocate',$q$
 INSERT INTO room_service_charges(id,room_service_id,room_id,billing_month,start_date,end_date,segment_number,segment_reason,quantity,unit_price,total_amount,calculation_snapshot,status,finalized_by,finalized_at)
 VALUES(pg_temp.demo_id('negative-charge'),pg_temp.demo_id('ELECTRIC-room'),pg_temp.demo_id('room-slot'),'2027-06-01','2027-06-01','2027-07-01',1,'REGULAR',100,3000,300000,'{}','FINALIZED',pg_temp.demo_id('manager-a'),now());
 INSERT INTO cost_allocation_proposals(room_service_charge_id,method,allocation_details,calculation_snapshot,status,processed_by,processed_at)
 VALUES(pg_temp.demo_id('negative-charge'),'RESIDENT_DAYS','[]','{}','APPROVED',pg_temp.demo_id('owner-a'),now());
$q$);
SELECT pg_temp.expect_integrity_failure('N35 contract cannot relabel sandbox obligations as real',$q$UPDATE contracts SET payment_environment='REAL' WHERE id=pg_temp.demo_id('contract-whole')$q$);
SELECT pg_temp.expect_integrity_failure('N36 invited user cannot activate without credentials',$q$UPDATE users SET status='ACTIVE' WHERE id=pg_temp.demo_id('invited-user')$q$);
SELECT pg_temp.expect_integrity_failure('N37 user contact remains globally unique',$q$UPDATE users SET email='tenant-a@example.invalid' WHERE id=pg_temp.demo_id('tenant-b')$q$);
SELECT pg_temp.expect_integrity_failure('N38 non-admin cannot approve owner',$q$UPDATE users SET owner_verification_status='APPROVED',owner_document_public_id='fixture',owner_document_resource_type='image',owner_reviewed_by=pg_temp.demo_id('owner-a'),owner_reviewed_at=now(),owner_review_reason='wrong actor' WHERE id=pg_temp.demo_id('owner-b')$q$);
SELECT pg_temp.expect_integrity_failure('N39 raw OTP rejected',$q$UPDATE users SET verification_purpose='EMAIL',verification_secret_hash='123456',verification_expires_at=now()+interval '1 hour',verification_max_attempts=3 WHERE id=pg_temp.demo_id('tenant-a')$q$);
SELECT pg_temp.expect_integrity_failure('N40 raw refresh token rejected',$q$UPDATE users SET refresh_token_hash='raw-token',refresh_token_expires_at=now()+interval '1 day' WHERE id=pg_temp.demo_id('tenant-a')$q$);
SELECT pg_temp.expect_integrity_failure('N41 challenge purpose cannot reuse hash',$q$UPDATE users SET verification_purpose='PASSWORD_RESET' WHERE id=pg_temp.demo_id('owner-a')$q$);
SELECT pg_temp.expect_integrity_failure('N42 consumed challenge cannot reopen',$q$UPDATE users SET verification_consumed_at=now() WHERE id=pg_temp.demo_id('owner-a'); UPDATE users SET verification_consumed_at=NULL WHERE id=pg_temp.demo_id('owner-a')$q$);
SELECT pg_temp.expect_integrity_failure('N43 hold conflicts with maintenance block',$q$
 INSERT INTO bookings(id,branch_id,room_id,requester_user_id,policy_id,rental_mode,start_date,end_date,hold_amount,policy_snapshot,payment_due_at,temporary_hold_until,payment_environment,status)
 VALUES(pg_temp.demo_id('negative-booking'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('room-free'),pg_temp.demo_id('visitor'),pg_temp.demo_id('branch-a-policy'),'ROOM','2027-01-01','2027-02-01',1000000,'{}',now()+interval '1 hour',now()+interval '1 hour','SANDBOX','AWAITING_PAYMENT');
 INSERT INTO resource_allocations(room_id,booking_id,start_date,end_date,expires_at,status)
 VALUES(pg_temp.demo_id('room-free'),pg_temp.demo_id('negative-booking'),'2027-01-01','2027-02-01',now()+interval '1 hour','HELD');
$q$);
SELECT pg_temp.expect_integrity_failure('N44 signed member snapshot immutable',$q$UPDATE contract_tenants SET tenant_snapshot='{"full_name":"Replacement"}' WHERE id=pg_temp.demo_id('member-a')$q$);
SELECT pg_temp.expect_integrity_failure('N45 duplicate active User in Contract',$q$
 INSERT INTO contract_tenants(contract_id,tenant_user_id,owner_id,room_id,room_slot_id,start_date,end_date,role,monthly_rent,first_month_rent,last_month_rent,prepaid_first_required,prepaid_last_required,terms,status,tenant_snapshot)
 SELECT contract_id,tenant_user_id,owner_id,room_id,room_slot_id,start_date+1,end_date,'MEMBER',monthly_rent,first_month_rent,last_month_rent,prepaid_first_required,prepaid_last_required,terms,status,tenant_snapshot FROM contract_tenants WHERE id=pg_temp.demo_id('member-a');
$q$);
DO $$ BEGIN
 IF to_regclass('rental.contract_amendments') IS NOT NULL THEN
  PERFORM pg_temp.expect_integrity_failure('N46 move-out completion requires leaving and handover',$q$UPDATE contract_tenants SET move_out_status='COMPLETED',move_out_requested_at=now(),requested_leave_date='2027-04-01',move_out_reason='fixture',move_out_processed_by=pg_temp.demo_id('owner-a'),move_out_processed_at=now() WHERE id=pg_temp.demo_id('member-a')$q$);
  PERFORM pg_temp.expect_integrity_failure('N47 prepaid disposition requires approval',$q$UPDATE contract_tenants SET prepaid_last_disposition='REFUND' WHERE id=pg_temp.demo_id('member-a')$q$);
  PERFORM pg_temp.expect_integrity_failure('N48 approved amendment immutable',$q$INSERT INTO contract_amendments(id,contract_id,amendment_number,effective_from,previous_terms,new_terms,reason,status,approved_by,approved_at) VALUES(pg_temp.demo_id('negative-amendment'),pg_temp.demo_id('contract-slot'),1,'2027-01-01','{}','{}','fixture','APPROVED',pg_temp.demo_id('owner-a'),now()); UPDATE contract_amendments SET new_terms='{"tampered":true}' WHERE id=pg_temp.demo_id('negative-amendment')$q$);
 END IF;
 IF to_regclass('rental.threads') IS NOT NULL THEN
  PERFORM pg_temp.expect_integrity_failure('N49 released contract document immutable',$q$UPDATE contracts SET document_public_id='demo/doc',document_resource_type='raw',document_sha256=repeat('c',64),document_version=1,document_snapshot='{}',document_status='AWAITING_CONFIRMATION' WHERE id=pg_temp.demo_id('contract-slot'); UPDATE contracts SET document_snapshot='{"tampered":true}' WHERE id=pg_temp.demo_id('contract-slot')$q$);
  PERFORM pg_temp.expect_integrity_failure('N50 duplicate notification channel',$q$INSERT INTO notifications(recipient_user_id,event_key,type,title,message,channel) SELECT recipient_user_id,event_key,type,title,message,channel FROM notifications LIMIT 1$q$);
 END IF;
END $$;
SELECT * FROM negative_results ORDER BY label;
DO $$ DECLARE expected integer; BEGIN
 expected:=CASE WHEN to_regclass('rental.threads') IS NOT NULL THEN 50 WHEN to_regclass('rental.contract_amendments') IS NOT NULL THEN 48 ELSE 45 END;
 IF (SELECT count(*) FROM negative_results)<>expected THEN RAISE EXCEPTION 'Missing negative cases'; END IF;
END $$;
ROLLBACK;
