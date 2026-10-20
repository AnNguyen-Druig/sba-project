-- Allow accounts created by the current auth flow to log in immediately.
-- Email/phone verification remains tracked separately in its *_verified_at column.
BEGIN;
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '30s';

DO $$
DECLARE
 constraint_name text;
BEGIN
 IF to_regclass('rental.users') IS NULL THEN
  RAISE EXCEPTION 'rental.users does not exist; refusing to apply auth constraint change';
 END IF;

 SELECT conname INTO constraint_name
 FROM pg_constraint
 WHERE conrelid = 'rental.users'::regclass
   AND contype = 'c'
   AND pg_get_constraintdef(oid) LIKE '%email_verified_at%'
   AND pg_get_constraintdef(oid) LIKE '%status%'
 LIMIT 1;

 IF constraint_name IS NOT NULL THEN
  EXECUTE format('ALTER TABLE rental.users DROP CONSTRAINT %I', constraint_name);
 END IF;

 IF NOT EXISTS (
  SELECT 1 FROM pg_constraint
  WHERE conrelid = 'rental.users'::regclass
    AND conname = 'users_active_requires_password'
 ) THEN
  ALTER TABLE rental.users
   ADD CONSTRAINT users_active_requires_password
   CHECK (status <> 'ACTIVE' OR (password_hash IS NOT NULL AND password_hash <> ''));
 END IF;
END $$;

COMMIT;
