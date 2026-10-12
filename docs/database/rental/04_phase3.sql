-- Optional Phase 3 schema only. No Like/Follow/Chat, no live provider credentials.
BEGIN;
SET LOCAL search_path=rental,pg_catalog;
CREATE TABLE threads (
 id uuid PRIMARY KEY DEFAULT gen_random_uuid(), author_user_id uuid NOT NULL REFERENCES users,
 title text NOT NULL, description text NOT NULL, criteria jsonb NOT NULL,
 status varchar(20) NOT NULL CHECK(status IN ('OPEN','CLOSED','HIDDEN')),
 expires_at timestamptz, closed_at timestamptz, moderation_reason text,
 moderated_by uuid REFERENCES users, moderated_at timestamptz,
 created_at timestamptz NOT NULL DEFAULT now(), updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_threads_status_time ON threads(status,created_at DESC);
CREATE FUNCTION rental.guard_thread_moderation() RETURNS trigger LANGUAGE plpgsql SET search_path=rental,pg_catalog AS $$
DECLARE actor uuid;
BEGIN
 IF (TG_OP='INSERT' AND (NEW.status='HIDDEN' OR num_nonnulls(NEW.moderated_by,NEW.moderated_at,NEW.moderation_reason)>0)) OR
  (TG_OP='UPDATE' AND NEW.status IS DISTINCT FROM OLD.status AND (NEW.status='HIDDEN' OR OLD.status='HIDDEN')) OR
  (TG_OP='UPDATE' AND (NEW.moderated_by,NEW.moderated_at,NEW.moderation_reason) IS DISTINCT FROM (OLD.moderated_by,OLD.moderated_at,OLD.moderation_reason)) THEN
  actor:=nullif(current_setting('rental.actor_id',true),'')::uuid;
  IF NEW.moderated_by IS DISTINCT FROM actor OR NEW.moderated_at IS NULL OR NEW.moderation_reason IS NULL OR NOT EXISTS(
    SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.id=actor AND u.status='ACTIVE' AND r.code='ADMIN' AND (u.locked_until IS NULL OR u.locked_until<=now())) THEN
   RAISE EXCEPTION 'Thread moderation requires ADMIN actor and reason' USING ERRCODE='23514'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER protect_moderation BEFORE INSERT OR UPDATE ON threads FOR EACH ROW EXECUTE FUNCTION guard_thread_moderation();
CREATE TRIGGER audit_change AFTER INSERT OR UPDATE OR DELETE ON threads FOR EACH ROW EXECUTE FUNCTION audit_change();
ALTER TABLE notifications
 ADD COLUMN channel varchar(10) NOT NULL DEFAULT 'IN_APP' CHECK(channel IN ('IN_APP','EMAIL','SMS')),
 ADD COLUMN delivery_status varchar(12) NOT NULL DEFAULT 'PENDING' CHECK(delivery_status IN ('PENDING','SENDING','SENT','FAILED','CANCELLED')),
 ADD COLUMN attempt_count integer NOT NULL DEFAULT 0 CHECK(attempt_count>=0),
 ADD COLUMN next_attempt_at timestamptz, ADD COLUMN sent_at timestamptz,
 ADD COLUMN provider_message_id text, ADD COLUMN last_error text;
ALTER TABLE notifications DROP CONSTRAINT notifications_recipient_user_id_event_key_type_key;
ALTER TABLE notifications ADD CONSTRAINT uq_notification_delivery UNIQUE(recipient_user_id,event_key,type,channel);
CREATE INDEX ix_notification_retry ON notifications(next_attempt_at) WHERE channel<>'IN_APP' AND delivery_status IN ('PENDING','FAILED');
-- One notification row per recipient/channel; templates stay in versioned source/config.
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['contracts','contract_amendments'] LOOP
 EXECUTE format('ALTER TABLE rental.%I
  ADD COLUMN document_public_id text,
  ADD COLUMN document_resource_type varchar(20),
  ADD COLUMN document_sha256 varchar(64) CHECK(document_sha256 ~ ''^[0-9a-f]{64}$''),
  ADD COLUMN document_version integer CHECK(document_version>0),
  ADD COLUMN document_snapshot jsonb CHECK(jsonb_typeof(document_snapshot)=''object''),
  ADD COLUMN document_status varchar(24) CHECK(document_status IN (''DRAFT'',''AWAITING_CONFIRMATION'',''CONFIRMED'')),
  ADD COLUMN confirmation_evidence jsonb NOT NULL DEFAULT ''[]'' CHECK(jsonb_typeof(confirmation_evidence)=''array''),
  ADD CONSTRAINT ck_document_complete CHECK(document_status IS NULL OR document_status=''DRAFT'' OR
   (document_public_id IS NOT NULL AND document_resource_type IS NOT NULL AND document_sha256 IS NOT NULL AND document_version IS NOT NULL AND document_snapshot IS NOT NULL))',t);
 END LOOP;
END $$;
CREATE FUNCTION rental.guard_document_snapshot() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='UPDATE' AND OLD.document_status IN ('AWAITING_CONFIRMATION','CONFIRMED') THEN
  IF (NEW.document_public_id,NEW.document_resource_type,NEW.document_sha256,NEW.document_version,NEW.document_snapshot) IS DISTINCT FROM
     (OLD.document_public_id,OLD.document_resource_type,OLD.document_sha256,OLD.document_version,OLD.document_snapshot)
     OR NEW.document_status IS NULL OR NEW.document_status='DRAFT' OR
     (OLD.document_status='CONFIRMED' AND (NEW.document_status<>'CONFIRMED' OR NEW.confirmation_evidence IS DISTINCT FROM OLD.confirmation_evidence)) THEN
   RAISE EXCEPTION 'Released/confirmed document is immutable; use a new amendment' USING ERRCODE='23514'; END IF;
 END IF;
 IF NEW.document_status='CONFIRMED' AND jsonb_array_length(NEW.confirmation_evidence)=0 THEN
  RAISE EXCEPTION 'Confirmation needs evidence; backend validates signers/challenges' USING ERRCODE='23514'; END IF;
 IF EXISTS(SELECT 1 FROM jsonb_array_elements(NEW.confirmation_evidence) e WHERE jsonb_typeof(e)<>'object' OR
   NOT (e ?& ARRAY['signer_user_id','confirmed_at','content_sha256']) OR
   e->>'content_sha256' IS DISTINCT FROM NEW.document_sha256 OR
   EXISTS(SELECT 1 FROM jsonb_object_keys(e) k WHERE k NOT IN ('signer_user_id','confirmed_at','content_sha256','capacity','result'))) THEN
  RAISE EXCEPTION 'Invalid confirmation evidence shape or content hash' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER protect_document BEFORE INSERT OR UPDATE ON contracts FOR EACH ROW EXECUTE FUNCTION guard_document_snapshot();
CREATE TRIGGER protect_document BEFORE INSERT OR UPDATE ON contract_amendments FOR EACH ROW EXECUTE FUNCTION guard_document_snapshot();
COMMENT ON COLUMN contracts.confirmation_evidence IS 'Extension only, not implemented OTP workflow; result snapshot, no OTP/challenge hash';
COMMIT;
