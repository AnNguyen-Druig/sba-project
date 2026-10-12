-- READ ONLY. Expected: rental exists, zero rental tables; public may contain old app tables.
SELECT current_database(),current_user,version();
SELECT nspname AS schema_name,pg_get_userbyid(nspowner) AS schema_owner
FROM pg_namespace WHERE nspname IN ('rental','public');
SELECT table_schema,count(*) AS tables FROM information_schema.tables
WHERE table_schema IN ('rental','public') AND table_type='BASE TABLE' GROUP BY table_schema;
SELECT tablename FROM pg_tables WHERE schemaname='rental' ORDER BY tablename;
SELECT to_regprocedure('gen_random_uuid()') IS NOT NULL AS has_uuid_generator;
