<#
.SYNOPSIS
Tests UUID migrations only on disposable PostgreSQL 17 databases in the local Docker container.
.DESCRIPTION
Creates new sba_uuid_test_* databases; never drops existing databases or connects to Supabase.
Requires the existing sba-invoice-postgres container bound to localhost:5433 and Maven/JDK.
Databases and logs are retained for inspection. LocalPassword is the local test database password.
#>
param([Parameter(Mandatory = $true)][string]$LocalPassword)
$ErrorActionPreference = 'Stop'
$backend = Split-Path $PSScriptRoot -Parent
if (Test-Path (Join-Path $backend 'src/main/java/com/sba/project/modules')) {
    throw 'Legacy public-schema migration test: run only from the old backend checkout. The rental foundation has no legacy Flyway profile or migration tests.'
}
$runId = Get-Date -Format 'yyyyMMddHHmmss'
$testPrefix = "sba_uuid_test_${runId}_"
$oldEnv = @{}
foreach ($key in @('DB_URL','DB_USERNAME','DB_PASSWORD','MIGRATION_TEST_DB_URL',
    'MIGRATION_TEST_DB_USERNAME','MIGRATION_TEST_DB_PASSWORD','MIGRATION_TEST_DB_NAME')) {
    $oldEnv[$key] = [Environment]::GetEnvironmentVariable($key, 'Process')
}

# Executes SQL only against this run's newly created local test databases.
function Invoke-TestSql([string]$Database, [string]$Sql) {
    if (-not $Database.StartsWith($testPrefix)) { throw 'Refusing a non-test database' }
    $output = $Sql | docker exec -i sba-invoice-postgres psql -X -At -v ON_ERROR_STOP=1 -U invoice_dev -d $Database
    if ($LASTEXITCODE -ne 0) { throw "SQL failed on $Database" }
    return ($output -join "`n")
}

# Creates a unique database; an existing name is an error, never a reset request.
function New-TestDatabase([string]$Suffix) {
    $database = $testPrefix + $Suffix
    docker exec sba-invoice-postgres createdb -U invoice_dev $database
    if ($LASTEXITCODE -ne 0) { throw "Cannot create $database" }
    return $database
}

# Runs Maven and checks both the exit status and the expected failure reason.
function Invoke-TestMaven([string]$Database, [string]$Label, [string[]]$Goals, [string]$ExpectedError = '') {
    if (-not $Database.StartsWith($testPrefix)) { throw 'Refusing a non-test database' }
    $env:DB_URL = "jdbc:postgresql://localhost:5433/$Database"
    $env:DB_USERNAME = 'invoice_dev'
    $env:DB_PASSWORD = $LocalPassword
    $log = Join-Path $backend "target/uuid-$runId-$Label.log"
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    & mvn -B @Goals *> $log
    $result = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    $output = [IO.File]::ReadAllText($log)
    if ($ExpectedError) {
        if ($result -eq 0 -or -not $output.Contains($ExpectedError)) {
            throw "Expected rejection '$ExpectedError'; inspect $log"
        }
    } elseif ($result -ne 0) {
        throw "Maven failed; inspect $log"
    }
    Write-Host "PASS $Label"
}

