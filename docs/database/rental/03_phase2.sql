-- Optional Phase 2 schema extensions. Features are NOT implemented by this DDL.
BEGIN;
SET LOCAL search_path=rental,pg_catalog;
CREATE TABLE contract_amendments (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), contract_id uuid NOT NULL REFERENCES contracts,
 contract_tenant_id uuid, amendment_number integer NOT NULL CHECK(amendment_number>0),
 effective_from date NOT NULL, previous_terms jsonb NOT NULL, new_terms jsonb NOT NULL,
 reason text NOT NULL, status varchar(20) NOT NULL CHECK(status IN ('DRAFT','APPROVED','REJECTED')),
 approved_by uuid REFERENCES users, approved_at timestamptz, created_at timestamptz NOT NULL DEFAULT now(),
 FOREIGN KEY(contract_tenant_id,contract_id) REFERENCES contract_tenants(id,contract_id),
 UNIQUE(contract_id,amendment_number), CHECK(status<>'APPROVED' OR (approved_by IS NOT NULL AND approved_at IS NOT NULL))
);
ALTER TABLE report_requests ADD COLUMN sla_due_at timestamptz;
ALTER TABLE report_requests ADD COLUMN rating integer CHECK(rating BETWEEN 1 AND 5);
ALTER TABLE report_requests ADD COLUMN rating_comment text;
ALTER TABLE report_requests ADD COLUMN rated_at timestamptz;
ALTER TABLE users ADD COLUMN notification_preferences jsonb NOT NULL DEFAULT '{}' CHECK(jsonb_typeof(notification_preferences)='object');
ALTER TABLE contract_tenants
 ADD COLUMN move_out_requested_at timestamptz,
 ADD COLUMN requested_leave_date date,
 ADD COLUMN move_out_reason text,
 ADD COLUMN move_out_status varchar(20) CHECK(move_out_status IN ('REQUESTED','APPROVED','REJECTED','COMPLETED','CANCELLED')),
 ADD COLUMN move_out_processed_by uuid REFERENCES users,
 ADD COLUMN move_out_processed_at timestamptz,
 ADD COLUMN handover_at timestamptz,
 ADD COLUMN handover_by uuid REFERENCES users,
 ADD COLUMN handover_snapshot jsonb CHECK(jsonb_typeof(handover_snapshot)='object'),
 ADD COLUMN handover_evidence_refs jsonb NOT NULL DEFAULT '[]' CHECK(jsonb_typeof(handover_evidence_refs)='array'),
 ADD COLUMN prepaid_last_disposition varchar(20) CHECK(prepaid_last_disposition IN ('PENDING','REFUND','REALLOCATE','RETAIN')),
 ADD COLUMN disposition_amount numeric(19,0) CHECK(disposition_amount>=0),
 ADD COLUMN disposition_reason text,
 ADD COLUMN disposition_approved_by uuid REFERENCES users,
 ADD COLUMN disposition_approved_at timestamptz,
 ADD CONSTRAINT ck_moveout_request CHECK(move_out_status IS NULL OR (move_out_requested_at IS NOT NULL AND requested_leave_date IS NOT NULL AND move_out_reason IS NOT NULL)),
 ADD CONSTRAINT ck_moveout_decision CHECK(move_out_status IS NULL OR move_out_status NOT IN ('APPROVED','REJECTED','COMPLETED') OR (move_out_processed_by IS NOT NULL AND move_out_processed_at IS NOT NULL)),
 ADD CONSTRAINT ck_moveout_completed CHECK(move_out_status IS DISTINCT FROM 'COMPLETED' OR (status='LEFT' AND actual_leave_date IS NOT NULL AND handover_at IS NOT NULL)),
 ADD CONSTRAINT ck_handover CHECK(num_nonnulls(handover_at,handover_by,handover_snapshot) IN (0,3)),
 ADD CONSTRAINT ck_disposition CHECK(prepaid_last_disposition IS NULL OR prepaid_last_disposition='PENDING' OR
  (disposition_amount IS NOT NULL AND nullif(btrim(disposition_reason),'') IS NOT NULL AND disposition_approved_by IS NOT NULL AND disposition_approved_by=owner_id AND disposition_approved_at IS NOT NULL));
CREATE INDEX ix_member_moveout ON contract_tenants(move_out_status,requested_leave_date) WHERE move_out_status IS NOT NULL;
CREATE INDEX ix_repair_sla ON report_requests(sla_due_at) WHERE status IN ('OPEN','ACCEPTED','IN_PROGRESS');
CREATE FUNCTION rental.guard_amendment() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
BEGIN
 IF TG_OP='UPDATE' AND OLD.status='APPROVED' AND
  (to_jsonb(NEW)-ARRAY['document_public_id','document_resource_type','document_sha256','document_version','document_snapshot','document_status','confirmation_evidence']) IS DISTINCT FROM
  (to_jsonb(OLD)-ARRAY['document_public_id','document_resource_type','document_sha256','document_version','document_snapshot','document_status','confirmation_evidence']) THEN
  RAISE EXCEPTION 'Approved amendment business snapshot is immutable' USING ERRCODE='23514'; END IF;
 IF NEW.status='APPROVED' AND NOT EXISTS(SELECT 1 FROM contracts c WHERE c.id=NEW.contract_id AND can_manage_branch(NEW.approved_by,c.branch_id)) THEN
  RAISE EXCEPTION 'Amendment approver outside Branch scope' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER protect_amendment BEFORE INSERT OR UPDATE ON contract_amendments FOR EACH ROW EXECUTE FUNCTION guard_amendment();
CREATE TRIGGER audit_change AFTER INSERT OR UPDATE OR DELETE ON contract_amendments FOR EACH ROW EXECUTE FUNCTION audit_change();
CREATE TRIGGER preserve_history BEFORE DELETE ON contract_amendments FOR EACH ROW EXECUTE FUNCTION deny_history_delete();
COMMENT ON TABLE contract_amendments IS 'Phase 2: approved changes override original terms by effective date; never overwrite original agreement';
COMMIT;
