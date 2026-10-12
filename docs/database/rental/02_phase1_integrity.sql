-- Required immediately after 01_phase1.sql, before any seed/application write.
BEGIN;
SET LOCAL search_path = rental, pg_catalog;

CREATE FUNCTION rental.can_manage_branch(actor uuid, target_branch uuid) RETURNS boolean
LANGUAGE sql STABLE SET search_path=rental,pg_catalog AS $$
 SELECT EXISTS(SELECT 1 FROM branches b JOIN users u ON u.id=actor AND u.status='ACTIVE'
  WHERE b.id=target_branch AND (u.locked_until IS NULL OR u.locked_until<=now()) AND
   ((b.owner_id=actor AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=actor AND r.code='OWNER'))
    OR (b.manager_id=actor AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=actor AND r.code='MANAGER'))));
$$;
CREATE FUNCTION rental.can_read_invoice(actor uuid, target_invoice uuid) RETURNS boolean
LANGUAGE sql STABLE SET search_path=rental,pg_catalog AS $$
 SELECT EXISTS(SELECT 1 FROM invoices i JOIN contract_tenants ct ON ct.id=i.contract_tenant_id
 JOIN contracts c ON c.id=ct.contract_id
 JOIN users u ON u.id=actor AND u.status='ACTIVE'
 WHERE i.id=target_invoice AND (u.locked_until IS NULL OR u.locked_until<=now())
 AND (ct.tenant_user_id=actor OR can_manage_branch(actor,c.branch_id)));
$$;
CREATE FUNCTION rental.invoice_due_date(month_start date, collection_day integer, grace_days integer)
RETURNS date LANGUAGE sql IMMUTABLE AS $$
 SELECT (date_trunc('month',month_start)::date +
  (least(collection_day, extract(day FROM (date_trunc('month',month_start)+interval '1 month - 1 day'))::integer)-1)) + grace_days;
$$;

CREATE FUNCTION rental.audit_change() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE before_row jsonb; after_row jsonb; actor uuid;
BEGIN
 before_row := CASE WHEN TG_OP='INSERT' THEN NULL ELSE to_jsonb(OLD) END;
 after_row := CASE WHEN TG_OP='DELETE' THEN NULL ELSE to_jsonb(NEW) END;
 -- Never audit password/token/document payloads. Attach only whitelisted tables below.
 IF TG_OP='UPDATE' AND (before_row-'version'-'updated_at')=(after_row-'version'-'updated_at') THEN RETURN NEW; END IF;
 actor := nullif(current_setting('rental.actor_id',true),'')::uuid;
 INSERT INTO audit_logs(actor_user_id,table_name,entity_id,action,detail,reason,request_key)
 VALUES(actor,TG_TABLE_NAME,coalesce(after_row->>'id',before_row->>'id')::uuid,TG_OP,
 jsonb_build_object('before',before_row-'evidence_refs'-'reference_text'-'payer_name_snapshot'-'tenant_snapshot'-'handover_snapshot'-'handover_evidence_refs'-'confirmation_evidence'-'document_snapshot',
                    'after',after_row-'evidence_refs'-'reference_text'-'payer_name_snapshot'-'tenant_snapshot'-'handover_snapshot'-'handover_evidence_refs'-'confirmation_evidence'-'document_snapshot'),
 nullif(current_setting('rental.reason',true),''),nullif(current_setting('rental.request_key',true),''));
 RETURN CASE WHEN TG_OP='DELETE' THEN OLD ELSE NEW END;
END $$;
CREATE FUNCTION rental.deny_history_delete() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Historical rows in % must not be deleted/rewritten by this operation',TG_TABLE_NAME USING ERRCODE='23514'; END $$;
CREATE TRIGGER audit_append_only BEFORE UPDATE OR DELETE ON audit_logs FOR EACH ROW EXECUTE FUNCTION deny_history_delete();
-- USERS must never use audit_change(): it contains authentication secrets and PII.
CREATE FUNCTION rental.guard_user_security() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE actor uuid; prev jsonb; curr jsonb;
BEGIN
 actor:=nullif(current_setting('rental.actor_id',true),'')::uuid;
 IF NEW.owner_verification_status IN ('APPROVED','REJECTED') AND (TG_OP='INSERT' OR
  (NEW.owner_verification_status,NEW.owner_reviewed_by,NEW.owner_reviewed_at,NEW.owner_review_reason,NEW.owner_document_public_id,NEW.owner_document_resource_type)
  IS DISTINCT FROM (OLD.owner_verification_status,OLD.owner_reviewed_by,OLD.owner_reviewed_at,OLD.owner_review_reason,OLD.owner_document_public_id,OLD.owner_document_resource_type)) THEN
  IF actor IS DISTINCT FROM NEW.owner_reviewed_by OR NOT EXISTS(SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id
    WHERE u.id=actor AND u.status='ACTIVE' AND (u.locked_until IS NULL OR u.locked_until<=now()) AND r.code='ADMIN') THEN
   RAISE EXCEPTION 'Owner verification decision requires authenticated ADMIN actor' USING ERRCODE='23514'; END IF;
 END IF;
 IF NEW.owner_verification_status IN ('NOT_SUBMITTED','PENDING') AND num_nonnulls(NEW.owner_reviewed_by,NEW.owner_reviewed_at,NEW.owner_review_reason)>0 THEN
  RAISE EXCEPTION 'Pending owner review must clear previous decision fields' USING ERRCODE='23514'; END IF;
 IF TG_OP='UPDATE' THEN
  IF NEW.verification_secret_hash IS NOT NULL AND NEW.verification_secret_hash IS NOT DISTINCT FROM OLD.verification_secret_hash AND
   (NEW.verification_purpose IS DISTINCT FROM OLD.verification_purpose OR NEW.verification_expires_at IS DISTINCT FROM OLD.verification_expires_at
    OR (OLD.verification_consumed_at IS NOT NULL AND NEW.verification_consumed_at IS DISTINCT FROM OLD.verification_consumed_at)
    OR NEW.verification_attempts<OLD.verification_attempts) THEN
   RAISE EXCEPTION 'Reissue a new challenge hash; do not reuse/reset an old challenge' USING ERRCODE='23514'; END IF;
  IF NEW.verification_consumed_at IS NOT NULL AND OLD.verification_consumed_at IS NULL AND
   (NEW.verification_consumed_at>NEW.verification_expires_at OR NEW.verification_attempts>=NEW.verification_max_attempts) THEN
   RAISE EXCEPTION 'Expired/exhausted challenge cannot be consumed' USING ERRCODE='23514'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER protect_user_security BEFORE INSERT OR UPDATE ON users FOR EACH ROW EXECUTE FUNCTION guard_user_security();
