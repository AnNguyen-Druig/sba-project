-- READ-ONLY preflight for public-schema.sql exported on 2026-10-10 (PostgreSQL 17.11).
-- Run on the existing Supabase database or its restored copy, BEFORE UUID migration.
-- No account details, passwords, OTPs, tokens or audit detail text are selected.
-- Export every result grid. Nonzero UUID reference counts require a verified mapping;
-- they do NOT prove those references are valid or invalid.
-- If any statement fails, execute ROLLBACK before retrying.
BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ READ ONLY;

-- 1. Confirm the target and actual table counts; do not rely on stale UI metadata.
SELECT current_database() AS database_name, current_user AS database_user, version();
SELECT 'accounts' AS table_name, count(*) AS row_count FROM public.accounts
UNION ALL
SELECT 'audit_logs' AS table_name, count(*) AS row_count FROM public.audit_logs
UNION ALL
SELECT 'bank_accounts' AS table_name, count(*) AS row_count FROM public.bank_accounts
UNION ALL
SELECT 'branches' AS table_name, count(*) AS row_count FROM public.branches
UNION ALL
SELECT 'contract_services' AS table_name, count(*) AS row_count FROM public.contract_services
UNION ALL
SELECT 'cost_allocation_items' AS table_name, count(*) AS row_count FROM public.cost_allocation_items
UNION ALL
SELECT 'cost_allocation_proposals' AS table_name, count(*) AS row_count FROM public.cost_allocation_proposals
UNION ALL
SELECT 'login_attempts' AS table_name, count(*) AS row_count FROM public.login_attempts
UNION ALL
SELECT 'manager_branch_assignments' AS table_name, count(*) AS row_count FROM public.manager_branch_assignments
UNION ALL
SELECT 'notifications' AS table_name, count(*) AS row_count FROM public.notifications
UNION ALL
SELECT 'otp_verifications' AS table_name, count(*) AS row_count FROM public.otp_verifications
UNION ALL
SELECT 'refresh_tokens' AS table_name, count(*) AS row_count FROM public.refresh_tokens
UNION ALL
SELECT 'room_service_charges' AS table_name, count(*) AS row_count FROM public.room_service_charges
UNION ALL
SELECT 'room_services' AS table_name, count(*) AS row_count FROM public.room_services
UNION ALL
SELECT 'services' AS table_name, count(*) AS row_count FROM public.services
UNION ALL
SELECT 'vietqr_references' AS table_name, count(*) AS row_count FROM public.vietqr_references
ORDER BY table_name;

-- 2. Actual key-like columns, including IDs without declared foreign keys.
SELECT table_name, column_name, data_type, is_nullable, is_identity, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND (column_name = 'id' OR column_name LIKE '%\_id' ESCAPE '\'
       OR column_name IN ('created_by', 'updated_by', 'requested_by', 'processed_by',
                          'proposed_by', 'amount', 'version'))
ORDER BY table_name, ordinal_position;

-- 3. Include inbound/outbound foreign keys involving other schemas as well.
SELECT ns.nspname AS source_schema, rel.relname AS source_table,
       constraint_row.conname AS constraint_name,
       target_ns.nspname AS target_schema, target.relname AS target_table,
       constraint_row.convalidated AS validated,
       pg_get_constraintdef(constraint_row.oid) AS definition
FROM pg_constraint constraint_row
JOIN pg_class rel ON rel.oid = constraint_row.conrelid
JOIN pg_namespace ns ON ns.oid = rel.relnamespace
JOIN pg_class target ON target.oid = constraint_row.confrelid
JOIN pg_namespace target_ns ON target_ns.oid = target.relnamespace
WHERE constraint_row.contype = 'f'
  AND (ns.nspname = 'public' OR target_ns.nspname = 'public')
ORDER BY source_schema, source_table, constraint_name;

-- 4. Cross-module UUID references cannot be matched to BIGINT accounts/branches
-- by a cast. Obtain the identity mapping from the module that wrote the data.
SELECT 'services.branch_id' AS reference_column, 'Branch' AS expected_domain,
       count(branch_id) AS non_null_rows, count(DISTINCT branch_id) AS distinct_ids
FROM public.services
UNION ALL
SELECT 'notifications.branch_id' AS reference_column, 'Branch' AS expected_domain,
       count(branch_id) AS non_null_rows, count(DISTINCT branch_id) AS distinct_ids
FROM public.notifications
UNION ALL
SELECT 'services.created_by' AS reference_column, 'Account' AS expected_domain,
       count(created_by) AS non_null_rows, count(DISTINCT created_by) AS distinct_ids
