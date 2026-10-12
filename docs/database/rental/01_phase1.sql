-- Fresh installation into an EMPTY rental schema. Not a migration from public.
-- Run as the schema owner. No DROP, no changes to public/auth/storage.
BEGIN;
SET LOCAL lock_timeout = '10s';
CREATE SCHEMA IF NOT EXISTS rental;
DO $$ BEGIN
 IF EXISTS (SELECT 1 FROM pg_tables WHERE schemaname = 'rental') THEN
  RAISE EXCEPTION 'rental is not empty: stop; do not overwrite an existing installation';
 END IF;
END $$;
SET LOCAL search_path = rental, pg_catalog;

CREATE TABLE users (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), email varchar(320), phone varchar(32),
 password_hash text, full_name varchar(255) NOT NULL,
 status varchar(24) NOT NULL CHECK(status IN ('INVITED','PENDING_VERIFICATION','ACTIVE','LOCKED','DISABLED')),
 date_of_birth date, gender varchar(30), identity_document_public_id text, identity_document_resource_type varchar(20),
 owner_verification_status varchar(20) NOT NULL DEFAULT 'NOT_SUBMITTED' CHECK(owner_verification_status IN ('NOT_SUBMITTED','PENDING','APPROVED','REJECTED')),
 owner_document_public_id text, owner_document_resource_type varchar(20),
 owner_reviewed_by uuid REFERENCES users, owner_reviewed_at timestamptz, owner_review_reason text,
 verification_purpose varchar(24) CHECK(verification_purpose IN ('EMAIL','PHONE','PASSWORD_RESET','INVITATION','CONTRACT_CONFIRMATION')),
 verification_secret_hash text CHECK(verification_secret_hash ~ '^[0-9a-f]{64}$'), verification_expires_at timestamptz,
 verification_attempts integer NOT NULL DEFAULT 0 CHECK(verification_attempts>=0),
 verification_max_attempts integer CHECK(verification_max_attempts>0), verification_consumed_at timestamptz,
 failed_login_count integer NOT NULL DEFAULT 0 CHECK(failed_login_count>=0), last_failed_login_at timestamptz,
 refresh_token_hash text CHECK(refresh_token_hash ~ '^[0-9a-f]{64}$'), refresh_token_expires_at timestamptz,
 email_verified_at timestamptz, phone_verified_at timestamptz, locked_until timestamptz,
 auth_version bigint NOT NULL DEFAULT 0,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 CHECK(status='INVITED' OR email IS NOT NULL OR phone IS NOT NULL),
 CHECK(status<>'ACTIVE' OR (password_hash IS NOT NULL AND password_hash<>'' AND
  ((email IS NOT NULL AND email_verified_at IS NOT NULL) OR (phone IS NOT NULL AND phone_verified_at IS NOT NULL)))),
 CHECK(num_nonnulls(refresh_token_hash,refresh_token_expires_at) IN (0,2)),
 CHECK((verification_secret_hash IS NULL AND verification_purpose IS NULL AND verification_expires_at IS NULL AND verification_max_attempts IS NULL AND verification_consumed_at IS NULL)
  OR (verification_secret_hash IS NOT NULL AND verification_purpose IS NOT NULL AND verification_expires_at IS NOT NULL AND verification_max_attempts IS NOT NULL)),
 CHECK(owner_verification_status='NOT_SUBMITTED' OR (owner_document_public_id IS NOT NULL AND owner_document_resource_type IS NOT NULL)),
 CHECK(owner_verification_status NOT IN ('APPROVED','REJECTED') OR (owner_reviewed_by IS NOT NULL AND owner_reviewed_at IS NOT NULL AND owner_review_reason IS NOT NULL)),
 CHECK(email IS NULL OR (email = lower(btrim(email)) AND email <> '')),
 CHECK(phone IS NULL OR phone ~ '^\+[1-9][0-9]{7,14}$')
);
CREATE UNIQUE INDEX uq_user_email ON users(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX uq_user_phone ON users(phone) WHERE phone IS NOT NULL;
CREATE TABLE roles (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), code varchar(20) NOT NULL UNIQUE
 CHECK(code IN ('USER','OWNER','MANAGER','TENANT','ADMIN'))
);
CREATE TABLE user_roles (
 user_id uuid NOT NULL REFERENCES users, role_id uuid NOT NULL REFERENCES roles,
 assigned_by uuid REFERENCES users, assigned_at timestamptz NOT NULL DEFAULT now(),
 PRIMARY KEY(user_id,role_id)
);
-- Current relationship only. Historical relationships belong to audit_logs.
CREATE TABLE owner_managers (
 owner_id uuid NOT NULL REFERENCES users, manager_id uuid NOT NULL REFERENCES users,
 assigned_at timestamptz NOT NULL DEFAULT now(), assigned_by uuid NOT NULL REFERENCES users,
 PRIMARY KEY(owner_id,manager_id), UNIQUE(manager_id), CHECK(owner_id <> manager_id)
);
CREATE TABLE branches (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), owner_id uuid NOT NULL REFERENCES users,
 manager_id uuid, code varchar(50) NOT NULL, name varchar(255) NOT NULL,
 address text NOT NULL, province varchar(100), district varchar(100), ward varchar(100),
 latitude numeric(10,7) CHECK(latitude BETWEEN -90 AND 90), longitude numeric(10,7) CHECK(longitude BETWEEN -180 AND 180),
 description text, rules text, public_contact text, is_public boolean NOT NULL DEFAULT false,
 image_refs jsonb NOT NULL DEFAULT '[]' CHECK(jsonb_typeof(image_refs)='array'),
 qr_public_id text, qr_url text, zalo_group_url text, zalo_enabled boolean NOT NULL DEFAULT false,
 status varchar(20) NOT NULL CHECK(status IN ('DRAFT','ACTIVE','INACTIVE')),
 version bigint NOT NULL DEFAULT 0, created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(owner_id,code), UNIQUE(id,owner_id),
 FOREIGN KEY(owner_id,manager_id) REFERENCES owner_managers(owner_id,manager_id),
 CHECK(status <> 'ACTIVE' OR manager_id IS NOT NULL)
);
CREATE TABLE branch_policies (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), branch_id uuid NOT NULL REFERENCES branches,
 version integer NOT NULL CHECK(version > 0), effective_from timestamptz NOT NULL,
 collection_day integer NOT NULL CHECK(collection_day BETWEEN 1 AND 31),
 payment_grace_days integer NOT NULL CHECK(payment_grace_days >= 0),
 booking_hold_amount numeric(19,0) NOT NULL CHECK(booking_hold_amount > 0),
 payment_window_minutes integer NOT NULL CHECK(payment_window_minutes > 0),
 temporary_hold_minutes integer NOT NULL CHECK(temporary_hold_minutes > 0),
 confirmed_hold_minutes integer NOT NULL CHECK(confirmed_hold_minutes > 0),
 appointment_policy jsonb NOT NULL DEFAULT '{}', cancellation_terms text,
 created_by uuid NOT NULL REFERENCES users, created_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(branch_id,version), UNIQUE(id,branch_id)
);
CREATE TABLE room_types (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), branch_id uuid NOT NULL REFERENCES branches,
 code varchar(50) NOT NULL, name text NOT NULL, default_capacity integer NOT NULL CHECK(default_capacity>0),
 description text, active boolean NOT NULL DEFAULT true, UNIQUE(branch_id,code), UNIQUE(id,branch_id)
);
CREATE TABLE rooms (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), branch_id uuid NOT NULL REFERENCES branches,
 room_type_id uuid NOT NULL, code varchar(50) NOT NULL,
 rental_mode varchar(4) NOT NULL CHECK(rental_mode IN ('ROOM','SLOT')),
 capacity integer NOT NULL CHECK(capacity>0), area_sqm numeric(10,2) CHECK(area_sqm>0), floor_number integer,
 reference_price numeric(19,0) NOT NULL CHECK(reference_price>=0), description text,
 amenities text[] NOT NULL DEFAULT '{}', image_refs jsonb NOT NULL DEFAULT '[]' CHECK(jsonb_typeof(image_refs)='array'),
 operational_status varchar(20) NOT NULL CHECK(operational_status IN ('AVAILABLE','MAINTENANCE','INACTIVE')),
 is_public boolean NOT NULL DEFAULT false, version bigint NOT NULL DEFAULT 0,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(branch_id,code), UNIQUE(id,branch_id),
 FOREIGN KEY(room_type_id,branch_id) REFERENCES room_types(id,branch_id)
);
CREATE TABLE room_slots (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL REFERENCES rooms,
 code varchar(50) NOT NULL, name text NOT NULL,
 status varchar(20) NOT NULL CHECK(status IN ('AVAILABLE','MAINTENANCE','INACTIVE')),
 UNIQUE(room_id,code), UNIQUE(id,room_id)
);
CREATE TABLE appointments (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL REFERENCES rooms,
 requester_user_id uuid NOT NULL REFERENCES users, assigned_to uuid REFERENCES users,
 start_at timestamptz NOT NULL, end_at timestamptz NOT NULL,
 status varchar(20) NOT NULL CHECK(status IN ('PENDING','CONFIRMED','COMPLETED','REJECTED','CANCELLED')),
 policy_snapshot jsonb NOT NULL, note text, version bigint NOT NULL DEFAULT 0,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), CHECK(end_at>start_at)
);
CREATE TABLE bookings (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), branch_id uuid NOT NULL REFERENCES branches,
 room_id uuid NOT NULL, room_slot_id uuid, requester_user_id uuid NOT NULL REFERENCES users,
 appointment_id uuid REFERENCES appointments, policy_id uuid NOT NULL,
 rental_mode varchar(4) NOT NULL CHECK(rental_mode IN ('ROOM','SLOT')),
 start_date date NOT NULL, end_date date NOT NULL,
 hold_amount numeric(19,0) NOT NULL CHECK(hold_amount>0), policy_snapshot jsonb NOT NULL,
 payment_due_at timestamptz NOT NULL, temporary_hold_until timestamptz NOT NULL, confirmed_hold_until timestamptz,
 payment_environment varchar(10) NOT NULL CHECK(payment_environment IN ('REAL','SANDBOX')),
 status varchar(24) NOT NULL CHECK(status IN ('REQUESTED','AWAITING_PAYMENT','CONFIRMED','CONVERTED','EXPIRED','CANCELLED','REJECTED')),
 cancellation_reason text, version bigint NOT NULL DEFAULT 0,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(id,room_id), UNIQUE(id,branch_id),
 FOREIGN KEY(room_id,branch_id) REFERENCES rooms(id,branch_id),
 FOREIGN KEY(room_slot_id,room_id) REFERENCES room_slots(id,room_id),
 FOREIGN KEY(policy_id,branch_id) REFERENCES branch_policies(id,branch_id),
 CHECK(end_date>start_date), CHECK((rental_mode='ROOM' AND room_slot_id IS NULL) OR (rental_mode='SLOT' AND room_slot_id IS NOT NULL)),
 CHECK(status<>'CONFIRMED' OR confirmed_hold_until IS NOT NULL)
);
CREATE TABLE contracts (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), owner_id uuid NOT NULL, branch_id uuid NOT NULL, room_id uuid NOT NULL,
 code varchar(50) NOT NULL, rental_mode varchar(4) NOT NULL CHECK(rental_mode IN ('ROOM','SLOT')),
 payment_environment varchar(10) NOT NULL CHECK(payment_environment IN ('REAL','SANDBOX')),
 start_date date NOT NULL, end_date date NOT NULL, terms text NOT NULL,
 status varchar(20) NOT NULL CHECK(status IN ('DRAFT','ACTIVE','ENDED','CANCELLED')),
 created_by uuid NOT NULL REFERENCES users, version bigint NOT NULL DEFAULT 0,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(),
 UNIQUE(branch_id,code), UNIQUE(id,owner_id), UNIQUE(id,room_id), UNIQUE(id,branch_id),
 FOREIGN KEY(branch_id,owner_id) REFERENCES branches(id,owner_id), FOREIGN KEY(room_id,branch_id) REFERENCES rooms(id,branch_id),
 CHECK(end_date>start_date)
);
CREATE TABLE contract_tenants (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), contract_id uuid NOT NULL, tenant_user_id uuid NOT NULL REFERENCES users, owner_id uuid NOT NULL,
 room_id uuid NOT NULL, room_slot_id uuid, source_booking_id uuid UNIQUE,
 tenant_snapshot jsonb NOT NULL CHECK(jsonb_typeof(tenant_snapshot)='object'),
 start_date date NOT NULL, end_date date NOT NULL, actual_leave_date date,
 role varchar(20) NOT NULL CHECK(role IN ('REPRESENTATIVE','MEMBER')),
 monthly_rent numeric(19,0) NOT NULL CHECK(monthly_rent>=0),
 first_month_rent numeric(19,0) NOT NULL CHECK(first_month_rent>=0), last_month_rent numeric(19,0) NOT NULL CHECK(last_month_rent>=0),
 prepaid_first_required numeric(19,0) NOT NULL CHECK(prepaid_first_required>=0),
 prepaid_last_required numeric(19,0) NOT NULL CHECK(prepaid_last_required>=0), terms text NOT NULL,
 status varchar(20) NOT NULL CHECK(status IN ('DRAFT','ACTIVE','LEFT','CANCELLED')),
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), version bigint NOT NULL DEFAULT 0,
 UNIQUE(id,contract_id), UNIQUE(id,room_id), UNIQUE(tenant_user_id,contract_id,start_date),
 FOREIGN KEY(contract_id,owner_id) REFERENCES contracts(id,owner_id),
 FOREIGN KEY(contract_id,room_id) REFERENCES contracts(id,room_id), FOREIGN KEY(room_slot_id,room_id) REFERENCES room_slots(id,room_id),
 FOREIGN KEY(source_booking_id,room_id) REFERENCES bookings(id,room_id),
 CHECK(end_date>start_date), CHECK(actual_leave_date IS NULL OR actual_leave_date>start_date),
 CHECK(prepaid_first_required=first_month_rent AND prepaid_last_required=last_month_rent)
 ,CHECK(status='DRAFT' OR coalesce(jsonb_typeof(tenant_snapshot->'full_name')='string' AND nullif(btrim(tenant_snapshot->>'full_name'),'') IS NOT NULL,false))
);
CREATE UNIQUE INDEX uq_contract_representative ON contract_tenants(contract_id) WHERE role='REPRESENTATIVE' AND status='ACTIVE';
CREATE UNIQUE INDEX uq_contract_current_member ON contract_tenants(contract_id,tenant_user_id) WHERE status='ACTIVE';
CREATE TABLE resource_allocations (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL REFERENCES rooms, room_slot_id uuid,
 booking_id uuid, contract_id uuid, contract_tenant_id uuid,
 start_date date NOT NULL, end_date date NOT NULL, expires_at timestamptz,
 allocation_kind varchar(20) NOT NULL DEFAULT 'RENTAL' CHECK(allocation_kind IN ('RENTAL','MAINTENANCE_BLOCK')),
 block_reason text, blocked_by uuid REFERENCES users,
 status varchar(20) NOT NULL CHECK(status IN ('HELD','LEASED','BLOCKED','RELEASED')),
 created_at timestamptz NOT NULL DEFAULT now(), released_at timestamptz,
 FOREIGN KEY(room_slot_id,room_id) REFERENCES room_slots(id,room_id), FOREIGN KEY(booking_id,room_id) REFERENCES bookings(id,room_id),
 FOREIGN KEY(contract_id,room_id) REFERENCES contracts(id,room_id),
 FOREIGN KEY(contract_tenant_id,contract_id) REFERENCES contract_tenants(id,contract_id),
 CHECK(end_date>start_date),
 CHECK((allocation_kind='RENTAL' AND num_nonnulls(booking_id,contract_id)=1 AND status<>'BLOCKED' AND block_reason IS NULL AND blocked_by IS NULL)
 OR (allocation_kind='MAINTENANCE_BLOCK' AND num_nonnulls(booking_id,contract_id,contract_tenant_id)=0 AND status IN ('BLOCKED','RELEASED') AND nullif(btrim(block_reason),'') IS NOT NULL AND blocked_by IS NOT NULL)),
 CHECK(contract_tenant_id IS NULL OR contract_id IS NOT NULL), CHECK(status<>'HELD' OR expires_at IS NOT NULL)
);
CREATE TABLE room_slot_occupancies (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL, room_slot_id uuid,
 contract_tenant_id uuid NOT NULL, start_date date NOT NULL, end_date date NOT NULL,
 created_at timestamptz NOT NULL DEFAULT now(),
 FOREIGN KEY(contract_tenant_id,room_id) REFERENCES contract_tenants(id,room_id),
 FOREIGN KEY(room_slot_id,room_id) REFERENCES room_slots(id,room_id), CHECK(end_date>start_date)
);
CREATE TABLE services (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), branch_id uuid NOT NULL REFERENCES branches,
 code varchar(50) NOT NULL, name text NOT NULL, unit varchar(30) NOT NULL,
 service_type varchar(20) NOT NULL CHECK(service_type IN ('ELECTRICITY','WATER','INTERNET','PARKING','CLEANING','SECURITY','OTHER')),
 billing_method varchar(20) NOT NULL CHECK(billing_method IN ('METERED','PER_PERSON','PER_ROOM','FIXED','PER_USE')),
 reference_price numeric(19,4) NOT NULL CHECK(reference_price>=0), mandatory boolean NOT NULL,
 active boolean NOT NULL DEFAULT true, UNIQUE(branch_id,code), UNIQUE(id,branch_id),
 CHECK(service_type NOT IN ('ELECTRICITY','WATER') OR mandatory)
);
-- One row per effective price/configuration revision; intervals are [start_date,end_date).
CREATE TABLE room_services (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL, branch_id uuid NOT NULL, service_id uuid NOT NULL,
 start_date date NOT NULL, end_date date NOT NULL, unit_price numeric(19,4) NOT NULL CHECK(unit_price>=0),
 billing_config jsonb NOT NULL DEFAULT '{}', status varchar(20) NOT NULL CHECK(status IN ('ACTIVE','INACTIVE')),
 UNIQUE(id,room_id), FOREIGN KEY(room_id,branch_id) REFERENCES rooms(id,branch_id),
 FOREIGN KEY(service_id,branch_id) REFERENCES services(id,branch_id), CHECK(end_date>start_date)
);
CREATE TABLE contract_services (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), contract_id uuid NOT NULL, contract_tenant_id uuid,
 room_id uuid NOT NULL, room_service_id uuid NOT NULL,
 start_date date NOT NULL, end_date date NOT NULL, agreed_unit_price numeric(19,4) CHECK(agreed_unit_price>=0),
 status varchar(24) NOT NULL CHECK(status IN ('PENDING','APPROVED','REJECTED','CANCEL_REQUESTED','CANCELLED')),
 requested_by uuid NOT NULL REFERENCES users, processed_by uuid REFERENCES users, processed_at timestamptz, reason text,
 FOREIGN KEY(contract_id,room_id) REFERENCES contracts(id,room_id),
 FOREIGN KEY(contract_tenant_id,contract_id) REFERENCES contract_tenants(id,contract_id),
 FOREIGN KEY(room_service_id,room_id) REFERENCES room_services(id,room_id), CHECK(end_date>start_date)
);
-- Multiple rows (segments) may belong to the same billing_month. No meter table.
CREATE TABLE room_service_charges (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_service_id uuid NOT NULL, room_id uuid NOT NULL,
 billing_month date NOT NULL CHECK(extract(day FROM billing_month)=1),
 start_date date NOT NULL, end_date date NOT NULL, segment_number integer NOT NULL CHECK(segment_number>0),
 meter_reference text, segment_reason varchar(20) NOT NULL CHECK(segment_reason IN ('REGULAR','METER_CHANGE','METER_RESET','PRICE_CHANGE','CORRECTION')),
 start_reading numeric(19,4), end_reading numeric(19,4), quantity numeric(19,4) NOT NULL CHECK(quantity>=0),
 unit_price numeric(19,4) NOT NULL CHECK(unit_price>=0), total_amount numeric(19,0) NOT NULL CHECK(total_amount>=0),
 calculation_snapshot jsonb NOT NULL, evidence_refs jsonb NOT NULL DEFAULT '[]',
 status varchar(20) NOT NULL CHECK(status IN ('DRAFT','FINALIZED','CANCELLED')),
 finalized_by uuid REFERENCES users, finalized_at timestamptz,
 FOREIGN KEY(room_service_id,room_id) REFERENCES room_services(id,room_id), UNIQUE(id,room_id),
 UNIQUE(room_service_id,billing_month,segment_number), CHECK(end_date>start_date),
 CHECK(num_nonnulls(start_reading,end_reading) IN (0,2)),
 CHECK(start_reading IS NULL OR (start_reading>=0 AND end_reading>=start_reading AND quantity=end_reading-start_reading)),
 CHECK(total_amount=round(quantity*unit_price)),
 CHECK(start_date>=billing_month AND end_date<=(billing_month+interval '1 month')::date),
 CHECK(status<>'FINALIZED' OR (finalized_by IS NOT NULL AND finalized_at IS NOT NULL))
);
CREATE TABLE cost_allocation_proposals (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_service_charge_id uuid NOT NULL REFERENCES room_service_charges,
 method varchar(20) NOT NULL CHECK(method IN ('RESIDENT_DAYS','APPROVED_CUSTOM')),
 allocation_details jsonb NOT NULL CHECK(jsonb_typeof(allocation_details)='array'),
 calculation_snapshot jsonb NOT NULL, reason text, proposed_by uuid REFERENCES users,
 status varchar(20) NOT NULL CHECK(status IN ('PENDING','APPROVED','REJECTED','SUPERSEDED')),
 processed_by uuid REFERENCES users, processed_at timestamptz, created_at timestamptz NOT NULL DEFAULT now(),
 CHECK(status<>'APPROVED' OR (processed_by IS NOT NULL AND processed_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_approved_proposal ON cost_allocation_proposals(room_service_charge_id) WHERE status='APPROVED';
CREATE TABLE furnitures (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL REFERENCES rooms,
 asset_code varchar(50) NOT NULL, name text NOT NULL, furniture_type varchar(100), serial_number text,
 purchase_date date, asset_value numeric(19,0) CHECK(asset_value>=0), condition text,
 status varchar(20) NOT NULL CHECK(status IN ('IN_USE','DAMAGED','REPAIRING','RETIRED')), UNIQUE(room_id,asset_code), UNIQUE(id,room_id)
);
CREATE TABLE furniture_warranties (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), furniture_id uuid NOT NULL REFERENCES furnitures,
 provider text NOT NULL, start_date date NOT NULL, end_date date NOT NULL, terms text, warranty_code text, CHECK(end_date>=start_date)
);
CREATE TABLE report_requests (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), room_id uuid NOT NULL REFERENCES rooms,
 requester_user_id uuid NOT NULL REFERENCES users, contract_tenant_id uuid,
 title text NOT NULL, description text NOT NULL, priority varchar(10) NOT NULL CHECK(priority IN ('LOW','NORMAL','HIGH','URGENT')),
 status varchar(20) NOT NULL CHECK(status IN ('OPEN','ACCEPTED','IN_PROGRESS','COMPLETED','REJECTED','CANCELLED')),
 assigned_to uuid REFERENCES users, evidence_refs jsonb NOT NULL DEFAULT '[]', completed_at timestamptz,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now(), version bigint NOT NULL DEFAULT 0,
 UNIQUE(id,room_id), FOREIGN KEY(contract_tenant_id,room_id) REFERENCES contract_tenants(id,room_id)
);
CREATE TABLE report_requests_services (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), report_request_id uuid NOT NULL, room_id uuid NOT NULL, room_service_id uuid NOT NULL,
 issue_description text, UNIQUE(report_request_id,room_service_id),
 FOREIGN KEY(report_request_id,room_id) REFERENCES report_requests(id,room_id), FOREIGN KEY(room_service_id,room_id) REFERENCES room_services(id,room_id)
);
CREATE TABLE report_requests_furnitures (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), report_request_id uuid NOT NULL, room_id uuid NOT NULL, furniture_id uuid NOT NULL,
 damage_description text, UNIQUE(report_request_id,furniture_id),
 FOREIGN KEY(report_request_id,room_id) REFERENCES report_requests(id,room_id), FOREIGN KEY(furniture_id,room_id) REFERENCES furnitures(id,room_id)
);
CREATE TABLE report_records (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), report_request_id uuid NOT NULL REFERENCES report_requests,
 record_type varchar(20) NOT NULL CHECK(record_type IN ('WORK','ASSIGNMENT','STATUS','COST','LIABILITY')),
 parent_cost_record_id uuid REFERENCES report_records, contract_tenant_id uuid REFERENCES contract_tenants,
 responsible_party varchar(10) CHECK(responsible_party IN ('OWNER','TENANT')),
 amount numeric(19,0) CHECK(amount>=0), description text NOT NULL,
 performed_by uuid NOT NULL REFERENCES users, assigned_to uuid REFERENCES users, status_after varchar(20),
 approval_status varchar(20) CHECK(approval_status IN ('PENDING','APPROVED','REJECTED')),
 approved_by uuid REFERENCES users, approved_at timestamptz,
 evidence_refs jsonb NOT NULL DEFAULT '[]', occurred_at timestamptz NOT NULL DEFAULT now(),
 CHECK((record_type='LIABILITY' AND parent_cost_record_id IS NOT NULL AND responsible_party IS NOT NULL AND amount IS NOT NULL) OR
       (record_type<>'LIABILITY' AND parent_cost_record_id IS NULL AND responsible_party IS NULL AND contract_tenant_id IS NULL)),
 CHECK(record_type<>'LIABILITY' OR ((responsible_party='TENANT' AND contract_tenant_id IS NOT NULL) OR (responsible_party='OWNER' AND contract_tenant_id IS NULL))),
 CHECK(record_type NOT IN ('COST','LIABILITY') OR (amount IS NOT NULL AND approval_status IS NOT NULL)),
 CHECK(approval_status IS DISTINCT FROM 'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL))
);
CREATE TABLE invoices (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), contract_tenant_id uuid NOT NULL REFERENCES contract_tenants,
 billing_month date NOT NULL CHECK(extract(day FROM billing_month)=1), due_date date NOT NULL,
 policy_snapshot jsonb NOT NULL, document_status varchar(12) NOT NULL CHECK(document_status IN ('DRAFT','ISSUED','CANCELLED')),
 payment_environment varchar(10) NOT NULL CHECK(payment_environment IN ('REAL','SANDBOX')),
 subtotal_amount numeric(19,0) NOT NULL CHECK(subtotal_amount>=0), adjustment_amount numeric(19,0) NOT NULL,
 total_amount numeric(19,0) NOT NULL CHECK(total_amount>=0), replaces_invoice_id uuid UNIQUE REFERENCES invoices,
 issued_by uuid REFERENCES users, issued_at timestamptz, cancellation_reason text,
 adjustment_approved_by uuid REFERENCES users, adjustment_approved_at timestamptz,
 created_by uuid NOT NULL REFERENCES users, created_at timestamptz NOT NULL DEFAULT now(), version bigint NOT NULL DEFAULT 0,
 CHECK(total_amount=subtotal_amount+adjustment_amount),
 CHECK(document_status<>'ISSUED' OR (issued_by IS NOT NULL AND issued_at IS NOT NULL))
);
CREATE UNIQUE INDEX uq_member_month_invoice ON invoices(contract_tenant_id,billing_month) WHERE document_status<>'CANCELLED';
CREATE TABLE payments (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), branch_id uuid NOT NULL REFERENCES branches,
 invoice_id uuid REFERENCES invoices, booking_id uuid REFERENCES bookings,
 payer_user_id uuid REFERENCES users, payer_name_snapshot text,
 method varchar(20) NOT NULL CHECK(method IN ('MANUAL_QR','VNPAY','MOMO')),
 environment varchar(10) NOT NULL CHECK(environment IN ('REAL','SANDBOX')),
 status varchar(20) NOT NULL CHECK(status IN ('PENDING','VERIFIED','FAILED','REVIEW')),
 amount_received numeric(19,0) NOT NULL CHECK(amount_received>0),
 allocated_amount numeric(19,0) NOT NULL DEFAULT 0 CHECK(allocated_amount>=0),
 prepaid_contract_tenant_id uuid REFERENCES contract_tenants,
 prepaid_first_amount numeric(19,0) NOT NULL DEFAULT 0 CHECK(prepaid_first_amount>=0),
 prepaid_last_amount numeric(19,0) NOT NULL DEFAULT 0 CHECK(prepaid_last_amount>=0),
 unallocated_refunded numeric(19,0) NOT NULL DEFAULT 0 CHECK(unallocated_refunded>=0),
 prepaid_first_refunded numeric(19,0) NOT NULL DEFAULT 0 CHECK(prepaid_first_refunded>=0),
 prepaid_last_refunded numeric(19,0) NOT NULL DEFAULT 0 CHECK(prepaid_last_refunded>=0),
 provider_account_key text, provider_transaction_id text, idempotency_key text NOT NULL UNIQUE,
 reference_text text, evidence_refs jsonb NOT NULL DEFAULT '[]', received_at timestamptz,
 verified_by uuid REFERENCES users, verified_at timestamptz, verification_note text,
 created_at timestamptz NOT NULL DEFAULT now(), version bigint NOT NULL DEFAULT 0,
 CHECK(num_nonnulls(invoice_id,booking_id)<=1),
 CHECK(allocated_amount=0 OR num_nonnulls(invoice_id,booking_id)=1),
 CHECK(prepaid_first_amount+prepaid_last_amount=0 OR prepaid_contract_tenant_id IS NOT NULL),
 CHECK(allocated_amount+prepaid_first_amount+prepaid_last_amount+unallocated_refunded<=amount_received),
 CHECK(prepaid_first_refunded<=prepaid_first_amount AND prepaid_last_refunded<=prepaid_last_amount),
 CHECK(status='VERIFIED' OR allocated_amount+prepaid_first_amount+prepaid_last_amount+unallocated_refunded=0),
 CHECK(status<>'VERIFIED' OR (received_at IS NOT NULL AND verified_at IS NOT NULL)),
 CHECK(status<>'VERIFIED' OR method='MANUAL_QR' OR (provider_account_key IS NOT NULL AND provider_transaction_id IS NOT NULL)),
 CHECK(status<>'VERIFIED' OR method<>'MANUAL_QR' OR (verified_by IS NOT NULL AND verification_note IS NOT NULL)),
 CHECK((method='MANUAL_QR' AND environment='REAL') OR (method IN ('VNPAY','MOMO') AND environment='SANDBOX'))
);
CREATE UNIQUE INDEX uq_provider_payment ON payments(method,environment,provider_account_key,provider_transaction_id)
 WHERE provider_transaction_id IS NOT NULL;