CREATE FUNCTION rental.audit_user_security() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE prev jsonb; curr jsonb;
BEGIN
 prev:=CASE WHEN TG_OP='INSERT' THEN NULL ELSE jsonb_build_object('status',OLD.status,'owner_verification_status',OLD.owner_verification_status,'owner_reviewed_by',OLD.owner_reviewed_by,'owner_reviewed_at',OLD.owner_reviewed_at,'auth_version',OLD.auth_version) END;
 curr:=jsonb_build_object('status',NEW.status,'owner_verification_status',NEW.owner_verification_status,'owner_reviewed_by',NEW.owner_reviewed_by,'owner_reviewed_at',NEW.owner_reviewed_at,'auth_version',NEW.auth_version);
 IF prev IS DISTINCT FROM curr THEN
  INSERT INTO audit_logs(actor_user_id,table_name,entity_id,action,detail)
  VALUES(nullif(current_setting('rental.actor_id',true),'')::uuid,'users',NEW.id,TG_OP,jsonb_build_object('before',prev,'after',curr));
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER audit_user_security AFTER INSERT OR UPDATE ON users FOR EACH ROW EXECUTE FUNCTION audit_user_security();
CREATE FUNCTION rental.guard_agreement() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME='contract_tenants' AND OLD.status<>'DRAFT' THEN
  IF (to_jsonb(NEW)->'tenant_snapshot') IS DISTINCT FROM (to_jsonb(OLD)->'tenant_snapshot') OR
    (to_jsonb(NEW)->'tenant_user_id') IS DISTINCT FROM (to_jsonb(OLD)->'tenant_user_id') THEN
   RAISE EXCEPTION 'Signed member identity snapshot cannot be reassigned/rewritten' USING ERRCODE='23514'; END IF;
 END IF;
 IF OLD.status<>'DRAFT' AND NEW.terms IS DISTINCT FROM OLD.terms THEN
  RAISE EXCEPTION 'Preserve original agreed terms; record an approved amendment' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER protect_agreement BEFORE UPDATE ON contracts FOR EACH ROW EXECUTE FUNCTION guard_agreement();
CREATE TRIGGER protect_member_agreement BEFORE UPDATE ON contract_tenants FOR EACH ROW EXECUTE FUNCTION guard_agreement();
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['user_roles','owner_managers','branches','appointments','bookings','contracts','contract_tenants','resource_allocations','room_slot_occupancies','cost_allocation_proposals','report_records','invoices','payments','invoice_items'] LOOP
  EXECUTE format('CREATE TRIGGER audit_change AFTER INSERT OR UPDATE OR DELETE ON rental.%I FOR EACH ROW EXECUTE FUNCTION rental.audit_change()',t);
 END LOOP;
 FOREACH t IN ARRAY ARRAY['bookings','appointments','contracts','contract_tenants','resource_allocations','room_slot_occupancies','room_service_charges','cost_allocation_proposals','report_requests','report_records','invoices','payments','payment_webhook_events'] LOOP
  EXECUTE format('CREATE TRIGGER preserve_history BEFORE DELETE ON rental.%I FOR EACH ROW EXECUTE FUNCTION rental.deny_history_delete()',t);
 END LOOP;
END $$;

-- Real writes to a parent guardian row serialize competing transactions, including
-- at REPEATABLE READ (where a stale writer receives a serialization failure).
-- Backend must retry serialization/deadlock failures; lock rooms in UUID order for multi-room operations.
CREATE FUNCTION rental.lock_room_write() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
BEGIN
 IF TG_OP='UPDATE' AND NEW.room_id IS DISTINCT FROM OLD.room_id THEN
  RAISE EXCEPTION 'Move by closing history and creating a new row; room_id is immutable' USING ERRCODE='23514';
 END IF;
 UPDATE rooms SET version=version+1 WHERE id=NEW.room_id;
 RETURN NEW;
END $$;
CREATE FUNCTION rental.guard_allocation_availability() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
BEGIN
 IF TG_OP='UPDATE' AND NEW.allocation_kind<>OLD.allocation_kind THEN
  RAISE EXCEPTION 'Allocation kind is immutable; release and create a new allocation' USING ERRCODE='23514'; END IF;
 IF NEW.allocation_kind='MAINTENANCE_BLOCK' AND (TG_OP='INSERT' OR NEW.blocked_by IS DISTINCT FROM OLD.blocked_by) AND NOT EXISTS(SELECT 1 FROM rooms r WHERE r.id=NEW.room_id AND can_manage_branch(NEW.blocked_by,r.branch_id)) THEN
  RAISE EXCEPTION 'Block author cannot manage this Branch' USING ERRCODE='23514'; END IF;
 IF NEW.status IN ('HELD','LEASED') AND (TG_OP='INSERT' OR OLD.status='RELEASED' OR
  (NEW.start_date,NEW.end_date,NEW.room_slot_id) IS DISTINCT FROM (OLD.start_date,OLD.end_date,OLD.room_slot_id)) THEN
  IF NOT EXISTS(SELECT 1 FROM rooms WHERE id=NEW.room_id AND operational_status='AVAILABLE') OR
   (NEW.room_slot_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM room_slots WHERE id=NEW.room_slot_id AND status='AVAILABLE')) THEN
   RAISE EXCEPTION 'Cannot allocate an unavailable Room/Slot' USING ERRCODE='23514'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER z_guard_allocation BEFORE INSERT OR UPDATE ON resource_allocations FOR EACH ROW EXECUTE FUNCTION guard_allocation_availability();
CREATE TRIGGER lock_slot_room BEFORE UPDATE ON room_slots FOR EACH ROW EXECUTE FUNCTION lock_room_write();
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['bookings','contracts','contract_tenants','resource_allocations','room_slot_occupancies','room_services','room_service_charges'] LOOP
 EXECUTE format('CREATE TRIGGER lock_room BEFORE INSERT OR UPDATE ON rental.%I FOR EACH ROW EXECUTE FUNCTION rental.lock_room_write()',t);
 END LOOP;
END $$;