FROM public.services
UNION ALL
SELECT 'contract_services.requested_by' AS reference_column, 'Account' AS expected_domain,
       count(requested_by) AS non_null_rows, count(DISTINCT requested_by) AS distinct_ids
FROM public.contract_services
UNION ALL
SELECT 'contract_services.processed_by' AS reference_column, 'Account' AS expected_domain,
       count(processed_by) AS non_null_rows, count(DISTINCT processed_by) AS distinct_ids
FROM public.contract_services
UNION ALL
SELECT 'cost_allocation_proposals.proposed_by' AS reference_column, 'Account_or_Tenant' AS expected_domain,
       count(proposed_by) AS non_null_rows, count(DISTINCT proposed_by) AS distinct_ids
FROM public.cost_allocation_proposals
UNION ALL
SELECT 'cost_allocation_proposals.processed_by' AS reference_column, 'Account' AS expected_domain,
       count(processed_by) AS non_null_rows, count(DISTINCT processed_by) AS distinct_ids
FROM public.cost_allocation_proposals
UNION ALL
SELECT 'room_service_charges.created_by' AS reference_column, 'Account' AS expected_domain,
       count(created_by) AS non_null_rows, count(DISTINCT created_by) AS distinct_ids
FROM public.room_service_charges
UNION ALL
SELECT 'notifications.recipient_id' AS reference_column, 'Account' AS expected_domain,
       count(recipient_id) AS non_null_rows, count(DISTINCT recipient_id) AS distinct_ids
FROM public.notifications
UNION ALL
SELECT 'cost_allocation_items.tenant_id' AS reference_column, 'Tenant' AS expected_domain,
       count(tenant_id) AS non_null_rows, count(DISTINCT tenant_id) AS distinct_ids
FROM public.cost_allocation_items
UNION ALL
SELECT 'contract_services.contract_id' AS reference_column, 'Contract' AS expected_domain,
       count(contract_id) AS non_null_rows, count(DISTINCT contract_id) AS distinct_ids
FROM public.contract_services
UNION ALL
SELECT 'room_services.room_id' AS reference_column, 'Room' AS expected_domain,
       count(room_id) AS non_null_rows, count(DISTINCT room_id) AS distinct_ids
FROM public.room_services
UNION ALL
SELECT 'room_service_charges.room_id' AS reference_column, 'Room' AS expected_domain,
       count(room_id) AS non_null_rows, count(DISTINCT room_id) AS distinct_ids
FROM public.room_service_charges
UNION ALL
SELECT 'cost_allocation_proposals.room_id' AS reference_column, 'Room' AS expected_domain,
       count(room_id) AS non_null_rows, count(DISTINCT room_id) AS distinct_ids
FROM public.cost_allocation_proposals
ORDER BY reference_column;

-- 5. Polymorphic references: determine the actual discriminators before conversion.
SELECT entity_type, count(*) AS row_count,
       count(entity_id) AS non_null_entity_ids,
       count(DISTINCT entity_id) AS distinct_entity_ids
FROM public.audit_logs
GROUP BY entity_type ORDER BY entity_type;

SELECT related_entity_type, purpose, count(*) AS row_count,
       count(related_entity_id) AS non_null_entity_ids,
       count(DISTINCT related_entity_id) AS distinct_entity_ids
FROM public.vietqr_references
GROUP BY related_entity_type, purpose ORDER BY related_entity_type, purpose;

SELECT reference_type, count(*) AS row_count,
       count(reference_id) AS non_null_reference_ids
FROM public.notifications
GROUP BY reference_type ORDER BY reference_type;

-- 6. Audit actors have no declared FK in the supplied dump.
-- Nonzero results may represent historical/deleted accounts; preserve, do not invent.
SELECT count(*) AS audit_rows_with_unresolved_actor
FROM public.audit_logs log_entry
LEFT JOIN public.accounts account ON account.id = log_entry.actor_id
WHERE log_entry.actor_id IS NOT NULL AND account.id IS NULL;

-- 7. Compare actual RLS/grants with the schema dump; no permission changes here.
SELECT rel.relname AS table_name, rel.relrowsecurity AS rls_enabled,
       rel.relforcerowsecurity AS rls_forced
FROM pg_class rel
JOIN pg_namespace ns ON ns.oid = rel.relnamespace
WHERE ns.nspname = 'public' AND rel.relkind IN ('r', 'p')
ORDER BY rel.relname;

SELECT tablename, policyname, roles, cmd
FROM pg_policies WHERE schemaname = 'public'
ORDER BY tablename, policyname;

SELECT table_name, grantee, privilege_type
FROM information_schema.table_privileges
WHERE table_schema = 'public' AND grantee IN ('anon', 'authenticated')
ORDER BY table_name, grantee, privilege_type;

COMMIT;