Push-Location $backend
try {
    [IO.Directory]::CreateDirectory((Join-Path $backend 'target')) | Out-Null
    $v1 = [IO.File]::ReadAllText((Join-Path $backend 'src/main/resources/db/migration/V1__existing_application_schema.sql'))
    $verify = [IO.File]::ReadAllText((Join-Path $backend '../docs/legacy/backend/sql/008_verify_current_schema.sql'))
    $seed = [IO.File]::ReadAllText((Join-Path $backend '../docs/legacy/backend/sql/006_uuid_local_demo_data.sql'))

    $fresh = New-TestDatabase 'fresh'
    Invoke-TestMaven $fresh 'empty-bootstrap' @('-Pdatabase-migration','flyway:migrate','flyway:validate')
    Invoke-TestSql $fresh $verify | Write-Host
    Invoke-TestMaven $fresh 'repeat-migrate' @('-Pdatabase-migration','flyway:migrate','flyway:validate')
    if ((Invoke-TestSql $fresh 'SELECT count(*) FROM public.flyway_schema_history') -ne '5') {
        throw 'Repeated migrate changed migration history'
    }

    $legacy = New-TestDatabase 'legacy'
    Invoke-TestSql $legacy $v1 | Out-Null
    Invoke-TestMaven $legacy 'legacy-baseline-upgrade' @('-Pdatabase-migration','flyway:baseline','flyway:migrate','flyway:validate')
    Invoke-TestSql $legacy $verify | Write-Host
    $structure = @'
SELECT md5(string_agg(table_name||'.'||column_name||':'||data_type||':'||is_nullable,
    ',' ORDER BY table_name,ordinal_position)) FROM information_schema.columns
WHERE table_schema='public' AND table_name<>'flyway_schema_history';
'@
    if ((Invoke-TestSql $fresh $structure) -ne (Invoke-TestSql $legacy $structure)) {
        throw 'Fresh and upgraded schemas differ'
    }

    # V4/V5 must preserve all existing V3 rows, not merely succeed on empty databases.
    $upgrade = New-TestDatabase 'v3_data'
    Invoke-TestMaven $upgrade 'bootstrap-through-v3' @('-Pdatabase-migration','-Dflyway.target=3','flyway:migrate')
    Invoke-TestSql $upgrade $seed | Out-Null
    $existingData = @'
SELECT md5(string_agg(row_json, ',' ORDER BY row_json)) FROM (
 SELECT row_to_json(t)::text AS row_json FROM public.accounts t
 UNION ALL SELECT (to_jsonb(t) - 'manager_id')::text FROM public.branches t
 UNION ALL SELECT row_to_json(t)::text FROM public.contracts t
 UNION ALL SELECT row_to_json(t)::text FROM public.contract_tenants t
 UNION ALL SELECT row_to_json(t)::text FROM public.manager_branch_assignments t
) snapshot;
'@
    $beforeUpgrade = Invoke-TestSql $upgrade $existingData
    Invoke-TestMaven $upgrade 'upgrade-v3-with-data' @('-Pdatabase-migration','flyway:migrate','flyway:validate')
    if ((Invoke-TestSql $upgrade $existingData) -ne $beforeUpgrade) { throw 'V4/V5 changed existing data' }
    Invoke-TestSql $upgrade $verify | Write-Host

    $duplicateEmail = New-TestDatabase 'duplicate_email'
    Invoke-TestMaven $duplicateEmail 'bootstrap-for-email-guard' @('-Pdatabase-migration','-Dflyway.target=3','flyway:migrate')
    Invoke-TestSql $duplicateEmail "INSERT INTO public.accounts (email,password_hash,role,status) VALUES ('Owner@example.com','keep-1','OWNER','ACTIVE'),('owner@example.com','keep-2','OWNER','ACTIVE')" | Out-Null
    Invoke-TestMaven $duplicateEmail 'reject-ambiguous-email' @('-Pdatabase-migration','flyway:migrate') 'AUTH_DATA_GUARD'
    if ((Invoke-TestSql $duplicateEmail 'SELECT count(*) FROM public.accounts') -ne '2') { throw 'Duplicate-email accounts were changed' }
    if ((Invoke-TestSql $duplicateEmail "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='rooms'") -ne '0') { throw 'V4 was not rolled back after V5 failure' }

    $nonempty = New-TestDatabase 'nonempty'
    Invoke-TestSql $nonempty $v1 | Out-Null
    Invoke-TestSql $nonempty "INSERT INTO public.accounts (password_hash,role,status) VALUES ('test-only','OWNER','ACTIVE')" | Out-Null
    Invoke-TestMaven $nonempty 'reject-data-at-baseline' @('-Pdatabase-migration','flyway:baseline') 'DATA_GUARD'
    if ((Invoke-TestSql $nonempty 'SELECT count(*) FROM public.accounts') -ne '1') { throw 'Data was lost' }
    if ((Invoke-TestSql $nonempty "SELECT data_type FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts' AND column_name='id'") -ne 'bigint') {
        throw 'Rejected baseline changed ID type'
    }

    $changed = New-TestDatabase 'changed'
    Invoke-TestSql $changed $v1 | Out-Null
    Invoke-TestSql $changed 'ALTER TABLE public.branches ADD COLUMN unexpected_field text' | Out-Null
    Invoke-TestMaven $changed 'reject-schema-drift' @('-Pdatabase-migration','flyway:baseline') 'SCHEMA_GUARD'

    $lateData = New-TestDatabase 'late_data'
    Invoke-TestSql $lateData $v1 | Out-Null
    Invoke-TestMaven $lateData 'baseline-before-late-data' @('-Pdatabase-migration','flyway:baseline')
    Invoke-TestSql $lateData "INSERT INTO public.accounts (password_hash,role,status) VALUES ('test-only','OWNER','ACTIVE')" | Out-Null
    Invoke-TestMaven $lateData 'reject-data-after-baseline' @('-Pdatabase-migration','flyway:migrate') 'DATA_GUARD'
    if ((Invoke-TestSql $lateData 'SELECT count(*) FROM public.accounts') -ne '1') { throw 'Late data was lost' }
    if ((Invoke-TestSql $lateData 'SELECT count(*) FROM public.flyway_schema_history') -ne '1') { throw 'Failed V2 was recorded as applied' }

    # Inject a failure only into a target/ copy to prove V2 and V3 roll back together.
    $rollbackDb = New-TestDatabase 'rollback'
    Invoke-TestSql $rollbackDb $v1 | Out-Null
    Invoke-TestMaven $rollbackDb 'baseline-before-rollback-test' @('-Pdatabase-migration','flyway:baseline')
    $failureLocation = Join-Path $backend "target/uuid-failure-$runId"
    [IO.Directory]::CreateDirectory($failureLocation) | Out-Null
    Copy-Item 'src/main/resources/db/migration/*.sql' -Destination $failureLocation
    $failureFile = Join-Path $failureLocation 'V3__add_invoice_draft_tables.sql'
    [IO.File]::AppendAllText($failureFile, "`nDO `$test`$ BEGIN RAISE EXCEPTION 'EXPECTED_V3_FAILURE'; END `$test`$;`n")
    $locationArgument = '-Dflyway.locations=filesystem:' + $failureLocation.Replace('\','/')
    Invoke-TestMaven $rollbackDb 'rollback-after-v2-ddl' @('-Pdatabase-migration',$locationArgument,'flyway:migrate') 'EXPECTED_V3_FAILURE'
    if ((Invoke-TestSql $rollbackDb "SELECT data_type FROM information_schema.columns WHERE table_schema='public' AND table_name='accounts' AND column_name='id'") -ne 'bigint') {
        throw 'V2 type changes were not rolled back after V3 failure'
    }
    if ((Invoke-TestSql $rollbackDb "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='invoices'") -ne '0') {
        throw 'V3 tables were not rolled back'
    }
    if ((Invoke-TestSql $rollbackDb 'SELECT count(*) FROM public.flyway_schema_history') -ne '1') { throw 'Rolled-back migrations polluted history' }

    Invoke-TestSql $fresh $seed | Out-Null
    $env:MIGRATION_TEST_DB_URL = "jdbc:postgresql://localhost:5433/$fresh"
    $env:MIGRATION_TEST_DB_USERNAME = 'invoice_dev'
    $env:MIGRATION_TEST_DB_PASSWORD = $LocalPassword
    Invoke-TestMaven $fresh 'hibernate-and-invoice-postgres' @('test','-Dtest=SharedSchemaPostgresTests')
    $startup = New-TestDatabase 'startup'
    $env:MIGRATION_TEST_DB_NAME = $startup
    $env:MIGRATION_TEST_DB_URL = "jdbc:postgresql://localhost:5433/$startup"
    Invoke-TestMaven $startup 'local-startup-auto-migration' @('test','-Dtest=LocalStartupMigrationTests')
    Invoke-TestSql $startup $verify | Write-Host
    Invoke-TestMaven $startup 'local-startup-repeat' @('test','-Dtest=LocalStartupMigrationTests')
    Write-Host "ALL CHECKS PASSED. Retained local databases: $testPrefix*"
} finally {
    foreach ($key in $oldEnv.Keys) { [Environment]::SetEnvironmentVariable($key, $oldEnv[$key], 'Process') }
    Pop-Location
}