CREATE FUNCTION rental.guard_source_snapshot() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
BEGIN
 IF TG_TABLE_NAME='branch_policies' THEN
  RAISE EXCEPTION 'Create a new policy version instead of changing history' USING ERRCODE='23514';
 ELSIF TG_TABLE_NAME='room_service_charges' AND (to_jsonb(OLD)->>'status') IN ('FINALIZED','CANCELLED') THEN
  IF (to_jsonb(NEW)-'status')<>(to_jsonb(OLD)-'status') OR NOT((to_jsonb(NEW)->>'status')=(to_jsonb(OLD)->>'status') OR ((to_jsonb(OLD)->>'status')='FINALIZED' AND (to_jsonb(NEW)->>'status')='CANCELLED')) THEN
   RAISE EXCEPTION 'Finalized charge values are immutable; use cancellation/correction workflow' USING ERRCODE='23514'; END IF;
 ELSIF TG_TABLE_NAME='cost_allocation_proposals' AND (to_jsonb(OLD)->>'status') IN ('APPROVED','SUPERSEDED') THEN
  IF (to_jsonb(NEW)-'status')<>(to_jsonb(OLD)-'status') OR NOT((to_jsonb(NEW)->>'status')=(to_jsonb(OLD)->>'status') OR ((to_jsonb(OLD)->>'status')='APPROVED' AND (to_jsonb(NEW)->>'status')='SUPERSEDED')) THEN
   RAISE EXCEPTION 'Approved allocation snapshot is immutable' USING ERRCODE='23514'; END IF;
 ELSIF TG_TABLE_NAME='report_records' AND (to_jsonb(OLD)->>'approval_status')='APPROVED' AND NEW IS DISTINCT FROM OLD THEN
  RAISE EXCEPTION 'Approved repair cost/liability is immutable' USING ERRCODE='23514';
 ELSIF TG_TABLE_NAME='bookings' THEN
  IF (to_jsonb(OLD)->>'status') IN ('EXPIRED','CANCELLED','REJECTED','CONVERTED') AND (to_jsonb(NEW)->>'status')<>(to_jsonb(OLD)->>'status') THEN
   RAISE EXCEPTION 'Terminal Booking cannot be reopened' USING ERRCODE='23514'; END IF;
 END IF;
 RETURN NEW;
END $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['branch_policies','room_service_charges','cost_allocation_proposals','report_records','bookings'] LOOP
 EXECUTE format('CREATE TRIGGER preserve_source_snapshot BEFORE UPDATE ON rental.%I FOR EACH ROW EXECUTE FUNCTION rental.guard_source_snapshot()',t);
 END LOOP;
END $$;

CREATE FUNCTION rental.check_leasing_integrity() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM bookings b JOIN rooms r ON r.id=b.room_id WHERE b.status IN ('AWAITING_PAYMENT','CONFIRMED') AND b.rental_mode<>r.rental_mode) THEN
  RAISE EXCEPTION 'Booking mode differs from Room mode' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM contracts c WHERE c.status='ACTIVE' AND
  (SELECT count(*) FROM contract_tenants ct WHERE ct.contract_id=c.id AND ct.status='ACTIVE' AND ct.role='REPRESENTATIVE')<>1) THEN
  RAISE EXCEPTION 'ACTIVE Contract must have exactly one ACTIVE representative' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM contracts a JOIN contracts b ON a.room_id=b.room_id AND a.id<b.id
  WHERE a.status IN ('ACTIVE','ENDED') AND b.status IN ('ACTIVE','ENDED') AND daterange(a.start_date,a.end_date,'[)') && daterange(b.start_date,b.end_date,'[)')) THEN
  RAISE EXCEPTION 'Overlapping main Room contracts' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM contracts c JOIN rooms r ON r.id=c.room_id WHERE c.rental_mode<>r.rental_mode AND c.status='ACTIVE') THEN
  RAISE EXCEPTION 'Contract mode differs from Room mode' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM contract_tenants ct JOIN contracts c ON c.id=ct.contract_id WHERE
  ct.start_date<c.start_date OR ct.end_date>c.end_date OR
  (c.rental_mode='SLOT' AND ct.room_slot_id IS NULL) OR (c.rental_mode='ROOM' AND ct.room_slot_id IS NOT NULL)) THEN
  RAISE EXCEPTION 'Member dates or Slot violate main Contract' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM contract_tenants ct JOIN bookings b ON b.id=ct.source_booking_id JOIN contracts main ON main.id=ct.contract_id
  WHERE b.room_slot_id IS DISTINCT FROM ct.room_slot_id OR b.requester_user_id IS DISTINCT FROM ct.tenant_user_id OR b.status<>'CONVERTED' OR b.payment_environment<>main.payment_environment) THEN
  RAISE EXCEPTION 'Booking conversion/member mismatch' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM resource_allocations a JOIN resource_allocations b ON a.room_id=b.room_id AND a.id<b.id
  WHERE a.status<>'RELEASED' AND b.status<>'RELEASED'
  AND (a.room_slot_id IS NULL OR b.room_slot_id IS NULL OR a.room_slot_id=b.room_slot_id)
  AND daterange(a.start_date,a.end_date,'[)') && daterange(b.start_date,b.end_date,'[)')) THEN
  RAISE EXCEPTION 'Room/Slot allocation overlap' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM resource_allocations a JOIN bookings b ON b.id=a.booking_id WHERE a.status<>'RELEASED' AND
  (a.status<>'HELD' OR b.status NOT IN ('AWAITING_PAYMENT','CONFIRMED') OR a.room_slot_id IS DISTINCT FROM b.room_slot_id
   OR a.start_date<>b.start_date OR a.end_date<>b.end_date)) THEN
  RAISE EXCEPTION 'Booking allocation mismatch' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM resource_allocations a JOIN contracts c ON c.id=a.contract_id
  LEFT JOIN contract_tenants ct ON ct.id=a.contract_tenant_id WHERE a.status<>'RELEASED' AND
  (a.status<>'LEASED' OR c.status<>'ACTIVE' OR a.start_date<c.start_date OR a.end_date>c.end_date OR
   (c.rental_mode='ROOM' AND (a.room_slot_id IS NOT NULL OR a.contract_tenant_id IS NOT NULL)) OR
   (c.rental_mode='SLOT' AND (ct.id IS NULL OR ct.status<>'ACTIVE' OR a.room_slot_id IS DISTINCT FROM ct.room_slot_id
     OR a.start_date<ct.start_date OR a.end_date>ct.end_date)))) THEN
  RAISE EXCEPTION 'Lease allocation mismatch' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM room_slot_occupancies o JOIN contract_tenants ct ON ct.id=o.contract_tenant_id JOIN contracts c ON c.id=ct.contract_id
  WHERE o.start_date<ct.start_date OR o.end_date>coalesce(ct.actual_leave_date,ct.end_date) OR
   (c.rental_mode='SLOT' AND o.room_slot_id IS NULL) OR NOT EXISTS(
    SELECT 1 FROM resource_allocations a WHERE a.contract_id=c.id AND a.room_id=o.room_id
    AND (a.room_slot_id IS NULL OR a.room_slot_id=o.room_slot_id) AND a.start_date<=o.start_date AND a.end_date>=o.end_date
    AND (a.contract_tenant_id IS NULL OR a.contract_tenant_id=ct.id))) THEN
  RAISE EXCEPTION 'Occupancy lacks matching member/lease allocation' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM room_slot_occupancies a JOIN room_slot_occupancies b ON a.id<b.id
  WHERE (a.contract_tenant_id=b.contract_tenant_id OR (a.room_slot_id IS NOT NULL AND a.room_slot_id=b.room_slot_id))
  AND daterange(a.start_date,a.end_date,'[)') && daterange(b.start_date,b.end_date,'[)')) THEN
  RAISE EXCEPTION 'Overlapping occupancy for member or Slot' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM rooms r JOIN room_slot_occupancies boundary ON boundary.room_id=r.id
  WHERE (SELECT count(*) FROM room_slot_occupancies o WHERE o.room_id=r.id AND o.start_date<=boundary.start_date AND o.end_date>boundary.start_date)>r.capacity) THEN
  RAISE EXCEPTION 'Room occupancy exceeds capacity' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['rooms','contracts','contract_tenants','resource_allocations','room_slot_occupancies','bookings'] LOOP
 EXECUTE format('CREATE CONSTRAINT TRIGGER leasing_integrity AFTER INSERT OR UPDATE ON rental.%I DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rental.check_leasing_integrity()',t);
 END LOOP;