CREATE TABLE payment_webhook_events (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), payment_id uuid REFERENCES payments,
 provider varchar(10) NOT NULL CHECK(provider IN ('VNPAY','MOMO')), environment varchar(10) NOT NULL CHECK(environment='SANDBOX'),
 provider_account_key text NOT NULL, event_key text NOT NULL, payload_hash text NOT NULL, safe_payload jsonb NOT NULL,
 signature_verified boolean NOT NULL, processing_status varchar(20) NOT NULL CHECK(processing_status IN ('RECEIVED','ACCEPTED','REJECTED','FAILED')),
 received_at timestamptz NOT NULL DEFAULT now(), processed_at timestamptz,
 UNIQUE(provider,environment,provider_account_key,event_key),
 CHECK(processing_status<>'ACCEPTED' OR (signature_verified AND payment_id IS NOT NULL AND processed_at IS NOT NULL))
);
CREATE TABLE invoice_items (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), invoice_id uuid NOT NULL REFERENCES invoices,
 line_number integer NOT NULL CHECK(line_number>0),
 item_type varchar(24) NOT NULL CHECK(item_type IN ('RENT','SERVICE','REPAIR','SURCHARGE','DISCOUNT','PREPAID_APPLICATION')),
 description text NOT NULL, quantity numeric(19,4) NOT NULL CHECK(quantity>=0),
 unit_price numeric(19,4) NOT NULL CHECK(unit_price>=0), amount numeric(19,0) NOT NULL CHECK(amount>=0),
 source_charge_id uuid REFERENCES room_service_charges, source_proposal_id uuid REFERENCES cost_allocation_proposals,
 source_report_record_id uuid REFERENCES report_records, source_payment_id uuid REFERENCES payments,
 prepaid_bucket varchar(12) CHECK(prepaid_bucket IN ('FIRST_MONTH','LAST_MONTH')),
 source_snapshot jsonb NOT NULL DEFAULT '{}', idempotency_key text UNIQUE,
 created_at timestamptz NOT NULL DEFAULT now(), reversed_at timestamptz,
 reversal_approved_by uuid REFERENCES users, reversal_reason text,
 UNIQUE(invoice_id,line_number), CHECK(amount=round(quantity*unit_price)),
 CHECK((item_type='PREPAID_APPLICATION' AND source_payment_id IS NOT NULL AND prepaid_bucket IS NOT NULL AND idempotency_key IS NOT NULL) OR
       (item_type<>'PREPAID_APPLICATION' AND source_payment_id IS NULL AND prepaid_bucket IS NULL AND reversed_at IS NULL)),
 CHECK((item_type='SERVICE')=(source_charge_id IS NOT NULL)),
 CHECK((item_type='REPAIR')=(source_report_record_id IS NOT NULL)),
 CHECK(reversed_at IS NULL OR (reversal_approved_by IS NOT NULL AND reversal_reason IS NOT NULL))
);
CREATE TABLE notifications (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), recipient_user_id uuid NOT NULL REFERENCES users,
 event_key text NOT NULL, type varchar(50) NOT NULL, title text NOT NULL, message text NOT NULL,
 reference_type text, reference_id uuid, read_at timestamptz,
 created_at timestamptz NOT NULL DEFAULT now(), UNIQUE(recipient_user_id,event_key,type)
);
CREATE TABLE audit_logs (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), actor_user_id uuid REFERENCES users,
 table_name text NOT NULL, entity_id uuid, action text NOT NULL,
 detail jsonb NOT NULL, reason text, request_key text,
 occurred_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX ix_roles_reverse ON user_roles(role_id);
