> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../backend/README.md).

# Đồng bộ database của nhóm — PostgreSQL 17 / Supabase / UUID

Bộ code hiện tại thống nhất Auth vào **accounts/UUID**, JWT dùng account UUID;
email vẫn là đầu vào đăng nhập. Migration **V1–V5** là nguồn schema dùng chung.
Không đổi mọi cột BIGINT thành UUID: chỉ ID/tham chiếu nghiệp vụ. Các cột tiền,
bộ đếm giữ nguyên; `invalidate_tokens.id` là JWT jti dạng chuỗi, không phải FK tài khoản.

**Giới hạn bảo toàn dữ liệu:** V2 chỉ chuyển được đúng 16 bảng legacy còn trống.
Snapshot cũ trống không đảm bảo hôm nay vẫn trống. Callback và V2 kiểm tra lại;
có dù một bản ghi thì dừng với DATA_GUARD, không xóa dữ liệu. Khi đó cần bộ ánh xạ
ID dựa trên dữ liệu thật; không bỏ guard, không TRUNCATE và không cast BIGINT có
dữ liệu trực tiếp sang UUID. V4/V5 hỗ trợ nâng cấp V3 có dữ liệu, giữ ID và nội dung cũ.

Ứng dụng thường dùng `ddl-auto: validate`, Flyway startup tắt. Chỉ profile
`local-dev` tự migrate PostgreSQL localhost. Supabase được một người phụ trách
migrate bằng Flyway Maven; không dùng `update`, `create`, `create-drop` trên database chung.

## 1. Chọn đúng luồng

| Database của bạn | Thực hiện |
| --- | --- |
| Supabase đang có 16 bảng BIGINT/UUID theo snapshot ngày 10/10/2026 | Người phụ trách chạy mục 3, một lần cho cả nhóm |
| Local hoàn toàn mới | Chạy mục 4: migrate V1 → V2 → V3 → V4 → V5, không baseline |
| Supabase đã lên version 3 | Người phụ trách migrate V4/V5, không baseline lần nữa |
| Supabase đã lên version 5 | Thành viên làm mục 5, không chạy lại baseline |
| Local Invoice cũ đã chạy 001/002 hoặc schema khác | Giữ nguyên database đó; tạo local mới theo mục 4 |

**Không chạy riêng từng V1–V5 trong Query Console.** Flyway phải quản lý thứ tự,
checksum và transaction. Các file kiểm tra 004/007/008 có thể chạy trong Console;
005 chỉ dành cho snapshot V3 cũ, không dùng để kiểm tra V5.

Giới hạn đã chốt: V2 chỉ chuyển **schema cũ có 16 bảng ứng dụng còn rỗng**.
Preflight Supabase lúc 10:50–10:51 ngày 10/10/2026 cho thấy cả 16 bảng đều rỗng.
Nếu hiện tại đã có dữ liệu, migration sẽ dừng và giữ nguyên dữ liệu. Không xóa
bản ghi, sửa guard hoặc chạy `clean` để ép migration thành công; cần ánh xạ ID
bảo toàn dữ liệu trong trường hợp đó.

## 2. Những gì được thay đổi

| File trong `src/main/resources/db/migration` | Công dụng |
| --- | --- |
| `V1__existing_application_schema.sql` | Tạo 16 bảng cũ trên local trống; giữ PK/FK/unique/check/index/identity |
| `beforeBaseline__check_legacy_schema.sql` | Tự kiểm tra schema, bảng rỗng và dependency trước khi nhận baseline |
| `V2__migrate_application_ids_to_uuid.sql` | Kiểm tra lại dưới khóa bảng; chuyển 9 PK BIGINT, FK và tham chiếu ID liên quan sang UUID |
| `V3__add_invoice_draft_tables.sql` | Thêm 8 bảng Contract/Invoice, dùng lại Branch của nhóm |
| `V4__add_room_and_appointment_tables.sql` | Thêm 6 bảng Student 2 và branches.manager_id |
| `V5__integrate_auth_with_shared_accounts.sql` | Auth dùng accounts hiện có; index email không phân biệt hoa thường và bảng invalidate_tokens |