END $$;

-- Largest-remainder rounding; UUID ascending breaks equal remainders deterministically.
CREATE FUNCTION rental.default_charge_allocation(charge_uuid uuid)
RETURNS TABLE(contract_tenant_id uuid,resident_days bigint,amount numeric)
LANGUAGE sql STABLE SET search_path=rental,pg_catalog AS $$
 WITH weights AS (
 SELECT ct.id, sum(greatest(0,least(o.end_date,ct.end_date,coalesce(ct.actual_leave_date,ct.end_date),ch.end_date)
                      -greatest(o.start_date,ct.start_date,ch.start_date)))::bigint days, ch.total_amount total
 FROM room_service_charges ch JOIN room_slot_occupancies o ON o.room_id=ch.room_id
 JOIN contract_tenants ct ON ct.id=o.contract_tenant_id
 WHERE ch.id=charge_uuid AND ct.status<>'CANCELLED' GROUP BY ct.id,ch.total_amount
 ), raw AS (SELECT *,total*days/nullif(sum(days) OVER(),0) share FROM weights WHERE days>0),
 rounded AS (SELECT *,floor(share) base, row_number() OVER(ORDER BY share-floor(share) DESC,id) rank,
 total-sum(floor(share)) OVER() remainder FROM raw)
 SELECT id,days,base+CASE WHEN rank<=remainder THEN 1 ELSE 0 END FROM rounded;
$$;

CREATE FUNCTION rental.check_utilities() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE p record; ch record; entry jsonb; member uuid; allocation_sum numeric; seen uuid[]; expected record;
BEGIN
 IF EXISTS(SELECT 1 FROM contract_services cs JOIN room_services rs ON rs.id=cs.room_service_id JOIN services s ON s.id=rs.service_id
   WHERE s.mandatory AND cs.status IN ('CANCEL_REQUESTED','CANCELLED')) THEN
  RAISE EXCEPTION 'Mandatory service cannot be cancelled by Tenant workflow' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM room_services a JOIN room_services b ON a.room_id=b.room_id AND a.service_id=b.service_id AND a.id<b.id
  WHERE a.status='ACTIVE' AND b.status='ACTIVE' AND daterange(a.start_date,a.end_date,'[)') && daterange(b.start_date,b.end_date,'[)')) THEN
  RAISE EXCEPTION 'Overlapping Room service price revisions' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM room_service_charges a JOIN room_service_charges b ON a.room_service_id=b.room_service_id AND a.id<b.id
  WHERE a.status<>'CANCELLED' AND b.status<>'CANCELLED' AND daterange(a.start_date,a.end_date,'[)') && daterange(b.start_date,b.end_date,'[)')) THEN
  RAISE EXCEPTION 'Overlapping service charge segments' USING ERRCODE='23514'; END IF;
 FOR p IN SELECT * FROM cost_allocation_proposals WHERE status='APPROVED' LOOP
  SELECT * INTO ch FROM room_service_charges WHERE id=p.room_service_charge_id;
  IF ch.status<>'FINALIZED' OR jsonb_array_length(p.allocation_details)=0 THEN
   RAISE EXCEPTION 'Approved allocation requires finalized charge and positive resident weights' USING ERRCODE='23514'; END IF;
  allocation_sum:=0; seen:='{}';
  FOR entry IN SELECT value FROM jsonb_array_elements(p.allocation_details) LOOP
   IF NOT(entry ?& ARRAY['contract_tenant_id','amount']) OR jsonb_typeof(entry->'amount')<>'number' THEN
    RAISE EXCEPTION 'Invalid allocation JSON shape' USING ERRCODE='23514'; END IF;
   member:=(entry->>'contract_tenant_id')::uuid;
   IF member=ANY(seen) OR (entry->>'amount')::numeric<0 OR (entry->>'amount')::numeric<>trunc((entry->>'amount')::numeric) THEN
    RAISE EXCEPTION 'Duplicate member or invalid VND allocation' USING ERRCODE='23514'; END IF;
   seen:=array_append(seen,member);
   SELECT * INTO expected FROM default_charge_allocation(ch.id) d WHERE d.contract_tenant_id=member;
   IF NOT FOUND THEN RAISE EXCEPTION 'Allocation member has no eligible resident days in Room/period' USING ERRCODE='23514'; END IF;
   IF p.method='RESIDENT_DAYS' AND ((entry->>'amount')::numeric<>expected.amount OR
       coalesce((entry->>'resident_days')::bigint,-1)<>expected.resident_days) THEN
    RAISE EXCEPTION 'Default resident-day allocation/rounding mismatch' USING ERRCODE='23514'; END IF;
   allocation_sum:=allocation_sum+(entry->>'amount')::numeric;
  END LOOP;
  IF allocation_sum<>ch.total_amount OR cardinality(seen)<>(SELECT count(*) FROM default_charge_allocation(ch.id)) THEN
   RAISE EXCEPTION 'Allocation must cover all eligible members and sum to source total' USING ERRCODE='23514'; END IF;
 END LOOP;
 RETURN NULL;
END $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['services','contract_services','room_services','room_service_charges','cost_allocation_proposals','room_slot_occupancies','contract_tenants'] LOOP
 EXECUTE format('CREATE CONSTRAINT TRIGGER utilities_integrity AFTER INSERT OR UPDATE ON rental.%I DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rental.check_utilities()',t);
 END LOOP;
