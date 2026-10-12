-- DEVELOPMENT FIXTURES ONLY. All money is SANDBOX, all callbacks are fabricated fixtures.
-- No usable password, provider credentials or real Cloudinary documents are included.
-- Run ONCE after 01..05. An accidental second execution fails and rolls back atomically.
-- Amounts/deadlines below are examples, NOT application defaults or agreed policy.
BEGIN;
SET LOCAL search_path=rental,pg_catalog;
CREATE OR REPLACE FUNCTION pg_temp.demo_id(label text) RETURNS uuid LANGUAGE sql IMMUTABLE
 AS $$ SELECT md5('rental-demo:'||label)::uuid $$;
INSERT INTO users(id,email,password_hash,full_name,status,email_verified_at)
SELECT pg_temp.demo_id(label),label||'@example.invalid','!DEMO_LOGIN_DISABLED',label,'ACTIVE',now()
FROM unnest(ARRAY['owner-a','owner-b','manager-a','manager-b','tenant-a','tenant-b','tenant-c','visitor','admin']) label;
INSERT INTO users(id,full_name,status) VALUES(pg_temp.demo_id('invited-user'),'Invited offline fixture','INVITED');
INSERT INTO user_roles(user_id,role_id)
SELECT u.id,r.id FROM users u CROSS JOIN roles r WHERE u.email LIKE '%@example.invalid' AND r.code='USER';
INSERT INTO user_roles(user_id,role_id)
SELECT pg_temp.demo_id(v.label),r.id FROM (VALUES
 ('owner-a','OWNER'),('owner-b','OWNER'),('manager-a','MANAGER'),('manager-b','MANAGER'),
 ('tenant-a','TENANT'),('tenant-b','TENANT'),('tenant-c','TENANT'),('admin','ADMIN')) v(label,code)
JOIN roles r ON r.code=v.code;
SELECT set_config('rental.actor_id',pg_temp.demo_id('owner-a')::text,true);
SELECT set_config('rental.reason','DEMO FIXTURE - not real transactions',true);
INSERT INTO owner_managers(owner_id,manager_id,assigned_by) VALUES
 (pg_temp.demo_id('owner-a'),pg_temp.demo_id('manager-a'),pg_temp.demo_id('owner-a')),
 (pg_temp.demo_id('owner-b'),pg_temp.demo_id('manager-b'),pg_temp.demo_id('owner-b'));
INSERT INTO branches(id,owner_id,manager_id,code,name,address,status,is_public)
SELECT pg_temp.demo_id(label),pg_temp.demo_id(owner_label),pg_temp.demo_id(manager_label),label,label,'DEMO ADDRESS','ACTIVE',true
FROM (VALUES ('branch-a','owner-a','manager-a'),('branch-a2','owner-a','manager-a'),('branch-b','owner-b','manager-b')) v(label,owner_label,manager_label);
INSERT INTO branch_policies(id,branch_id,version,effective_from,collection_day,payment_grace_days,booking_hold_amount,
 payment_window_minutes,temporary_hold_minutes,confirmed_hold_minutes,appointment_policy,cancellation_terms,created_by)
SELECT pg_temp.demo_id(b.code||'-policy'),b.id,1,'2026-01-01T00:00:00+07',31,3,1000000,30,45,1440,
 '{"fixture":true,"group_viewing_allowed":true}','UNDECIDED - fixture only',b.owner_id FROM branches b;
INSERT INTO room_types(id,branch_id,code,name,default_capacity)
SELECT pg_temp.demo_id(code||'-type'),id,'STANDARD','Demo standard',2 FROM branches;
INSERT INTO rooms(id,branch_id,room_type_id,code,rental_mode,capacity,reference_price,operational_status,is_public)
SELECT pg_temp.demo_id(label),pg_temp.demo_id(branch),pg_temp.demo_id(branch||'-type'),label,mode,2,5000000,'AVAILABLE',true
FROM (VALUES ('room-slot','branch-a','SLOT'),('room-whole','branch-a2','ROOM'),('room-free','branch-a','ROOM'),('room-other','branch-b','ROOM')) v(label,branch,mode);
INSERT INTO room_slots(id,room_id,code,name,status) VALUES
 (pg_temp.demo_id('slot-a'),pg_temp.demo_id('room-slot'),'A','Demo bed A','AVAILABLE'),
 (pg_temp.demo_id('slot-b'),pg_temp.demo_id('room-slot'),'B','Demo bed B','AVAILABLE');