V2 giữ nguyên các cột tiền, số lượng, bộ đếm. Ví dụ `vietqr_references.amount`
vẫn là BIGINT. UUID có sẵn không bị thay thế. Các PK vừa chuyển có default
`gen_random_uuid()` thay cho identity; Java cũng có thể tự sinh UUID.

Sau V5: **31 bảng ứng dụng + 1 bảng `flyway_schema_history` = 32 bảng public**,
nếu nhóm chưa thêm migration khác. Riêng 8 bảng Contract/Invoice từ V3:

```text
contracts                  contract_tenants
invoices                   invoice_billing_periods
invoice_items              invoice_item_allocations
invoice_draft_warnings     invoice_audit_events
```

Chỉ tác động schema ứng dụng `public`; không đổi auth/storage hoặc user/role của
Supabase. Migration không tự đổi grants/RLS. Quyền khác nhau giữa local và
Supabase là cấu hình môi trường; cấu trúc bảng/ID phải giống nhau.

## 3. Chạy migration Supabase ngay — dành cho người phụ trách

### 3.1. Chuẩn bị

1. Nhóm tạm dừng các backend, seed và tác vụ đang ghi vào Supabase cho đến hết
   bước kiểm tra sau migration. Không để code BIGINT cũ chạy với schema UUID.
2. Dùng bộ code chứa đầy đủ file migration và `pom.xml` trong cùng commit/PR.
3. Mở **PowerShell** ở `be-sba-project` (nơi chứa `pom.xml`):

   ```powershell
   java -version
   mvn -version
   ```

   Dùng JDK 21 và Maven 3.9.x. Không cần cài Flyway CLI hoặc plugin IntelliJ.
   Nếu chưa có Maven, thay `mvn` trong các lệnh bằng `.\mvnw.cmd`.

4. Khai báo kết nối Supabase **Session Pooler cổng 5432**, đối chiếu Dashboard →
   Connect. Không dùng transaction pooler 6543 cho lượt migration này.

   ```powershell
   $env:DB_URL = 'jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require'
   $env:DB_USERNAME = 'postgres.fmzupdyoxtlkxrfdtuly'
   $migrationPassword = Read-Host 'Nhap database password Supabase' -AsSecureString
   $migrationCredential = New-Object System.Management.Automation.PSCredential('database', $migrationPassword)
   $env:DB_PASSWORD = $migrationCredential.GetNetworkCredential().Password
   ```

   Mật khẩu chỉ đưa vào process environment. Không ghi vào lệnh `-D...password`,
   file Git hoặc log. Flyway Maven **không đọc `application-local.yml`**; ba biến
   trên là cấu hình của runner, kể cả khi IntelliJ đã kết nối datasource được.

### 3.2. Backup mới trước khi thay đổi

Giữ bản backup cũ, đồng thời xuất bản mới sát thời điểm migration. Lệnh dưới
dùng Docker `postgres:17`, không cần cài pg_dump trên Windows:

```powershell
$backupDir = Join-Path $env:USERPROFILE ('sba-db-backups\' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-before-uuid')
New-Item -ItemType Directory -Path $backupDir -ErrorAction Stop | Out-Null
$env:PGHOST = 'aws-0-ap-northeast-2.pooler.supabase.com'
$env:PGPORT = '5432'
$env:PGDATABASE = 'postgres'
$env:PGUSER = $env:DB_USERNAME
$env:PGPASSWORD = $env:DB_PASSWORD
$env:PGSSLMODE = 'require'
docker run --rm -e PGHOST -e PGPORT -e PGDATABASE -e PGUSER -e PGPASSWORD -e PGSSLMODE --mount "type=bind,source=$backupDir,target=/backup" postgres:17 pg_dump --format=custom --schema=public --no-owner --no-privileges --file=/backup/public-before-uuid.dump
if ($LASTEXITCODE -ne 0) { throw 'Backup failed. Stop here.' }
docker run --rm --mount "type=bind,source=$backupDir,target=/backup,readonly" postgres:17 pg_restore --list /backup/public-before-uuid.dump
if ($LASTEXITCODE -ne 0) { throw 'Cannot read backup. Stop here.' }
```

