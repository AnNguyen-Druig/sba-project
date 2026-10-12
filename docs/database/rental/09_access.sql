-- Run after all CREATE scripts, before application access. Re-runnable.
-- Backend JDBC only. No auth.uid(), Supabase Auth, Data API grants or RLS policies.
BEGIN;
REVOKE ALL ON SCHEMA rental FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA rental FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA rental FROM PUBLIC;
ALTER DEFAULT PRIVILEGES IN SCHEMA rental REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC;
DO $$ DECLARE role_name text; BEGIN
 FOREACH role_name IN ARRAY ARRAY['anon','authenticated','service_role'] LOOP
  IF EXISTS(SELECT 1 FROM pg_roles WHERE rolname=role_name) THEN
   EXECUTE format('REVOKE ALL ON SCHEMA rental FROM %I',role_name);
   EXECUTE format('REVOKE ALL ON ALL TABLES IN SCHEMA rental FROM %I',role_name);
   EXECUTE format('REVOKE ALL ON ALL FUNCTIONS IN SCHEMA rental FROM %I',role_name);
   EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA rental REVOKE ALL ON TABLES FROM %I',role_name);
   EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA rental REVOKE ALL ON FUNCTIONS FROM %I',role_name);
  END IF;
 END LOOP;
END $$;
COMMIT;