END $$;

CREATE FUNCTION rental.lock_financial_branch() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE target uuid; inv uuid; payment_uuid uuid;
BEGIN
 IF TG_TABLE_NAME IN ('payments','bookings','contracts') THEN target:=(to_jsonb(NEW)->>'branch_id')::uuid;
 ELSIF TG_TABLE_NAME IN ('contract_tenants','room_slot_occupancies','room_service_charges') THEN SELECT r.branch_id INTO target FROM rooms r WHERE r.id=(to_jsonb(NEW)->>'room_id')::uuid;
 ELSIF TG_TABLE_NAME='cost_allocation_proposals' THEN SELECT r.branch_id INTO target FROM room_service_charges ch JOIN rooms r ON r.id=ch.room_id WHERE ch.id=NEW.room_service_charge_id;
 ELSIF TG_TABLE_NAME='invoices' THEN SELECT c.branch_id INTO target FROM contract_tenants ct JOIN contracts c ON c.id=ct.contract_id WHERE ct.id=NEW.contract_tenant_id;
 ELSIF TG_TABLE_NAME='invoice_items' THEN
  inv:=CASE WHEN TG_OP='DELETE' THEN OLD.invoice_id ELSE NEW.invoice_id END;
  SELECT c.branch_id INTO target FROM invoices i JOIN contract_tenants ct ON ct.id=i.contract_tenant_id JOIN contracts c ON c.id=ct.contract_id WHERE i.id=inv;
 ELSE
  SELECT r.branch_id INTO target FROM report_requests rr JOIN rooms r ON r.id=rr.room_id WHERE rr.id=NEW.report_request_id;
 END IF;
 UPDATE branches SET version=version+1 WHERE id=target;
 RETURN CASE WHEN TG_OP='DELETE' THEN OLD ELSE NEW END;
END $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['payments','invoices','invoice_items','report_records','bookings','contracts','contract_tenants','room_slot_occupancies','room_service_charges','cost_allocation_proposals'] LOOP
 EXECUTE format('CREATE TRIGGER financial_lock BEFORE INSERT OR UPDATE OR DELETE ON rental.%I FOR EACH ROW EXECUTE FUNCTION rental.lock_financial_branch()',t);
 END LOOP;
END $$;

CREATE FUNCTION rental.guard_invoice() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE b branches; actor uuid;
BEGIN
 SELECT br.* INTO b FROM contract_tenants ct JOIN contracts c ON c.id=ct.contract_id JOIN branches br ON br.id=c.branch_id WHERE ct.id=NEW.contract_tenant_id;
 IF TG_OP='UPDATE' AND OLD.document_status IN ('ISSUED','CANCELLED') THEN
  IF (to_jsonb(NEW)-ARRAY['document_status','cancellation_reason','adjustment_approved_by','adjustment_approved_at','version']) IS DISTINCT FROM
     (to_jsonb(OLD)-ARRAY['document_status','cancellation_reason','adjustment_approved_by','adjustment_approved_at','version']) THEN
   RAISE EXCEPTION 'Issued invoice snapshot is immutable' USING ERRCODE='23514'; END IF;
  IF OLD.document_status='CANCELLED' AND NEW.document_status<>'CANCELLED' THEN RAISE EXCEPTION 'Cancelled invoice cannot reopen' USING ERRCODE='23514'; END IF;
  IF NEW.document_status<>OLD.document_status AND NOT(NEW.document_status='CANCELLED' AND NEW.adjustment_approved_by=b.owner_id
     AND NEW.adjustment_approved_at IS NOT NULL AND NEW.cancellation_reason IS NOT NULL) THEN
   RAISE EXCEPTION 'Issued invoice cancellation requires Owner approval and reason' USING ERRCODE='23514'; END IF;
 END IF;
 IF NEW.document_status='ISSUED' AND NOT can_manage_branch(NEW.issued_by,b.id) THEN RAISE EXCEPTION 'Issuer cannot manage Branch' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER immutable_invoice BEFORE INSERT OR UPDATE ON invoices FOR EACH ROW EXECUTE FUNCTION guard_invoice();

CREATE FUNCTION rental.guard_invoice_item() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE inv invoices; row_data invoice_items; member_row contract_tenants; eligible date;
BEGIN
 row_data:=CASE WHEN TG_OP='DELETE' THEN OLD ELSE NEW END;
 SELECT * INTO inv FROM invoices WHERE id=row_data.invoice_id;
 IF TG_OP='INSERT' AND row_data.item_type='PREPAID_APPLICATION' THEN
  SELECT * INTO member_row FROM contract_tenants WHERE id=inv.contract_tenant_id;
  eligible:=CASE WHEN row_data.prepaid_bucket='FIRST_MONTH' THEN date_trunc('month',member_row.start_date)::date ELSE date_trunc('month',member_row.end_date-1)::date END;
  IF inv.billing_month<>eligible THEN RAISE EXCEPTION 'Prepaid bucket targets wrong agreed month' USING ERRCODE='23514'; END IF;
  row_data.source_snapshot:=row_data.source_snapshot||jsonb_build_object('eligible_month',eligible,'member_start_date',member_row.start_date,'member_end_date',member_row.end_date);
 END IF;
 IF TG_OP='UPDATE' AND NEW.invoice_id<>OLD.invoice_id THEN RAISE EXCEPTION 'Cannot move invoice item' USING ERRCODE='23514'; END IF;
 IF row_data.item_type<>'PREPAID_APPLICATION' AND inv.document_status<>'DRAFT' THEN
  RAISE EXCEPTION 'Issued/cancelled charge items are immutable' USING ERRCODE='23514'; END IF;
 IF TG_OP IN ('UPDATE','DELETE') AND OLD.item_type='PREPAID_APPLICATION' THEN
  IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Reverse prepaid application instead of deleting' USING ERRCODE='23514'; END IF;
  IF (to_jsonb(NEW)-ARRAY['reversed_at','reversal_approved_by','reversal_reason'])<>(to_jsonb(OLD)-ARRAY['reversed_at','reversal_approved_by','reversal_reason'])
    OR OLD.reversed_at IS NOT NULL OR NEW.reversed_at IS NULL THEN
   RAISE EXCEPTION 'Prepaid application is append-only except approved reversal' USING ERRCODE='23514'; END IF;
  IF NOT EXISTS(SELECT 1 FROM contract_tenants ct JOIN contracts c ON c.id=ct.contract_id WHERE ct.id=inv.contract_tenant_id AND c.owner_id=NEW.reversal_approved_by) THEN
   RAISE EXCEPTION 'Only Owner can approve prepaid reversal' USING ERRCODE='23514'; END IF;
 END IF;
 RETURN row_data;