Nếu host hoặc project khác, sửa cả DB_URL và PGHOST/PGUSER cho cùng đích.
Backup này chứa schema và dữ liệu public, không phải backup toàn bộ dự án
Supabase. Giữ ngoài Git. `pg_restore --list` chỉ kiểm tra đọc archive; cách restore
thử thực tế nằm ở mục 7. Thực hiện restore thử trước khi cutover nếu chưa kiểm
chứng backup. Không import archive này ngược vào Supabase đang dùng.

### 3.3. Kiểm tra đích và baseline

Chạy trong PowerShell đã khai báo DB_URL:

```powershell
mvn -B -Pdatabase-migration flyway:info
if ($LASTEXITCODE -ne 0) { throw 'Flyway info failed' }
```

Kiểm tra dòng `Database: jdbc:postgresql://...` đúng Supabase cần migrate.
Nếu đã có version 3 thành công, bỏ qua baseline và đến mục 3.4 để nâng V4/V5.
Nếu đã ở V5, validate/info và làm mục 3.5.

Trước tiên chạy toàn bộ [007_current_schema_inventory.sql](sql/007_current_schema_inventory.sql)
trên Supabase, lưu kết quả database, cột, số hàng, constraint và RLS. Nếu có history,
chạy thêm câu SELECT history ở cuối file. Nếu chưa có lịch sử Flyway, chạy toàn bộ
[`docs/sql/004_check_legacy_schema_before_baseline.sql`](sql/004_check_legacy_schema_before_baseline.sql)
trong Query Console của **Supabase**. Script lấy khóa ngắn hạn rồi ROLLBACK,
không lưu thay đổi. Nó kiểm tra chính xác cấu trúc bằng catalog; mismatch sẽ báo
`SCHEMA_GUARD`, có bản ghi mới sẽ báo `DATA_GUARD`. Nếu Console báo lỗi trong
transaction, chạy `ROLLBACK;` rồi dừng để xử lý nguyên nhân.

Chạy baseline **chỉ lần đầu với schema cũ**:

```powershell
mvn -B -Pdatabase-migration flyway:baseline
if ($LASTEXITCODE -ne 0) { throw 'Baseline failed. Do not continue.' }
```

Kết quả phải có `Successfully baselined schema with version: 1`.
Callback tự kiểm tra lại ngay trong lệnh này, nên không thể bỏ qua guard chỉ
bằng cách bỏ bước Console. Baseline ghi nhận schema cũ là mốc 1, không chạy V1
CREATE TABLE lên Supabase và không chuyển kiểu ID ở bước này.

### 3.4. Áp dụng các version còn thiếu đến V5

```powershell
mvn -B -Pdatabase-migration flyway:migrate
if ($LASTEXITCODE -ne 0) { throw 'Migration failed. Keep writers stopped and inspect the error.' }
```

Kết quả phải có các dòng tương đương:

```text
Migrating schema "public" to version "2 - migrate application ids to uuid"
Migrating schema "public" to version "3 - add invoice draft tables"
Migrating schema "public" to version "4 - add room and appointment tables"
Migrating schema "public" to version "5 - integrate auth with shared accounts"
Successfully applied 4 migrations ... now at version v5
BUILD SUCCESS
```

Runner bật `group=true`: các migration pending trong lượt migrate chạy cùng
transaction PostgreSQL. Lỗi trong các version pending sẽ rollback lượt đó; baseline 1
từ lệnh trước có thể vẫn tồn tại. Không dùng `repair` hoặc xóa history khi chưa
hiểu lỗi. V2 kiểm tra lại dữ liệu dưới khóa bảng nên dữ liệu được thêm sau
baseline vẫn khiến migration dừng an toàn.

### 3.5. Kiểm tra sau migration

```powershell
mvn -B -Pdatabase-migration flyway:validate flyway:info
if ($LASTEXITCODE -ne 0) { throw 'Validation failed' }
```

Supabase đã baseline sẽ có V1 `Ignored (Baseline)`, một dòng `BASELINE` version 1,
V2–V5 `Success`. Đây là trạng thái đúng. Local mới có năm SQL migration Success.