CREATE INDEX ix_branches_manager ON branches(manager_id);
CREATE INDEX ix_appointment_room_time ON appointments(room_id,start_at,end_at);
CREATE INDEX ix_booking_requester ON bookings(requester_user_id,created_at DESC);
CREATE INDEX ix_booking_expiry ON bookings(temporary_hold_until) WHERE status='AWAITING_PAYMENT';
CREATE INDEX ix_contract_room ON contracts(room_id,start_date,end_date);
CREATE INDEX ix_member_tenant ON contract_tenants(tenant_user_id);
CREATE INDEX ix_allocation_conflict ON resource_allocations(room_id,start_date,end_date) WHERE status<>'RELEASED';
CREATE INDEX ix_occupancy_room_dates ON room_slot_occupancies(room_id,start_date,end_date);
CREATE INDEX ix_occupancy_member ON room_slot_occupancies(contract_tenant_id);
CREATE INDEX ix_room_services_room ON room_services(room_id,start_date,end_date);
CREATE INDEX ix_contract_services_member ON contract_services(contract_tenant_id);
CREATE INDEX ix_charge_period ON room_service_charges(room_id,billing_month);
CREATE INDEX ix_report_room_status ON report_requests(room_id,status);
CREATE INDEX ix_record_request ON report_records(report_request_id,occurred_at);
CREATE INDEX ix_invoice_due ON invoices(due_date) WHERE document_status='ISSUED';
CREATE INDEX ix_items_payment ON invoice_items(source_payment_id) WHERE source_payment_id IS NOT NULL;
CREATE INDEX ix_items_charge ON invoice_items(source_charge_id) WHERE source_charge_id IS NOT NULL;
CREATE INDEX ix_items_repair ON invoice_items(source_report_record_id) WHERE source_report_record_id IS NOT NULL;
CREATE INDEX ix_payments_invoice ON payments(invoice_id) WHERE invoice_id IS NOT NULL;
CREATE INDEX ix_payments_booking ON payments(booking_id) WHERE booking_id IS NOT NULL;
CREATE INDEX ix_payments_prepaid ON payments(prepaid_contract_tenant_id) WHERE prepaid_contract_tenant_id IS NOT NULL;
CREATE INDEX ix_payments_branch_time ON payments(branch_id,received_at);
CREATE INDEX ix_notifications_user ON notifications(recipient_user_id,created_at DESC);
CREATE INDEX ix_audit_entity ON audit_logs(table_name,entity_id,occurred_at);
CREATE UNIQUE INDEX uq_payment_audit_request ON audit_logs(entity_id,request_key)
 WHERE table_name='payments' AND action='UPDATE' AND request_key IS NOT NULL;

REVOKE ALL ON SCHEMA rental FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA rental FROM PUBLIC;
COMMIT;
