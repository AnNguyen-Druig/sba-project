-- Run the WHOLE file. Takes brief locks but persists no changes. Stop writers first.
BEGIN;
-- Exact legacy schema guard (PostgreSQL 17), shared by baseline and V2.
-- Locks prevent inserts between the empty-data check and ALTER TABLE.
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '120s';
SET LOCAL search_path = pg_catalog;
DO $guard$
DECLARE
    actual_signature text;
    table_name text;
    has_data boolean;
BEGIN
    IF current_setting('server_version_num')::integer / 10000 <> 17 THEN
        RAISE EXCEPTION 'SCHEMA_GUARD: this migration was verified on PostgreSQL 17; review other major versions first';
    END IF;
    IF (SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname='public' AND c.relkind IN ('r','p') AND c.relname<>'flyway_schema_history') <> 16
       OR EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname='public' AND c.relkind='p') THEN
        RAISE EXCEPTION 'SCHEMA_GUARD: expected exactly the 16 ordinary legacy tables';
    END IF;
    LOCK TABLE public.accounts, public.audit_logs, public.bank_accounts, public.branches,
        public.contract_services, public.cost_allocation_items, public.cost_allocation_proposals,
        public.login_attempts, public.manager_branch_assignments, public.notifications,
        public.otp_verifications, public.refresh_tokens, public.room_service_charges,
        public.room_services, public.services, public.vietqr_references IN ACCESS EXCLUSIVE MODE;
    WITH parts AS (
    SELECT 'column' AS kind, c.relname::text AS tbl, a.attname::text AS name,
           concat_ws('|', a.attnum, format_type(a.atttypid, a.atttypmod),
               a.attnotnull, a.attidentity, coalesce(pg_get_expr(d.adbin, d.adrelid), '')) AS definition
    FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
    JOIN pg_attribute a ON a.attrelid=c.oid AND a.attnum>0 AND NOT a.attisdropped
    LEFT JOIN pg_attrdef d ON d.adrelid=c.oid AND d.adnum=a.attnum
    WHERE n.nspname='public' AND c.relkind='r' AND c.relname <> 'flyway_schema_history'
    UNION ALL
    SELECT 'constraint', c.relname, k.conname, pg_get_constraintdef(k.oid)
    FROM pg_constraint k JOIN pg_class c ON c.oid=k.conrelid
    JOIN pg_namespace n ON n.oid=c.relnamespace
    WHERE n.nspname='public' AND c.relname <> 'flyway_schema_history'
    UNION ALL
    SELECT 'index', tablename, indexname, indexdef FROM pg_indexes
    WHERE schemaname='public' AND tablename <> 'flyway_schema_history'
)
SELECT md5(string_agg(concat_ws('|',kind,tbl,name,definition), E'\n'
    ORDER BY kind COLLATE "C",tbl COLLATE "C",name COLLATE "C")) INTO actual_signature FROM parts;
    IF actual_signature IS DISTINCT FROM 'ad654294880c66201d0794a9847d0fa4' THEN
        RAISE EXCEPTION 'SCHEMA_GUARD: legacy schema differs from reviewed V1 (signature %). Do not baseline or bypass this check.', actual_signature;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_constraint k
        JOIN pg_class src ON src.oid=k.conrelid JOIN pg_namespace sn ON sn.oid=src.relnamespace
        JOIN pg_class dst ON dst.oid=k.confrelid JOIN pg_namespace dn ON dn.oid=dst.relnamespace
        WHERE k.contype='f' AND ((sn.nspname='public' AND dn.nspname<>'public')
            OR (sn.nspname<>'public' AND dn.nspname='public')
            OR (sn.nspname='public' AND NOT k.convalidated))) THEN
        RAISE EXCEPTION 'SCHEMA_GUARD: unexpected cross-schema or unvalidated foreign key';
    END IF;
    IF EXISTS (SELECT 1 FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid
        JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname='public' AND NOT t.tgisinternal) THEN
        RAISE EXCEPTION 'SCHEMA_GUARD: custom triggers need review before changing IDs';
    END IF;
    FOR table_name IN SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname='public' AND c.relkind IN ('r','p') AND c.relname<>'flyway_schema_history'
        ORDER BY c.relname
    LOOP
        EXECUTE format('SELECT EXISTS (SELECT 1 FROM public.%I)', table_name) INTO has_data;
        IF has_data THEN
            RAISE EXCEPTION 'DATA_GUARD: public.% contains data. Migration stopped; preserve it and design an ID mapping.', table_name;
        END IF;
    END LOOP;
END
$guard$;
ROLLBACK;