END $$;
CREATE TRIGGER immutable_invoice_item BEFORE INSERT OR UPDATE OR DELETE ON invoice_items FOR EACH ROW EXECUTE FUNCTION guard_invoice_item();

CREATE FUNCTION rental.guard_payment() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE b branches; ref_booking bookings;
BEGIN
 SELECT * INTO b FROM branches WHERE id=NEW.branch_id;
 IF TG_OP='UPDATE' AND OLD.status='VERIFIED' THEN
  IF NEW.status<>'VERIFIED' OR NEW.amount_received<>OLD.amount_received OR NEW.environment<>OLD.environment OR NEW.method<>OLD.method
   OR NEW.branch_id<>OLD.branch_id OR NEW.received_at IS DISTINCT FROM OLD.received_at OR NEW.provider_transaction_id IS DISTINCT FROM OLD.provider_transaction_id THEN
   RAISE EXCEPTION 'Verified cash receipt is immutable; track approved allocations/refunds separately' USING ERRCODE='23514'; END IF;
  IF NEW.unallocated_refunded<OLD.unallocated_refunded OR NEW.prepaid_first_refunded<OLD.prepaid_first_refunded OR NEW.prepaid_last_refunded<OLD.prepaid_last_refunded THEN
   RAISE EXCEPTION 'Refund counters cannot decrease' USING ERRCODE='23514'; END IF;
  IF NEW.unallocated_refunded+NEW.prepaid_first_refunded+NEW.prepaid_last_refunded > OLD.unallocated_refunded+OLD.prepaid_first_refunded+OLD.prepaid_last_refunded THEN
   IF nullif(current_setting('rental.actor_id',true),'')::uuid IS DISTINCT FROM b.owner_id OR nullif(current_setting('rental.reason',true),'') IS NULL
      OR nullif(current_setting('rental.request_key',true),'') IS NULL THEN
    RAISE EXCEPTION 'Refund requires Owner actor, approved reason and idempotent request_key' USING ERRCODE='23514'; END IF;
  END IF;
 END IF;
 IF NEW.status='VERIFIED' AND NEW.method='MANUAL_QR' AND NOT can_manage_branch(NEW.verified_by,NEW.branch_id) THEN
  RAISE EXCEPTION 'Manual verification requires assigned Owner/Manager' USING ERRCODE='23514'; END IF;
 IF NEW.booking_id IS NOT NULL AND NEW.allocated_amount>0 THEN
  SELECT * INTO ref_booking FROM bookings WHERE id=NEW.booking_id;
  IF (TG_OP='INSERT' OR NEW.allocated_amount>OLD.allocated_amount) AND
    (ref_booking.status NOT IN ('AWAITING_PAYMENT','CONFIRMED') OR NEW.received_at>ref_booking.payment_due_at) THEN
   RAISE EXCEPTION 'Late/expired Booking payment must stay unallocated for review' USING ERRCODE='23514'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER protect_payment BEFORE INSERT OR UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION guard_payment();

CREATE FUNCTION rental.check_finance() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE inv record; p record; item record; ch record; proposal record; liability record; ct record;
 debit numeric; discounts numeric; credits numeric; direct_paid numeric; expected_amount numeric;