Trong Query Console Supabase chạy toàn bộ
[`docs/sql/008_verify_current_schema.sql`](sql/008_verify_current_schema.sql).
Kết quả cuối phải là `UUID_SCHEMA_VERIFIED_V5`. Script kiểm tra số bảng, kiểu ID,
identity, kiểu tiền và FK; `flyway:validate` kiểm tra checksum/lịch sử chứ không
tự phát hiện mọi thay đổi schema thủ công.

Trong IntelliJ: chọn datasource Supabase → **Refresh/Synchronize** → mở
`postgres → public → tables`. Nếu thiếu bảng, vào Data Source Properties →
Schemas, chọn `public`, Apply rồi refresh. Đóng/mở lại console nếu autocomplete
còn dùng metadata cũ. Refresh chỉ tải metadata; không chạy migration.

Khởi động code đã đồng bộ UUID với `ddl-auto: validate`, kiểm thử các module
đang dùng, rồi mới cho cả nhóm chạy backend trở lại. Auth đã dùng accounts/UUID;
đăng nhập lại để nhận JWT có UUID subject. Cấu hình JWT_SECRET theo AUTH_MERGE.md.
Schema sẵn sàng không đồng nghĩa mọi feature đã được implement.

Sau khi hoàn tất, đóng PowerShell hoặc xóa credential khỏi process:

```powershell
Remove-Item Env:DB_PASSWORD, Env:PGPASSWORD -ErrorAction SilentlyContinue
$migrationCredential = $null
$migrationPassword = $null
```

## 4. Thành viên muốn có database local giống nhóm

Không nhập lại thủ công từng bảng hoặc chạy Hibernate update. Dùng PostgreSQL 17
local; ví dụ container đang có `sba-invoice-postgres`, port host 5433:

```powershell
docker exec sba-invoice-postgres createdb -U invoice_dev sba_uuid_dev
if ($LASTEXITCODE -ne 0) { throw 'Create database failed; do not reset an existing database.' }
$env:DB_URL = 'jdbc:postgresql://localhost:5433/sba_uuid_dev'
$env:DB_USERNAME = 'invoice_dev'
$localPassword = Read-Host 'Nhap database password LOCAL' -AsSecureString
$localCredential = New-Object System.Management.Automation.PSCredential('local', $localPassword)
$env:DB_PASSWORD = $localCredential.GetNetworkCredential().Password
mvn -B -Pdatabase-migration flyway:migrate flyway:validate flyway:info
if ($LASTEXITCODE -ne 0) { throw 'Local migration failed' }
```

V1→V2→V3→V4→V5 chạy tự động. Nếu database đã tồn tại, kiểm tra nó và dùng migrate khi
đúng lịch sử; hoặc tạo tên local mới. Không xóa database Invoice cũ để đổi tên.
Nếu thành viên mới chưa có PostgreSQL local, dùng Docker Desktop và chạy khối
dưới thay cho bước `createdb` ở trên. Không chạy nếu cổng 5433 đang được container
PostgreSQL khác sử dụng; khi đó dùng container có sẵn hoặc chọn cổng khác:

```powershell
$localPassword = Read-Host 'Nhap password PostgreSQL LOCAL moi' -AsSecureString
$localCredential = New-Object System.Management.Automation.PSCredential('local', $localPassword)
$env:POSTGRES_PASSWORD = $localCredential.GetNetworkCredential().Password
docker run --name sba-uuid-postgres --detach --publish 127.0.0.1:5433:5432 --env POSTGRES_USER=invoice_dev --env POSTGRES_PASSWORD --env POSTGRES_DB=sba_uuid_dev --mount type=volume,source=sba_uuid_dev_data,target=/var/lib/postgresql/data postgres:17
if ($LASTEXITCODE -ne 0) { throw 'Container creation failed; do not delete existing databases/volumes' }
docker exec sba-uuid-postgres pg_isready -U invoice_dev -d sba_uuid_dev
# Chờ đến khi accepting connections, rồi chạy profile local-dev ở mục 4.1.
Remove-Item Env:POSTGRES_PASSWORD
```

Database/container được giữ qua các lần restart. Chạy `docker start sba-uuid-postgres`
nếu container đã có nhưng đang dừng. Không chạy lại docker run cùng tên hoặc xóa volume
để xử lý lỗi migration. Tên container trong ví dụ này khác container test có sẵn.