INSERT INTO appointments(id,room_id,requester_user_id,assigned_to,start_at,end_at,status,policy_snapshot)
SELECT pg_temp.demo_id('appointment-'||label),pg_temp.demo_id('room-free'),pg_temp.demo_id(label),pg_temp.demo_id('manager-a'),
 '2026-10-25T09:00:00+07','2026-10-25T10:00:00+07','CONFIRMED','{"group_viewing_allowed":true,"fixture":true}'
FROM unnest(ARRAY['visitor','tenant-b']) label;
INSERT INTO bookings(id,branch_id,room_id,room_slot_id,requester_user_id,policy_id,rental_mode,start_date,end_date,
 hold_amount,policy_snapshot,payment_due_at,temporary_hold_until,confirmed_hold_until,payment_environment,status)
VALUES(pg_temp.demo_id('booking-a'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('slot-a'),
 pg_temp.demo_id('tenant-a'),pg_temp.demo_id('branch-a-policy'),'SLOT','2026-11-01','2027-05-01',1000000,'{"fixture":true,"hold":1000000}',
 '2026-10-26T10:30:00+07','2026-10-26T10:45:00+07','2026-10-27T10:00:00+07','SANDBOX','AWAITING_PAYMENT');
INSERT INTO resource_allocations(id,room_id,room_slot_id,booking_id,start_date,end_date,expires_at,status)
VALUES(pg_temp.demo_id('booking-allocation'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('slot-a'),pg_temp.demo_id('booking-a'),
 '2026-11-01','2027-05-01','2026-10-26T10:45:00+07','HELD');
INSERT INTO payments(id,branch_id,booking_id,payer_user_id,method,environment,status,amount_received,allocated_amount,
 provider_account_key,provider_transaction_id,idempotency_key,received_at,verified_at,verification_note)
VALUES(pg_temp.demo_id('hold-payment'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('booking-a'),pg_temp.demo_id('tenant-a'),
 'VNPAY','SANDBOX','VERIFIED',1000000,1000000,'DEMO','DEMO-HOLD','demo-hold','2026-10-26T10:10:00+07','2026-10-26T10:10:00+07','Fabricated test callback');
INSERT INTO payment_webhook_events(payment_id,provider,environment,provider_account_key,event_key,payload_hash,safe_payload,signature_verified,processing_status,processed_at)
VALUES(pg_temp.demo_id('hold-payment'),'VNPAY','SANDBOX','DEMO','DEMO-HOLD','fixture-not-a-real-signature','{"fixture":true}',true,'ACCEPTED','2026-10-26T10:10:00+07');
UPDATE bookings SET status='CONFIRMED' WHERE id=pg_temp.demo_id('booking-a');
SET CONSTRAINTS ALL IMMEDIATE;
SET CONSTRAINTS ALL DEFERRED;
-- Create a single main room Contract, including two members with different dates/prices.
INSERT INTO contracts(id,owner_id,branch_id,room_id,code,rental_mode,payment_environment,start_date,end_date,terms,status,created_by) VALUES
 (pg_temp.demo_id('contract-slot'),pg_temp.demo_id('owner-a'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('room-slot'),'DEMO-SLOT','SLOT','SANDBOX','2026-11-01','2027-05-01','Fixture agreement','ACTIVE',pg_temp.demo_id('owner-a')),
 (pg_temp.demo_id('contract-whole'),pg_temp.demo_id('owner-a'),pg_temp.demo_id('branch-a2'),pg_temp.demo_id('room-whole'),'DEMO-ROOM','ROOM','SANDBOX','2026-11-01','2027-05-01','Fixture agreement','ACTIVE',pg_temp.demo_id('owner-a'));
UPDATE bookings SET status='CONVERTED' WHERE id=pg_temp.demo_id('booking-a');
-- A second Booking joins the same main Room Contract later.
INSERT INTO bookings(id,branch_id,room_id,room_slot_id,requester_user_id,policy_id,rental_mode,start_date,end_date,
 hold_amount,policy_snapshot,payment_due_at,temporary_hold_until,confirmed_hold_until,payment_environment,status)
VALUES(pg_temp.demo_id('booking-b'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('slot-b'),
 pg_temp.demo_id('tenant-b'),pg_temp.demo_id('branch-a-policy'),'SLOT','2026-11-21','2027-05-01',1000000,'{"fixture":true}',
 '2026-11-20T10:30:00+07','2026-11-20T10:45:00+07','2026-11-21T10:00:00+07','SANDBOX','AWAITING_PAYMENT');
INSERT INTO payments(id,branch_id,booking_id,payer_user_id,method,environment,status,amount_received,allocated_amount,
 provider_account_key,provider_transaction_id,idempotency_key,received_at,verified_at)
VALUES(pg_temp.demo_id('hold-payment-b'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('booking-b'),pg_temp.demo_id('tenant-b'),
 'VNPAY','SANDBOX','VERIFIED',1000000,1000000,'DEMO','DEMO-HOLD-B','demo-hold-b','2026-11-20T10:10:00+07','2026-11-20T10:10:00+07');
UPDATE bookings SET status='CONFIRMED' WHERE id=pg_temp.demo_id('booking-b');
UPDATE bookings SET status='CONVERTED' WHERE id=pg_temp.demo_id('booking-b');
INSERT INTO contract_tenants(id,contract_id,tenant_user_id,owner_id,room_id,room_slot_id,source_booking_id,start_date,end_date,role,
 monthly_rent,first_month_rent,last_month_rent,prepaid_first_required,prepaid_last_required,terms,status,tenant_snapshot) VALUES
 (pg_temp.demo_id('member-a'),pg_temp.demo_id('contract-slot'),pg_temp.demo_id('tenant-a'),pg_temp.demo_id('owner-a'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('slot-a'),pg_temp.demo_id('booking-a'),'2026-11-01','2027-05-01','REPRESENTATIVE',5000000,5000000,5000000,5000000,5000000,'Fixture first/last prepaid','ACTIVE','{"full_name":"Signed demo member"}'),
 (pg_temp.demo_id('member-b'),pg_temp.demo_id('contract-slot'),pg_temp.demo_id('tenant-b'),pg_temp.demo_id('owner-a'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('slot-b'),pg_temp.demo_id('booking-b'),'2026-11-21','2027-05-01','MEMBER',3000000,2000000,3000000,2000000,3000000,'First partial month explicitly agreed at 2M; not automatic proration','ACTIVE','{"full_name":"Signed demo member"}'),
 (pg_temp.demo_id('member-c'),pg_temp.demo_id('contract-whole'),pg_temp.demo_id('tenant-c'),pg_temp.demo_id('owner-a'),pg_temp.demo_id('room-whole'),NULL,NULL,'2026-11-01','2027-05-01','REPRESENTATIVE',7000000,7000000,7000000,7000000,7000000,'Whole room fixture','ACTIVE','{"full_name":"Signed demo member"}');
UPDATE resource_allocations SET status='RELEASED',released_at='2026-10-26T11:00:00+07' WHERE id=pg_temp.demo_id('booking-allocation');
INSERT INTO resource_allocations(id,room_id,room_slot_id,contract_id,contract_tenant_id,start_date,end_date,status)
SELECT pg_temp.demo_id('lease-'||id::text),room_id,room_slot_id,contract_id,
 CASE WHEN room_slot_id IS NULL THEN NULL ELSE id END,start_date,end_date,'LEASED' FROM contract_tenants;
INSERT INTO room_slot_occupancies(id,room_id,room_slot_id,contract_tenant_id,start_date,end_date)
SELECT pg_temp.demo_id('occupancy-'||id::text),room_id,room_slot_id,id,start_date,end_date FROM contract_tenants;
-- Transfer internal classification of the SAME received payment; no second receipt.
UPDATE payments SET allocated_amount=0,prepaid_contract_tenant_id=pg_temp.demo_id('member-a'),prepaid_first_amount=1000000
WHERE id=pg_temp.demo_id('hold-payment');
UPDATE payments SET allocated_amount=0,prepaid_contract_tenant_id=pg_temp.demo_id('member-b'),prepaid_first_amount=1000000
WHERE id=pg_temp.demo_id('hold-payment-b');
INSERT INTO services(id,branch_id,code,name,unit,service_type,billing_method,reference_price,mandatory) VALUES
 (pg_temp.demo_id('electricity'),pg_temp.demo_id('branch-a'),'ELECTRIC','Electricity','kWh','ELECTRICITY','METERED',3000,true),
 (pg_temp.demo_id('water'),pg_temp.demo_id('branch-a'),'WATER','Water','m3','WATER','METERED',15000,true);
INSERT INTO room_services(id,room_id,branch_id,service_id,start_date,end_date,unit_price,status)
SELECT pg_temp.demo_id(code||'-room'),pg_temp.demo_id('room-slot'),branch_id,id,'2026-11-01','2027-05-01',reference_price,'ACTIVE' FROM services;
INSERT INTO contract_services(contract_id,room_id,room_service_id,start_date,end_date,status,requested_by,processed_by,processed_at)
SELECT pg_temp.demo_id('contract-slot'),room_id,id,start_date,end_date,'APPROVED',pg_temp.demo_id('owner-a'),pg_temp.demo_id('owner-a'),now() FROM room_services;
INSERT INTO room_service_charges(id,room_service_id,room_id,billing_month,start_date,end_date,segment_number,meter_reference,segment_reason,
 start_reading,end_reading,quantity,unit_price,total_amount,calculation_snapshot,status,finalized_by,finalized_at)
VALUES(pg_temp.demo_id('charge-nov'),pg_temp.demo_id('ELECTRIC-room'),pg_temp.demo_id('room-slot'),'2026-11-01','2026-11-01','2026-12-01',1,
 'DEMO-METER','REGULAR',100,200,100,3000,300000,'{"fixture":true,"formula":"quantity * price"}','FINALIZED',pg_temp.demo_id('manager-a'),'2026-12-01T08:00:00+07');
INSERT INTO cost_allocation_proposals(id,room_service_charge_id,method,allocation_details,calculation_snapshot,status,processed_by,processed_at)
SELECT pg_temp.demo_id('proposal-nov'),pg_temp.demo_id('charge-nov'),'RESIDENT_DAYS',
 jsonb_agg(jsonb_build_object('contract_tenant_id',contract_tenant_id,'resident_days',resident_days,'amount',amount)),
 '{"fixture":true,"rounding":"largest remainder; UUID ascending tie-break"}','APPROVED',pg_temp.demo_id('manager-a'),'2026-12-01T08:00:00+07'
FROM default_charge_allocation(pg_temp.demo_id('charge-nov'));
INSERT INTO furnitures(id,room_id,asset_code,name,status) VALUES(pg_temp.demo_id('desk'),pg_temp.demo_id('room-slot'),'DESK-1','Demo desk','REPAIRING');
INSERT INTO report_requests(id,room_id,requester_user_id,contract_tenant_id,title,description,priority,status,assigned_to)
VALUES(pg_temp.demo_id('repair'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('tenant-a'),pg_temp.demo_id('member-a'),'Demo repair','Fixture only','NORMAL','COMPLETED',pg_temp.demo_id('manager-a'));
INSERT INTO report_requests_furnitures(report_request_id,room_id,furniture_id)
VALUES(pg_temp.demo_id('repair'),pg_temp.demo_id('room-slot'),pg_temp.demo_id('desk'));
INSERT INTO report_records(id,report_request_id,record_type,amount,description,performed_by,approval_status,approved_by,approved_at)
VALUES(pg_temp.demo_id('repair-cost'),pg_temp.demo_id('repair'),'COST',100000,'Fixture approved cost',pg_temp.demo_id('manager-a'),'APPROVED',pg_temp.demo_id('owner-a'),now());
INSERT INTO report_records(id,report_request_id,record_type,parent_cost_record_id,contract_tenant_id,responsible_party,amount,description,performed_by,approval_status,approved_by,approved_at) VALUES
 (pg_temp.demo_id('repair-owner'),pg_temp.demo_id('repair'),'LIABILITY',pg_temp.demo_id('repair-cost'),NULL,'OWNER',40000,'Owner share',pg_temp.demo_id('manager-a'),'APPROVED',pg_temp.demo_id('owner-a'),now()),
 (pg_temp.demo_id('repair-tenant'),pg_temp.demo_id('repair'),'LIABILITY',pg_temp.demo_id('repair-cost'),pg_temp.demo_id('member-a'),'TENANT',60000,'Tenant share',pg_temp.demo_id('manager-a'),'APPROVED',pg_temp.demo_id('owner-a'),now());
INSERT INTO invoices(id,contract_tenant_id,billing_month,due_date,policy_snapshot,document_status,payment_environment,subtotal_amount,adjustment_amount,total_amount,created_by)
SELECT pg_temp.demo_id(label),pg_temp.demo_id(member),month::date,invoice_due_date(month::date,31,3),
 '{"fixture":true,"collection_day":31,"grace_days":3}','DRAFT','SANDBOX',total,0,total,pg_temp.demo_id('owner-a')
FROM (VALUES ('invoice-a','member-a','2026-11-01',5285000),('invoice-b','member-b','2026-11-01',2075000),('invoice-c','member-c','2026-11-01',7000000),('invoice-a-last','member-a','2027-04-01',5000000)) v(label,member,month,total);
INSERT INTO invoice_items(invoice_id,line_number,item_type,description,quantity,unit_price,amount)
SELECT pg_temp.demo_id(label),1,'RENT','Fixture agreed rent',1,amount,amount
FROM (VALUES ('invoice-a',5000000),('invoice-b',2000000),('invoice-c',7000000),('invoice-a-last',5000000)) v(label,amount);
INSERT INTO invoice_items(invoice_id,line_number,item_type,description,quantity,unit_price,amount,source_charge_id,source_proposal_id)
SELECT pg_temp.demo_id(label),2,'SERVICE','November electricity by actual resident days',1,amount,amount,pg_temp.demo_id('charge-nov'),pg_temp.demo_id('proposal-nov')
FROM (VALUES ('invoice-a',225000),('invoice-b',75000)) v(label,amount);
INSERT INTO invoice_items(invoice_id,line_number,item_type,description,quantity,unit_price,amount,source_report_record_id)
VALUES(pg_temp.demo_id('invoice-a'),3,'REPAIR','Approved tenant liability',1,60000,60000,pg_temp.demo_id('repair-tenant'));
UPDATE invoices SET document_status='ISSUED',issued_by=pg_temp.demo_id('owner-a'),issued_at='2026-12-01T09:00:00+07';
INSERT INTO payments(id,branch_id,invoice_id,payer_user_id,method,environment,status,amount_received,allocated_amount,prepaid_contract_tenant_id,
 prepaid_first_amount,prepaid_last_amount,provider_account_key,provider_transaction_id,idempotency_key,received_at,verified_at)
SELECT pg_temp.demo_id(label),pg_temp.demo_id('branch-a'),pg_temp.demo_id(invoice),pg_temp.demo_id(payer),'VNPAY','SANDBOX','VERIFIED',
 amount,direct,CASE WHEN member IS NULL THEN NULL ELSE pg_temp.demo_id(member) END,first_amount,last_amount,'DEMO',label,label,'2026-12-01T10:00:00+07','2026-12-01T10:00:00+07'
FROM (VALUES
 ('prepaid-a','invoice-a','tenant-a','member-a',9000000,0,4000000,5000000),
 ('prepaid-b','invoice-b','tenant-b','member-b',4000000,0,1000000,3000000),
 ('third-party-payment','invoice-a','tenant-b',NULL,100000,100000,0,0),
 ('overpayment-b','invoice-b','tenant-b',NULL,100000,75000,0,0)) v(label,invoice,payer,member,amount,direct,first_amount,last_amount);
INSERT INTO payment_webhook_events(payment_id,provider,environment,provider_account_key,event_key,payload_hash,safe_payload,signature_verified,processing_status,processed_at)
SELECT id,method,environment,'DEMO',idempotency_key,'fixture-not-a-real-signature','{"fixture":true}',true,'ACCEPTED',verified_at
FROM payments WHERE id<>pg_temp.demo_id('hold-payment');
INSERT INTO payments(id,branch_id,invoice_id,payer_user_id,method,environment,status,amount_received,prepaid_contract_tenant_id,
 prepaid_first_amount,prepaid_last_amount,provider_account_key,provider_transaction_id,idempotency_key,received_at,verified_at)
VALUES(pg_temp.demo_id('prepaid-c'),pg_temp.demo_id('branch-a2'),pg_temp.demo_id('invoice-c'),pg_temp.demo_id('tenant-c'),'VNPAY','SANDBOX','VERIFIED',14000000,
 pg_temp.demo_id('member-c'),7000000,7000000,'DEMO','DEMO-PREPAID-C','demo-prepaid-c','2026-12-01T10:00:00+07','2026-12-01T10:00:00+07');
INSERT INTO payment_webhook_events(payment_id,provider,environment,provider_account_key,event_key,payload_hash,safe_payload,signature_verified,processing_status,processed_at)
VALUES(pg_temp.demo_id('prepaid-c'),'VNPAY','SANDBOX','DEMO','DEMO-PREPAID-C','fixture-not-a-real-signature','{"fixture":true}',true,'ACCEPTED','2026-12-01T10:00:00+07');
INSERT INTO invoice_items(invoice_id,line_number,item_type,description,quantity,unit_price,amount,source_payment_id,prepaid_bucket,idempotency_key)
SELECT pg_temp.demo_id(inv),line,'PREPAID_APPLICATION','Internal settlement, excluded from invoice face amount',1,amount,amount,pg_temp.demo_id(payment),bucket,key
FROM (VALUES
 ('invoice-a',4,1000000,'hold-payment','FIRST_MONTH','credit-hold-a'),
 ('invoice-a',5,4000000,'prepaid-a','FIRST_MONTH','credit-first-a'),
 ('invoice-b',3,1000000,'prepaid-b','FIRST_MONTH','credit-first-b'),
 ('invoice-b',4,1000000,'hold-payment-b','FIRST_MONTH','credit-hold-b'),
 ('invoice-c',2,7000000,'prepaid-c','FIRST_MONTH','credit-first-c'),
 ('invoice-a-last',2,5000000,'prepaid-a','LAST_MONTH','credit-last-a')) v(inv,line,amount,payment,bucket,key);
-- Expired reservation remains expired even if a late payment is received.
INSERT INTO bookings(id,branch_id,room_id,requester_user_id,policy_id,rental_mode,start_date,end_date,hold_amount,policy_snapshot,
 payment_due_at,temporary_hold_until,payment_environment,status)
VALUES(pg_temp.demo_id('booking-expired'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('room-free'),pg_temp.demo_id('visitor'),pg_temp.demo_id('branch-a-policy'),'ROOM',
 '2026-11-01','2027-05-01',1000000,'{"fixture":true}','2026-10-20T10:00:00+07','2026-10-20T10:15:00+07','SANDBOX','EXPIRED');
INSERT INTO payments(id,branch_id,booking_id,payer_user_id,method,environment,status,amount_received,provider_account_key,provider_transaction_id,idempotency_key,received_at,verified_at)
VALUES(pg_temp.demo_id('late-payment'),pg_temp.demo_id('branch-a'),pg_temp.demo_id('booking-expired'),pg_temp.demo_id('visitor'),'VNPAY','SANDBOX','VERIFIED',1000000,'DEMO','DEMO-LATE','demo-late','2026-10-20T11:00:00+07','2026-10-20T11:00:00+07');
INSERT INTO payment_webhook_events(payment_id,provider,environment,provider_account_key,event_key,payload_hash,safe_payload,signature_verified,processing_status,processed_at)
VALUES(pg_temp.demo_id('late-payment'),'VNPAY','SANDBOX','DEMO','DEMO-LATE','fixture-not-a-real-signature','{"fixture":true}',true,'ACCEPTED','2026-10-20T11:00:00+07');
INSERT INTO notifications(recipient_user_id,event_key,type,title,message,reference_type,reference_id)
VALUES(pg_temp.demo_id('tenant-a'),'demo-invoice-issued','INVOICE_ISSUED','Demo invoice','Fixture only','INVOICE',pg_temp.demo_id('invoice-a'));
-- Same User may have agreements with different Owners; no duplicate person profile.
INSERT INTO contracts(id,owner_id,branch_id,room_id,code,rental_mode,payment_environment,start_date,end_date,terms,status,created_by)
VALUES(pg_temp.demo_id('contract-other-owner'),pg_temp.demo_id('owner-b'),pg_temp.demo_id('branch-b'),pg_temp.demo_id('room-other'),'OTHER-OWNER','ROOM','SANDBOX','2027-06-01','2027-12-01','Draft fixture','DRAFT',pg_temp.demo_id('owner-b'));
INSERT INTO contract_tenants(id,contract_id,tenant_user_id,owner_id,room_id,start_date,end_date,role,monthly_rent,first_month_rent,last_month_rent,prepaid_first_required,prepaid_last_required,terms,status,tenant_snapshot)
VALUES(pg_temp.demo_id('member-other-owner'),pg_temp.demo_id('contract-other-owner'),pg_temp.demo_id('tenant-a'),pg_temp.demo_id('owner-b'),pg_temp.demo_id('room-other'),'2027-06-01','2027-12-01','REPRESENTATIVE',5000000,5000000,5000000,5000000,5000000,'Draft fixture','DRAFT','{"full_name":"Same User, another Owner"}');
INSERT INTO resource_allocations(id,room_id,start_date,end_date,allocation_kind,block_reason,blocked_by,status)
VALUES(pg_temp.demo_id('maintenance-block'),pg_temp.demo_id('room-free'),'2027-01-01','2027-02-01','MAINTENANCE_BLOCK','Fixture scheduled work',pg_temp.demo_id('owner-a'),'BLOCKED');
-- Only ADMIN may decide Owner verification. No credential/document payload enters audit.
SELECT set_config('rental.actor_id',pg_temp.demo_id('admin')::text,true);
UPDATE users SET owner_verification_status='APPROVED',owner_document_public_id='demo/private/owner-a',owner_document_resource_type='image',
 owner_reviewed_by=pg_temp.demo_id('admin'),owner_reviewed_at=now(),owner_review_reason='Fixture review',
 verification_purpose='EMAIL',verification_secret_hash=repeat('a',64),verification_expires_at=now()+interval '1 hour',verification_max_attempts=3,
 refresh_token_hash=repeat('b',64),refresh_token_expires_at=now()+interval '1 day' WHERE id=pg_temp.demo_id('owner-a');
SET CONSTRAINTS ALL IMMEDIATE;
COMMIT;