BEGIN
 FOR inv IN SELECT * FROM invoices LOOP
  IF NOT EXISTS(SELECT 1 FROM contract_tenants m JOIN contracts c ON c.id=m.contract_id WHERE m.id=inv.contract_tenant_id AND c.payment_environment=inv.payment_environment) THEN
   RAISE EXCEPTION 'Invoice environment differs from main Contract' USING ERRCODE='23514'; END IF;
  SELECT coalesce(sum(amount) FILTER(WHERE item_type NOT IN ('DISCOUNT','PREPAID_APPLICATION')),0),
         coalesce(sum(amount) FILTER(WHERE item_type='DISCOUNT'),0),
         coalesce(sum(amount) FILTER(WHERE item_type='PREPAID_APPLICATION' AND reversed_at IS NULL),0)
   INTO debit,discounts,credits FROM invoice_items WHERE invoice_id=inv.id;
  SELECT coalesce(sum(allocated_amount),0) INTO direct_paid FROM payments WHERE invoice_id=inv.id AND status='VERIFIED';
  IF inv.document_status='ISSUED' AND (inv.subtotal_amount<>debit OR inv.adjustment_amount<>-discounts OR inv.total_amount<>debit-discounts) THEN
   RAISE EXCEPTION 'Invoice totals do not match charge items' USING ERRCODE='23514'; END IF;
  IF credits+direct_paid>inv.total_amount OR (inv.document_status<>'ISSUED' AND credits+direct_paid>0) THEN
   RAISE EXCEPTION 'Settlement exceeds debt or targets non-issued Invoice' USING ERRCODE='23514'; END IF;
  IF inv.replaces_invoice_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM invoices prev WHERE prev.id=inv.replaces_invoice_id AND prev.document_status='CANCELLED'
    AND prev.contract_tenant_id=inv.contract_tenant_id AND prev.billing_month=inv.billing_month) THEN
   RAISE EXCEPTION 'Replacement Invoice must match cancelled member/month' USING ERRCODE='23514'; END IF;
 END LOOP;
 FOR p IN SELECT * FROM payments LOOP
  IF p.invoice_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM invoices i JOIN contract_tenants m ON m.id=i.contract_tenant_id JOIN contracts c ON c.id=m.contract_id
    WHERE i.id=p.invoice_id AND c.branch_id=p.branch_id AND i.payment_environment=p.environment) THEN
   RAISE EXCEPTION 'Payment invoice scope/environment mismatch' USING ERRCODE='23514'; END IF;
  IF p.booking_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM bookings b WHERE b.id=p.booking_id AND b.branch_id=p.branch_id AND b.payment_environment=p.environment) THEN
   RAISE EXCEPTION 'Payment Booking scope/environment mismatch' USING ERRCODE='23514'; END IF;
  IF p.prepaid_contract_tenant_id IS NOT NULL AND NOT EXISTS(
   SELECT 1 FROM contract_tenants m JOIN contracts c ON c.id=m.contract_id WHERE m.id=p.prepaid_contract_tenant_id AND c.branch_id=p.branch_id AND c.payment_environment=p.environment
   AND ((p.booking_id IS NOT NULL AND m.source_booking_id=p.booking_id) OR
        (p.invoice_id IS NOT NULL AND EXISTS(SELECT 1 FROM invoices i WHERE i.id=p.invoice_id AND i.contract_tenant_id=m.id)))) THEN
   RAISE EXCEPTION 'Prepaid funds must trace to the same member Invoice or converted Booking' USING ERRCODE='23514'; END IF;
  SELECT coalesce(sum(amount) FILTER(WHERE prepaid_bucket='FIRST_MONTH'),0),coalesce(sum(amount) FILTER(WHERE prepaid_bucket='LAST_MONTH'),0)
  INTO debit,credits FROM invoice_items WHERE source_payment_id=p.id AND reversed_at IS NULL;
  IF debit+p.prepaid_first_refunded>p.prepaid_first_amount OR credits+p.prepaid_last_refunded>p.prepaid_last_amount THEN
   RAISE EXCEPTION 'Prepaid application/refund exceeds reserved source funds' USING ERRCODE='23514'; END IF;
  IF p.status='VERIFIED' AND p.method IN ('VNPAY','MOMO') AND NOT EXISTS(
   SELECT 1 FROM payment_webhook_events w WHERE w.payment_id=p.id AND w.provider=p.method AND w.environment=p.environment
    AND w.processing_status='ACCEPTED' AND w.signature_verified) THEN
   RAISE EXCEPTION 'Verified Sandbox payment lacks accepted callback evidence' USING ERRCODE='23514'; END IF;
 END LOOP;
 FOR item IN SELECT it.*,i.contract_tenant_id,i.billing_month,i.payment_environment,i.document_status
  FROM invoice_items it JOIN invoices i ON i.id=it.invoice_id LOOP
  SELECT * INTO ct FROM contract_tenants WHERE id=item.contract_tenant_id;
  IF item.item_type='PREPAID_APPLICATION' AND item.reversed_at IS NULL THEN
   SELECT * INTO p FROM payments WHERE id=item.source_payment_id;
   IF item.document_status<>'ISSUED' OR p.status<>'VERIFIED' OR p.prepaid_contract_tenant_id IS DISTINCT FROM item.contract_tenant_id
    OR p.environment<>item.payment_environment OR
    item.billing_month IS DISTINCT FROM (item.source_snapshot->>'eligible_month')::date THEN
    RAISE EXCEPTION 'Invalid prepaid source, beneficiary, period or environment' USING ERRCODE='23514'; END IF;
  END IF;
  IF item.item_type='SERVICE' AND item.document_status<>'CANCELLED' THEN
   SELECT * INTO ch FROM room_service_charges WHERE id=item.source_charge_id;
   SELECT * INTO proposal FROM cost_allocation_proposals WHERE id=item.source_proposal_id;
   IF ch.status<>'FINALIZED' OR ch.room_id<>ct.room_id OR ch.billing_month<>item.billing_month OR proposal.id IS NULL
    OR proposal.room_service_charge_id<>ch.id OR proposal.status NOT IN ('APPROVED','SUPERSEDED') THEN
    RAISE EXCEPTION 'Service item requires a finalized matching charge/proposal' USING ERRCODE='23514'; END IF;
   SELECT (e->>'amount')::numeric INTO expected_amount FROM jsonb_array_elements(proposal.allocation_details) e WHERE (e->>'contract_tenant_id')::uuid=ct.id;
   IF expected_amount IS NULL OR expected_amount<>item.amount THEN RAISE EXCEPTION 'Service item differs from approved member allocation' USING ERRCODE='23514'; END IF;
   IF (SELECT count(*) FROM invoice_items x JOIN invoices i ON i.id=x.invoice_id WHERE x.source_charge_id=ch.id AND i.contract_tenant_id=ct.id AND i.document_status<>'CANCELLED')>1 THEN
    RAISE EXCEPTION 'Service charge billed twice to member' USING ERRCODE='23514'; END IF;
  END IF;
  IF item.item_type='REPAIR' AND item.document_status<>'CANCELLED' THEN
   SELECT * INTO liability FROM report_records WHERE id=item.source_report_record_id;
   IF liability.record_type<>'LIABILITY' OR liability.responsible_party<>'TENANT' OR liability.approval_status<>'APPROVED'
    OR liability.contract_tenant_id IS DISTINCT FROM ct.id OR liability.amount<>item.amount
    OR NOT EXISTS(SELECT 1 FROM report_records parent WHERE parent.id=liability.parent_cost_record_id AND parent.record_type='COST' AND parent.approval_status='APPROVED') THEN
    RAISE EXCEPTION 'Repair item requires approved matching Tenant liability' USING ERRCODE='23514'; END IF;
   IF (SELECT count(*) FROM invoice_items x JOIN invoices i ON i.id=x.invoice_id WHERE x.source_report_record_id=liability.id AND i.document_status<>'CANCELLED')>1 THEN
    RAISE EXCEPTION 'Repair liability billed twice' USING ERRCODE='23514'; END IF;
  END IF;
 END LOOP;
 IF EXISTS(SELECT 1 FROM invoices i
  WHERE (SELECT coalesce(sum(it.amount),0) FROM invoice_items it WHERE it.invoice_id=i.id AND it.item_type='PREPAID_APPLICATION' AND it.reversed_at IS NULL)
   >(SELECT coalesce(sum(it.amount),0) FROM invoice_items it WHERE it.invoice_id=i.id AND it.item_type='RENT')) THEN
  RAISE EXCEPTION 'Prepaid rent cannot settle more than rent lines' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM bookings b WHERE b.status='CONFIRMED' AND
  (SELECT coalesce(sum(bp.allocated_amount),0) FROM payments bp WHERE bp.booking_id=b.id AND bp.status='VERIFIED')<b.hold_amount) THEN
  RAISE EXCEPTION 'Booking requires sufficient verified funds before CONFIRMED' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM bookings b WHERE
  (SELECT coalesce(sum(bp.allocated_amount),0) FROM payments bp WHERE bp.booking_id=b.id AND bp.status='VERIFIED')>b.hold_amount) THEN
  RAISE EXCEPTION 'Booking overpayment must remain unallocated' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM contract_tenants member WHERE member.status='ACTIVE' AND
  ((SELECT coalesce(sum(pp.prepaid_first_amount-pp.prepaid_first_refunded),0) FROM payments pp WHERE pp.prepaid_contract_tenant_id=member.id AND pp.status='VERIFIED')<member.prepaid_first_required
   OR (SELECT coalesce(sum(pp.prepaid_last_amount-pp.prepaid_last_refunded),0) FROM payments pp WHERE pp.prepaid_contract_tenant_id=member.id AND pp.status='VERIFIED')<member.prepaid_last_required)) THEN
  RAISE EXCEPTION 'ACTIVE member requires verified first/last rent prepayment' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['contracts','invoices','invoice_items','payments','payment_webhook_events','bookings','contract_tenants','cost_allocation_proposals','room_service_charges','report_records'] LOOP
 EXECUTE format('CREATE CONSTRAINT TRIGGER financial_integrity AFTER INSERT OR UPDATE OR DELETE ON rental.%I DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rental.check_finance()',t);
 END LOOP;