Datasource IntelliJ local:

```text
Host: localhost        Port: 5433
Database: sba_uuid_dev  User: invoice_dev
```

Sau đó chạy SQL 005 để kiểm tra. Muốn dữ liệu demo Invoice: chỉ trên database
local này, chạy một lần [`docs/sql/006_uuid_local_demo_data.sql`](sql/006_uuid_local_demo_data.sql).
Không chạy lại 001/002. 006 tạo account placeholder LOCKED với password_hash `!`;
chúng không phải tài khoản đăng nhập thật. Profile `invoice-demo` dùng xác thực
in-memory riêng, như hướng dẫn [invoice-draft.md](invoice-draft.md).
Không bật profile demo hoặc nạp seed này vào Supabase của nhóm.

Đặt cấu hình backend local (ngoài `src/main/resources` và không commit):

```yaml
DB_URL: 'jdbc:postgresql://localhost:5433/sba_uuid_dev'
DB_USERNAME: 'invoice_dev'
DB_PASSWORD: 'YOUR_LOCAL_PASSWORD'
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Các biến môi trường có thể ghi đè file local; xóa biến kết nối Supabase cũ khỏi
Run Configuration khi chuyển sang local. Room/Tenant đã có bảng; các trường UUID
của Contract/Student 4 chưa khai báo FK vẫn là tham chiếu mềm, không tự đoán để backfill.
Tenant/Manager profile ID khác account ID; không tự đồng nhất hai loại danh tính.

### 4.1. Tự cập nhật schema local mỗi lần pull + chạy backend

Dùng database local mới hoặc local đã có đúng lịch sử Flyway. Cấu hình lần đầu:

```powershell
$env:LOCAL_DB_NAME = 'sba_uuid_dev'
$env:LOCAL_DB_PORT = '5433'
$env:LOCAL_DB_USERNAME = 'invoice_dev'
$env:LOCAL_DB_PASSWORD = $localCredential.GetNetworkCredential().Password
# JWT_SECRET: cấu hình theo AUTH_MERGE.md, không dùng mật khẩu DB làm JWT secret.
mvn spring-boot:run '-Dspring-boot.run.profiles=local-dev'
```

IntelliJ: Run Configuration → Active profiles = `local-dev`; Environment variables
có LOCAL_DB_NAME/PORT/USERNAME/PASSWORD và JWT_SECRET. Profile dùng localhost,
không lấy DB_URL Supabase. Không override `spring.datasource.url` hoặc
`spring.flyway.url` sang Supabase trong profile này.

Sau mỗi lần pull, chạy lại backend: Flyway migrate trước Hibernate validate.
Version đã thành công không chạy lại. Nếu checksum sai hoặc schema có bảng nhưng
không có history, startup dừng; không tự baseline, clean hoặc tạo lại bảng.
Pull Git tự nó không chạy SQL. Local có cùng cấu trúc, không tự có dữ liệu thật của
Supabase. Seed 006 chỉ là dữ liệu demo local tùy chọn; không chạy trên Supabase.

## 5. Thành viên dùng chung Supabase sau khi pull

1. Pull commit/nhánh nhóm đã thống nhất, chứa cả migration, mapping Java và tài liệu này.
2. Cấu hình DB_URL/DB_USERNAME/DB_PASSWORD riêng; không commit mật khẩu.
3. Cấu hình JWT_SECRET riêng, giữ `spring.flyway.enabled: false` và
   `spring.jpa.hibernate.ddl-auto: validate`; không bật `local-dev` trên Supabase.
4. Refresh datasource và xem `flyway_schema_history`: phải cùng version với nhóm.
5. Chạy backend, đăng nhập lại. Nếu mapping không khớp, sửa code/migration mới; không dùng update
   để ép database chạy theo entity riêng của một người.

Người dùng cùng Supabase không phải chạy lại baseline hoặc tự ALTER TABLE.
Pull Git chỉ tải file; thành viên dùng local phải chạy migrate trên local mình.
Từ lần thay đổi tiếp theo, người phụ trách chạy migration chung rồi thông báo
commit và version đã áp dụng cho nhóm.

Quy ước code mới:

- Entity PK/FK Java dùng `UUID`, repository dùng `JpaRepository<Entity, UUID>`.
- PK mới có thể dùng `@GeneratedValue(strategy = GenerationType.UUID)`;
  không dùng IDENTITY cho UUID. ID qua JSON là chuỗi UUID, không phải số Long.
- Branch giữ code/name/status và owner_id; quyền Manager đọc
  `manager_branch_assignments` đang active; manager_id ở bảng này trỏ accounts.
  `branches.manager_id` của Student 2 trỏ managers (profile), không thay quyền Invoice.
- Mỗi PR đổi schema có migration version tiếp theo (V6, V7...) được thống nhất
  trong nhóm. Không sửa bất kỳ migration đã áp dụng lên database dùng chung.
- Không bật `baseline-on-migrate`, không tự baseline một schema khác, không dùng clean.
- Không commit archive dữ liệu thật, credential, token, log hoặc application-local.yml.
- Các migration ở `src/main/resources/db/migration` là nguồn chuẩn; 001/002 chỉ
  là tài liệu demo cũ, không phải một cách dựng schema song song.

## 6. Khi bị chặn

| Thông báo | Cách xử lý |
| --- | --- |
| DATA_GUARD | Có dữ liệu mới. Giữ dữ liệu, dừng chuyển đổi; cần migration ánh xạ ID |
| SCHEMA_GUARD | Schema hoặc major PostgreSQL khác bản đã kiểm chứng; xuất DDL và review khác biệt |
| lock timeout | Có session giữ khóa. Dừng nguồn ghi, chờ transaction kết thúc rồi chạy lại |
| Non-empty schema without schema history | Với đúng schema cũ, chạy baseline có guard. Với schema khác, không tự baseline |
| Checksum mismatch | Khôi phục file migration đúng bản đã chạy, tạo migration mới cho thay đổi |
| Password authentication failed | Kiểm tra DB_USERNAME dạng pooler và database password; không dùng anon key |
| Schema-validation: missing column manager_id | Chưa áp dụng V4 hoặc kết nối nhầm database |
| Schema-validation: missing table | Kiểm tra đúng database, history và profile; không đổi sang update |
| AUTH_DATA_GUARD | Email trùng không phân biệt hoa thường; đối soát, không tự xóa/gộp account |
| AUTH_SCHEMA_GUARD | Có users riêng hoặc accounts chưa UUID; review mapping dữ liệu trước |

Nếu lỗi trong Query Console để lại transaction aborted, chạy `ROLLBACK;`.
Flyway tự rollback transaction lỗi. Giữ nhóm dừng ghi khi trạng thái cutover chưa
được xác minh. Sau commit thành công không có nút undo tự động trong bộ này;
rollback vận hành cần khôi phục backup đã kiểm chứng và chạy lại code BIGINT
tương thích. Nếu đã phát sinh ghi mới sau cutover, không restore đè làm mất dữ liệu;
ưu tiên migration sửa tiếp hoặc lập phương án đối soát.

## 7. Restore thử backup vào local riêng

Giả sử `$backupDir` từ mục 3.2 còn trong PowerShell. Dùng database riêng, không
ghi đè `sba_invoice_dev` hoặc Supabase. Archive có CREATE SCHEMA public, nhưng
database mới đã có schema đó; lọc đúng mục tạo/comment schema khỏi TOC khi restore:

```powershell
$toc = docker run --rm --mount "type=bind,source=$backupDir,target=/backup,readonly" postgres:17 pg_restore --list /backup/public-before-uuid.dump
if ($LASTEXITCODE -ne 0) { throw 'Cannot read archive' }
$toc = $toc | Where-Object { $_ -notmatch '\sSCHEMA - public\s' -and $_ -notmatch '\sCOMMENT - SCHEMA public\s' }
[IO.File]::WriteAllLines((Join-Path $backupDir 'public.restore.list'), [string[]]$toc, (New-Object System.Text.UTF8Encoding($false)))
docker exec sba-invoice-postgres createdb -U invoice_dev sba_uuid_rehearsal
if ($LASTEXITCODE -ne 0) { throw 'Choose a new rehearsal database; do not overwrite existing data' }
$restorePassword = Read-Host 'Nhap database password LOCAL' -AsSecureString
$restoreCredential = New-Object System.Management.Automation.PSCredential('local', $restorePassword)
$env:PGPASSWORD = $restoreCredential.GetNetworkCredential().Password
docker run --rm -e PGPASSWORD --mount "type=bind,source=$backupDir,target=/backup,readonly" postgres:17 pg_restore --host=host.docker.internal --port=5433 --username=invoice_dev --dbname=sba_uuid_rehearsal --no-owner --no-privileges --exit-on-error --single-transaction --use-list=/backup/public.restore.list /backup/public-before-uuid.dump
if ($LASTEXITCODE -ne 0) { throw 'Restore failed. Stop and inspect the error.' }
```

Các PGHOST/PGSSLMODE ở host không được truyền vào container restore. Lệnh restore
này luôn trỏ LOCAL qua host.docker.internal:5433, không dùng DB_URL Supabase.
Đối chiếu số bản ghi bằng script 003 trên database vừa restore. Muốn diễn tập
nâng cấp, đổi DB_URL/DB_USERNAME/DB_PASSWORD sang local này rồi làm baseline →
migrate → validate như mục 3. Nếu archive có dữ liệu thì guard phải từ chối.
Mở PowerShell khác với kết nối Supabase khi thực hiện cutover để tránh nhầm đích.

## 8. Kiểm thử lại bộ migration

Với container local `sba-invoice-postgres` đang chạy ở localhost:5433:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-DatabaseMigration.ps1
```

