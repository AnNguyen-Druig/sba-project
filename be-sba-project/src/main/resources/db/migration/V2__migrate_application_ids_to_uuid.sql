-- EMPTY legacy tables only. No TRUNCATE, DELETE, CASCADE or generated replacement IDs.
-- Flyway runs this in a transaction; failure rolls back all schema changes.
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

ALTER TABLE public.login_attempts DROP CONSTRAINT fk1sof2im7gh0k5hg712fauwb0e;
ALTER TABLE public.room_service_charges DROP CONSTRAINT fk2llc7t7y43nckx9m467ykhmdw;
ALTER TABLE public.branches DROP CONSTRAINT fk440vr7frsvio8wvsghxkj2v6j;
ALTER TABLE public.bank_accounts DROP CONSTRAINT fk4dfphvjoinwhdikx70y7w2aaq;
ALTER TABLE public.bank_accounts DROP CONSTRAINT fk4tcpdpir75yg8obbd9b9j25rb;
ALTER TABLE public.refresh_tokens DROP CONSTRAINT fk6fm1gdbsrg5h8r5e3voiu6bo9;
ALTER TABLE public.cost_allocation_items DROP CONSTRAINT fkdst5vwoqiyp11ohvcdddrhp1v;
ALTER TABLE public.vietqr_references DROP CONSTRAINT fkf45shdy5ca7ucqd403nmoqiue;
ALTER TABLE public.otp_verifications DROP CONSTRAINT fkgi7hwfky7tttkl1r1hx51w7fl;
ALTER TABLE public.manager_branch_assignments DROP CONSTRAINT fkhna1qjitjt8wepubbynpf969u;
ALTER TABLE public.cost_allocation_proposals DROP CONSTRAINT fkkdi3jm2813tb8078vs99y6lfs;
ALTER TABLE public.room_services DROP CONSTRAINT fkrvoqlh9yqrup1v41ejfevv6po;
ALTER TABLE public.contract_services DROP CONSTRAINT fks1fj5xjyogrijgaoi3aymwrtb;
ALTER TABLE public.manager_branch_assignments DROP CONSTRAINT fkteps0mylo1nf6xrjj5v7mrq5g;
ALTER TABLE public.accounts ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.accounts ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.accounts ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.audit_logs ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.audit_logs ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.audit_logs ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.audit_logs ALTER COLUMN actor_id TYPE uuid USING (actor_id::text::uuid);
ALTER TABLE public.audit_logs ALTER COLUMN entity_id TYPE uuid USING (entity_id::text::uuid);
ALTER TABLE public.bank_accounts ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.bank_accounts ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.bank_accounts ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.bank_accounts ALTER COLUMN branch_id TYPE uuid USING (branch_id::text::uuid);
ALTER TABLE public.bank_accounts ALTER COLUMN owner_id TYPE uuid USING (owner_id::text::uuid);
ALTER TABLE public.branches ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.branches ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.branches ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.branches ALTER COLUMN owner_id TYPE uuid USING (owner_id::text::uuid);
ALTER TABLE public.login_attempts ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.login_attempts ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.login_attempts ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.login_attempts ALTER COLUMN account_id TYPE uuid USING (account_id::text::uuid);
ALTER TABLE public.manager_branch_assignments ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.manager_branch_assignments ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.manager_branch_assignments ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.manager_branch_assignments ALTER COLUMN branch_id TYPE uuid USING (branch_id::text::uuid);
ALTER TABLE public.manager_branch_assignments ALTER COLUMN manager_id TYPE uuid USING (manager_id::text::uuid);
ALTER TABLE public.otp_verifications ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.otp_verifications ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.otp_verifications ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.otp_verifications ALTER COLUMN account_id TYPE uuid USING (account_id::text::uuid);
ALTER TABLE public.refresh_tokens ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.refresh_tokens ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.refresh_tokens ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.refresh_tokens ALTER COLUMN account_id TYPE uuid USING (account_id::text::uuid);
ALTER TABLE public.vietqr_references ALTER COLUMN id DROP IDENTITY;
ALTER TABLE public.vietqr_references ALTER COLUMN id TYPE uuid USING (id::text::uuid);
ALTER TABLE public.vietqr_references ALTER COLUMN id SET DEFAULT gen_random_uuid();
ALTER TABLE public.vietqr_references ALTER COLUMN related_entity_id TYPE uuid USING (related_entity_id::text::uuid);
ALTER TABLE public.vietqr_references ALTER COLUMN bank_account_id TYPE uuid USING (bank_account_id::text::uuid);

ALTER TABLE ONLY public.login_attempts
    ADD CONSTRAINT fk1sof2im7gh0k5hg712fauwb0e FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.room_service_charges
    ADD CONSTRAINT fk2llc7t7y43nckx9m467ykhmdw FOREIGN KEY (room_service_id) REFERENCES public.room_services(id);
ALTER TABLE ONLY public.branches
    ADD CONSTRAINT fk440vr7frsvio8wvsghxkj2v6j FOREIGN KEY (owner_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.bank_accounts
    ADD CONSTRAINT fk4dfphvjoinwhdikx70y7w2aaq FOREIGN KEY (branch_id) REFERENCES public.branches(id);
ALTER TABLE ONLY public.bank_accounts
    ADD CONSTRAINT fk4tcpdpir75yg8obbd9b9j25rb FOREIGN KEY (owner_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk6fm1gdbsrg5h8r5e3voiu6bo9 FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.cost_allocation_items
    ADD CONSTRAINT fkdst5vwoqiyp11ohvcdddrhp1v FOREIGN KEY (proposal_id) REFERENCES public.cost_allocation_proposals(id);
ALTER TABLE ONLY public.vietqr_references
    ADD CONSTRAINT fkf45shdy5ca7ucqd403nmoqiue FOREIGN KEY (bank_account_id) REFERENCES public.bank_accounts(id);
ALTER TABLE ONLY public.otp_verifications
    ADD CONSTRAINT fkgi7hwfky7tttkl1r1hx51w7fl FOREIGN KEY (account_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.manager_branch_assignments
    ADD CONSTRAINT fkhna1qjitjt8wepubbynpf969u FOREIGN KEY (manager_id) REFERENCES public.accounts(id);
ALTER TABLE ONLY public.cost_allocation_proposals
    ADD CONSTRAINT fkkdi3jm2813tb8078vs99y6lfs FOREIGN KEY (room_service_charge_id) REFERENCES public.room_service_charges(id);
ALTER TABLE ONLY public.room_services
    ADD CONSTRAINT fkrvoqlh9yqrup1v41ejfevv6po FOREIGN KEY (service_id) REFERENCES public.services(id);
ALTER TABLE ONLY public.contract_services
    ADD CONSTRAINT fks1fj5xjyogrijgaoi3aymwrtb FOREIGN KEY (room_service_id) REFERENCES public.room_services(id);
ALTER TABLE ONLY public.manager_branch_assignments
    ADD CONSTRAINT fkteps0mylo1nf6xrjj5v7mrq5g FOREIGN KEY (branch_id) REFERENCES public.branches(id);