END $$;

CREATE FUNCTION rental.check_repair_costs() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM report_records l JOIN report_records c ON c.id=l.parent_cost_record_id
  JOIN report_requests r ON r.id=l.report_request_id LEFT JOIN contract_tenants ct ON ct.id=l.contract_tenant_id
  WHERE l.record_type='LIABILITY' AND (c.record_type<>'COST' OR c.report_request_id<>l.report_request_id OR
   (l.responsible_party='TENANT' AND ct.room_id<>r.room_id))) THEN
  RAISE EXCEPTION 'Repair liability source/member scope mismatch' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM report_records c WHERE c.record_type='COST' AND c.approval_status='APPROVED'
  AND (SELECT coalesce(sum(l.amount),0) FROM report_records l WHERE l.parent_cost_record_id=c.id AND l.approval_status='APPROVED')<>c.amount) THEN
  RAISE EXCEPTION 'Approved repair liabilities must sum to approved cost' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER repair_integrity AFTER INSERT OR UPDATE ON report_records DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_repair_costs();

CREATE VIEW rental.invoice_balances AS
 SELECT i.id invoice_id,i.contract_tenant_id,i.billing_month,i.document_status,i.payment_environment,i.total_amount,i.due_date,
 coalesce(p.paid,0)+coalesce(a.applied,0) settled_amount,
 i.total_amount-coalesce(p.paid,0)-coalesce(a.applied,0) outstanding_amount,
 CASE WHEN i.document_status<>'ISSUED' THEN 'NOT_PAYABLE' WHEN coalesce(p.paid,0)+coalesce(a.applied,0)=i.total_amount THEN 'PAID'
  WHEN coalesce(p.paid,0)+coalesce(a.applied,0)>0 THEN 'PARTIALLY_PAID' ELSE 'UNPAID' END payment_status
 FROM invoices i LEFT JOIN (SELECT invoice_id,sum(allocated_amount) paid FROM payments WHERE status='VERIFIED' GROUP BY invoice_id) p ON p.invoice_id=i.id
 LEFT JOIN (SELECT invoice_id,sum(amount) applied FROM invoice_items WHERE item_type='PREPAID_APPLICATION' AND reversed_at IS NULL GROUP BY invoice_id) a ON a.invoice_id=i.id;
CREATE VIEW rental.prepaid_balances AS
 SELECT p.id payment_id,p.prepaid_contract_tenant_id,p.environment,
 p.prepaid_first_amount-p.prepaid_first_refunded-coalesce(a.first_used,0) first_remaining,
 p.prepaid_last_amount-p.prepaid_last_refunded-coalesce(a.last_used,0) last_remaining,
 p.amount_received-p.allocated_amount-p.prepaid_first_amount-p.prepaid_last_amount-p.unallocated_refunded unallocated_amount
 FROM payments p LEFT JOIN (SELECT source_payment_id,
 sum(amount) FILTER(WHERE prepaid_bucket='FIRST_MONTH') first_used,
 sum(amount) FILTER(WHERE prepaid_bucket='LAST_MONTH') last_used FROM invoice_items WHERE item_type='PREPAID_APPLICATION' AND reversed_at IS NULL GROUP BY source_payment_id) a ON a.source_payment_id=p.id
 WHERE p.status='VERIFIED';
CREATE VIEW rental.real_cash_by_branch AS
 SELECT branch_id,sum(amount_received) actual_received,
 sum(unallocated_refunded+prepaid_first_refunded+prepaid_last_refunded) refunded,
 sum(amount_received-unallocated_refunded-prepaid_first_refunded-prepaid_last_refunded) net_cash
 FROM payments WHERE status='VERIFIED' AND environment='REAL' GROUP BY branch_id;
CREATE VIEW rental.branch_funds AS
 SELECT p.branch_id,p.environment,sum(p.amount_received) received,
 sum(p.unallocated_refunded+p.prepaid_first_refunded+p.prepaid_last_refunded) refunded,
 sum(CASE WHEN p.booking_id IS NOT NULL THEN p.allocated_amount ELSE 0 END) booking_hold,
 sum(b.first_remaining+b.last_remaining) unused_prepaid,sum(b.unallocated_amount) unmatched_or_excess
 FROM payments p JOIN prepaid_balances b ON b.payment_id=p.id GROUP BY p.branch_id,p.environment;
CREATE VIEW rental.invoiced_by_period AS
 SELECT c.branch_id,i.payment_environment,i.billing_month,sum(i.total_amount) invoiced,
 sum(b.outstanding_amount) outstanding,
 sum(coalesce(items.rent,0)) period_rent,sum(coalesce(items.services,0)) period_services,
 sum(coalesce(items.repairs,0)) repair_recoveries,sum(coalesce(items.surcharges,0)) surcharges,sum(coalesce(items.discounts,0)) discounts
 FROM invoices i JOIN invoice_balances b ON b.invoice_id=i.id JOIN contract_tenants ct ON ct.id=i.contract_tenant_id JOIN contracts c ON c.id=ct.contract_id
 LEFT JOIN (SELECT invoice_id,sum(amount) FILTER(WHERE item_type='RENT') rent,sum(amount) FILTER(WHERE item_type='SERVICE') services,
 sum(amount) FILTER(WHERE item_type='REPAIR') repairs,sum(amount) FILTER(WHERE item_type='SURCHARGE') surcharges,sum(amount) FILTER(WHERE item_type='DISCOUNT') discounts
 FROM invoice_items GROUP BY invoice_id) items ON items.invoice_id=i.id WHERE i.document_status='ISSUED'
 GROUP BY c.branch_id,i.payment_environment,i.billing_month;

-- Backend-only schema. Users/roles here are application identities, not PostgreSQL logins.
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA rental FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA rental FROM PUBLIC;
DO $$ DECLARE r text; BEGIN
 FOREACH r IN ARRAY ARRAY['anon','authenticated','service_role'] LOOP
  IF EXISTS(SELECT 1 FROM pg_roles WHERE rolname=r) THEN
   EXECUTE format('REVOKE ALL ON SCHEMA rental FROM %I',r);
   EXECUTE format('REVOKE ALL ON ALL TABLES IN SCHEMA rental FROM %I',r);
   EXECUTE format('REVOKE ALL ON ALL FUNCTIONS IN SCHEMA rental FROM %I',r);
  END IF;
 END LOOP;
END $$;
COMMIT;