Script dùng password local mẫu `InvoiceDev_2026`; có thể truyền `-LocalPassword`
qua biến đã nhập bằng Read-Host nếu cấu hình khác. Script chỉ tạo các database
`sba_uuid_test_<timestamp>_*`, không xóa database có sẵn và không dùng Supabase.
Kiểm tra bootstrap, upgrade, chạy migrate lần hai, từ chối dữ liệu/schema khác,
dữ liệu phát sinh sau baseline, Hibernate validate và vòng đời Invoice trên PG.
Các database test và log trong target được giữ để kiểm tra.

Kết quả snapshot V1–V3: [biên bản cũ](database/migration-validation-2026-10-10.md).
Kết quả V1–V5 sau merge: [biên bản hiện tại](database/migration-v5-validation.md).

## 9. Quyền Supabase quan sát được

Preflight xác nhận anon/authenticated có quyền rộng và 16 bảng đang tắt RLS.
Nếu nhóm chỉ dùng Spring backend/JDBC, kiểm tra và tắt Data API không sử dụng
trong Supabase; nếu frontend cần Data API, chủ module phải thiết kế grants/RLS
phù hợp trước khi nạp dữ liệu nhạy cảm. Migration UUID này không tự thu hồi quyền
hoặc đổi cơ chế truy cập của module khác.

## 10. Các file cần push cùng nhau

Review và commit toàn bộ entity/repository/security/DTO đã đổi UUID, phần Invoice
còn untracked, pom.xml, application.yml, application-local-dev.yml, db/migration
V1–V5 và callback, tests, scripts, tài liệu này. Chỉ push SQL mà thiếu code UUID
hoặc thiếu Branch/Contract sẽ khiến thành viên pull về không chạy được.

Chạy `git status --short`, `git diff --check` trước commit. Không đưa file local,
.env, backup dump hoặc log vào Git. Đưa thay đổi qua PR của nhóm; sau review/test,
người phụ trách áp dụng đúng commit đó lên Supabase. Pull không lấy được file còn
untracked/chưa commit trên máy của người khác.

## Tham khảo

- [Spring Boot Flyway startup](https://docs.spring.io/spring-boot/how-to/data-initialization.html)

- [Flyway Maven](https://documentation.red-gate.com/flyway/reference/usage/maven-goal)
- [Flyway baseline](https://documentation.red-gate.com/flyway/reference/commands/baseline)
- [PostgreSQL pg_restore](https://www.postgresql.org/docs/17/app-pgrestore.html)
- [Supabase connections](https://supabase.com/docs/guides/database/connecting-to-postgres)
