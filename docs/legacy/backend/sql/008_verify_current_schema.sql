-- Read-only verification after Flyway migration. Run the entire file.
BEGIN TRANSACTION READ ONLY;
SELECT current_database(), current_user, version();
SELECT installed_rank, version, description, type, success
FROM public.flyway_schema_history ORDER BY installed_rank;

DO $verify$
BEGIN
    IF (SELECT count(*) FROM information_schema.tables
        WHERE table_schema='public' AND table_type='BASE TABLE'
          AND table_name<>'flyway_schema_history') <> 31 THEN
        RAISE EXCEPTION 'Expected 31 application tables immediately after V5';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public'
        AND (column_name='id' OR column_name LIKE '%\_id' ESCAPE '\')
        AND data_type<>'uuid' AND table_name NOT IN ('flyway_schema_history','invalidate_tokens')) THEN
        RAISE EXCEPTION 'An application ID column is not UUID';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public'
        AND is_identity='YES' AND table_name<>'flyway_schema_history') THEN
        RAISE EXCEPTION 'Legacy identity column remains';
    END IF;
    IF (SELECT data_type FROM information_schema.columns WHERE table_schema='public'
        AND table_name='vietqr_references' AND column_name='amount') IS DISTINCT FROM 'bigint' THEN
        RAISE EXCEPTION 'Money amount must remain BIGINT';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_constraint k JOIN pg_namespace n ON n.oid=k.connamespace
        WHERE n.nspname='public' AND k.contype='f' AND NOT k.convalidated) THEN
        RAISE EXCEPTION 'Unvalidated foreign key remains';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM public.flyway_schema_history WHERE version='5' AND success) THEN
        RAISE EXCEPTION 'V5 was not applied successfully';
    END IF;
    IF to_regclass('public.users') IS NOT NULL THEN
        RAISE EXCEPTION 'Unexpected separate users table';
    END IF;
    IF to_regclass('public.uq_accounts_email_ignore_case') IS NULL THEN
        RAISE EXCEPTION 'Missing case-insensitive login index';
    END IF;
END
$verify$;

SELECT table_name, column_name, data_type, column_default
FROM information_schema.columns
WHERE table_schema='public' AND column_name='id'
ORDER BY table_name;
SELECT 'UUID_SCHEMA_VERIFIED_V5' AS result;
COMMIT;
