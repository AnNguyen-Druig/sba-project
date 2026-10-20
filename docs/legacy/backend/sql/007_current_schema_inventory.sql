-- Read-only inventory for choosing the migration route. Execute the entire file.
BEGIN TRANSACTION READ ONLY;
SELECT current_database(), current_user, current_setting('server_version') AS server_version;
SELECT to_regclass('public.flyway_schema_history') AS flyway_history,
       to_regclass('public.users') AS separate_auth_users,
       to_regclass('public.accounts') AS shared_accounts;
SELECT table_name, column_name, data_type, is_nullable, column_default, is_identity
FROM information_schema.columns
WHERE table_schema='public'
ORDER BY table_name, ordinal_position;

-- Exact row counts, without selecting personal data/passwords.
SELECT table_name,
       (xpath('/row/n/text()', query_to_xml(
           format('SELECT count(*) AS n FROM %I.%I', table_schema, table_name),
           false, true, '')))[1]::text::bigint AS row_count
FROM information_schema.tables
WHERE table_schema='public' AND table_type='BASE TABLE'
ORDER BY table_name;
SELECT n.nspname AS source_schema, t.relname AS source_table, c.conname,
       pg_get_constraintdef(c.oid) AS definition, c.convalidated
FROM pg_constraint c JOIN pg_class t ON t.oid=c.conrelid
JOIN pg_namespace n ON n.oid=t.relnamespace
WHERE c.conrelid IN (SELECT oid FROM pg_class WHERE relnamespace='public'::regnamespace)
   OR c.confrelid IN (SELECT oid FROM pg_class WHERE relnamespace='public'::regnamespace)
ORDER BY source_schema, source_table, c.conname;
SELECT c.relname, c.relrowsecurity, c.relforcerowsecurity
FROM pg_class c WHERE c.relnamespace='public'::regnamespace AND c.relkind IN ('r','p');
SELECT schemaname, tablename, policyname, roles, cmd, qual, with_check
FROM pg_policies WHERE schemaname='public';
ROLLBACK;
-- If flyway_history above is not NULL, also run:
-- SELECT installed_rank, version, description, type, checksum, success
-- FROM public.flyway_schema_history ORDER BY installed_rank;
