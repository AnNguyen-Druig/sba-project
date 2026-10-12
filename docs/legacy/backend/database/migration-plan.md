> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../../backend/README.md).

# Kế hoạch hợp nhất schema và chuyển ID sang UUID

Ngày đối chiếu: 2026-10-10. Yêu cầu: giữ toàn bộ dữ liệu hiện có.

**Đã triển khai bộ V1–V3 và runner Flyway Maven.** Hướng dẫn thao tác hiện hành:
[DATABASE_MIGRATION.md](../DATABASE_MIGRATION.md). Tài liệu này giữ bối cảnh
thiết kế; chưa có thao tác migration trực tiếp trên Supabase từ công cụ.

## Trạng thái và nguồn

- `reference/public-before-uuid-2026-10-10.sql` là bản chụp DDL do người dùng xuất
  từ Supabase PostgreSQL 17.11 bằng pg_dump 17.11. Đây là tài liệu tham chiếu,
  **không phải migration và không được chạy nguyên file vào database hiện có**.
  SHA-256 bản gốc: `C7F3F3116F156B08E9A5B4359B5FC8DD60058584621D4E0A6A7A936BC6089994`.
- File có lệnh psql `\restrict`, `\unrestrict`, CREATE SCHEMA public, ownership và
  grants cho các role Supabase; không đưa nguyên bản vào Flyway.
- `../sql/003_uuid_migration_preflight.sql` chỉ đọc dữ liệu/catalog; chạy trước khi
  chuyển UUID trên Supabase hoặc bản sao được khôi phục. Xuất tất cả result grid.
- Đã có V1/V2/V3 và callback trước baseline trong classpath; runner Maven nằm
  trong profile `database-migration`. Chưa thay đổi database chung.
- Các script `001_invoice_draft.sql`, `002_invoice_demo_data.sql` dành cho nền
  Invoice độc lập; chưa tương thích Branch/Account đầy đủ của nhóm.

## Kết quả đối chiếu DDL

16 bảng, 9 khóa chính BIGINT/identity và 7 khóa chính UUID; 14 FK được khai báo.

| BIGINT PK cần chuyển | UUID PK đã có, cần giữ nguyên |
| --- | --- |
| accounts | services |
| audit_logs | room_services |
| bank_accounts | room_service_charges |
| branches | contract_services |
| login_attempts | cost_allocation_proposals |
| manager_branch_assignments | cost_allocation_items |
| otp_verifications | notifications |
| refresh_tokens | |
| vietqr_references | |

Giữ `vietqr_references.amount` là BIGINT: đó là tiền, không phải ID. Giữ các bộ
đếm, version và kiểu numeric của tiền/số lượng. Không thay UUID đã tồn tại.

Các FK BIGINT cần dùng cùng ánh xạ với bảng cha:

- accounts.id -> branches.owner_id, bank_accounts.owner_id,
  manager_branch_assignments.manager_id, login_attempts.account_id,
  otp_verifications.account_id, refresh_tokens.account_id.
- branches.id -> bank_accounts.branch_id, manager_branch_assignments.branch_id.
- bank_accounts.id -> vietqr_references.bank_account_id.

Không thể suy ra mọi quan hệ chỉ từ FK:

- audit_logs.actor_id không có FK; phải giữ được lịch sử người dùng đã xóa.
- audit_logs.entity_id phụ thuộc entity_type.
- vietqr_references.related_entity_id phụ thuộc related_entity_type/purpose;
  dump không có bảng Invoice/Deposit để tự suy ra quan hệ.
- services.branch_id, notifications.branch_id đã là UUID trong khi Branch có
  PK BIGINT. Các UUID người tạo/người nhận/người xử lý cũng chưa có FK đến Account.
  Nếu có dữ liệu, cần xác định UUID do module nguồn dùng đại diện cho đối tượng
  nào. Không tự ghép theo thứ tự bản ghi, không cast BIGINT thành UUID, không
  sinh UUID mới tùy ý rồi coi các liên kết này đã đúng.
- Tenant ID có thể khác Account ID; cần hợp nhất theo module sở hữu Tenant.
- Các tham chiếu Room/Contract chưa có bảng cha trong dump.

Schema-only không cho biết bảng nào rỗng hoặc mã entity_type thực tế. Preflight
ngày 2026-10-10 đã hoàn thành: cả 16 bảng được đếm đều có 0 bản ghi trong snapshot
10:50–10:51 UTC+7. Xem [biên bản đối chiếu](preflight-review-2026-10-10.md).
Code Account/Branch/Manager assignment mới nhất vẫn cần được hợp nhất.

Có thể dùng migration có điều kiện các bảng ứng dụng còn rỗng: khóa bảng và kiểm
tra lại trong cùng transaction trước khi đổi kiểu; dừng nếu có dữ liệu mới hoặc
schema không khớp. Không cần bảng ánh xạ cho snapshot rỗng, nhưng không được
xóa dữ liệu mới để vượt qua điều kiện. Các quy tắc ánh xạ bên dưới áp dụng khi
có dữ liệu và phải chọn hướng migration bảo toàn dữ liệu tương ứng.

## Quyền truy cập quan sát được

Dump có GRANT ALL cho anon/authenticated trên accounts, refresh_tokens,
otp_verifications và các bảng khác; không có ENABLE ROW LEVEL SECURITY hoặc
CREATE POLICY trong file. Nếu Data API đang expose public, cần kiểm tra khả năng
truy cập các bảng này. Chưa kiểm chứng cấu hình Data API từ DDL.

Preflight đã xác nhận catalog: 16 bảng đều tắt RLS, không có policy trong public,
224 dòng quyền cho anon/authenticated. Đây là trạng thái quan sát được, không
chỉ là suy luận từ dump; cấu hình exposed schema của Data API vẫn chưa xác minh.

Giữ bản chụp grants để review, nhưng không sao chép chúng một cách mặc định vào
baseline portable. Chốt quyền với nhóm và quản lý bằng migration/thiết lập riêng.
Nếu chỉ dùng Spring JDBC, cân nhắc tắt Data API không sử dụng. Không tự sửa auth,
storage hoặc role do Supabase quản lý để phục vụ entity ứng dụng.

## Thứ tự triển khai

1. Preflight đã hoàn thành; xác nhận code mới nhất của module Account/Branch và
   ID Tenant. Kiểm tra lại trạng thái database sát thời điểm triển khai.
2. Lập inventory từng ID/FK/tham chiếu mềm. Với snapshot rỗng hiện tại, ưu tiên
   migration có điều kiện rỗng; nếu có dữ liệu, cần bảng ánh xạ được kiểm chứng.
3. Khôi phục backup vào database diễn tập riêng; không ghi đè sba_invoice_dev.
4. Soạn baseline V1 portable giữ đủ 16 bảng, identity, constraint, index của schema
   cũ; tách các role/grants đặc thù môi trường. So sánh với dump gốc.
5. Soạn V2 chuyển ID với kiểm tra điều kiện rỗng có khóa bảng. Nếu cần hỗ trợ
   database có dữ liệu, phải thiết kế hướng ánh xạ riêng: mỗi BIGINT PK được ánh
   xạ một lần; UUID đang được dùng ngoài bảng cha phải được đối chiếu trước khi
   chọn UUID mới cho đối tượng đó. Không tự bỏ điều kiện rỗng.
6. Giữ dữ liệu legacy/mapping cho audit và tham chiếu chưa giải nghĩa được; dừng
   migration nếu gặp trường hợp chưa có quy tắc. Không xóa, đặt null hoặc tạo tài
   khoản/hóa đơn giả để làm FK hợp lệ.
7. Cập nhật Java/API/auth/frontend; dùng manager_branch_assignments thay cho
   branches.manager_id giả định. Giữ các cột code/status bắt buộc của Branch.
8. Soạn V3 tạo Contract/ContractTenant/Invoice còn thiếu theo schema đã thống nhất,
   không tạo lại branches. Viết lại seed demo tương thích schema nhóm.
9. Kiểm thử PostgreSQL cả database trống và bản khôi phục schema cũ; kiểm tra
   rollback, số bản ghi, FK, constraints, quyền và API các module. Với V2 yêu cầu
   rỗng, kiểm thử thêm bản sao có dữ liệu để xác nhận migration dừng và giữ dữ
   liệu. Nếu triển khai ánh xạ dữ liệu, phải kiểm thử ánh xạ thành công riêng.
10. Một người/pipeline triển khai migration trên Supabase trong thời gian dừng ghi,
    cùng bản code UUID tương thích. Backup sát thời điểm cutover; code BIGINT cũ
    không được tiếp tục ghi sau khi đổi schema. Kiểm chứng trước khi mở ghi lại.

Không dùng ddl-auto:update để chuyển kiểu khóa. Baseline chỉ được ghi nhận khi
database hiện hữu khớp V1; baseline không tự chứng minh hoặc sửa sự tương thích.
Local Invoice độc lập đang có schema khác nên không được đánh dấu V1 tùy tiện.

## Cấu trúc migration dự kiến dùng chung

```text
src/main/resources/db/migration/
  V1__existing_application_schema.sql
  V2__migrate_application_ids_to_uuid.sql
  V3__add_invoice_draft_tables.sql
docs/database/
  migration-plan.md
  reference/public-before-uuid-2026-10-10.sql
```

Triển khai thực tế dùng Flyway Maven plugin 12.4.0 (phiên bản quản lý bởi Boot),
không dùng auto-migrate khi backend khởi động. Plugin có PostgreSQL module/driver,
baselineOnMigrate=false, cleanDisabled=true, group=true. Hibernate dùng validate.
Local chạy runner trên database riêng; Supabase chỉ do người phụ trách chạy.

- Database trống: migrate V1 -> V2 -> V3.
- Database cũ khớp V1: baseline version 1 có chủ đích -> migrate V2 -> V3.
- Không chạy V1 CREATE TABLE trên Supabase đã tồn tại.
- Không chỉnh migration đã áp dụng. Thay đổi tiếp theo dùng version mới; phối hợp
  tránh trùng số version giữa các PR.
- Seed dữ liệu giả tách riêng local/test, không tự nạp vào database chung.
- Snapshot schema sau migration giúp review; migration đã commit mới là lịch sử
  có thể tái tạo. Raw dump không phải nguồn bootstrap chạy song song với Flyway.

## Thành viên khác cập nhật như thế nào?

Pull code chỉ tải entity/migration, không tự thay đổi database local. Sau khi các
file migration được hoàn thiện và merge, từng thành viên:

1. Pull nhánh chung chứa cả code UUID và migration.
2. Cấu hình database local riêng, bật Flyway cho database đó.
3. Chạy migration trước khi sử dụng code mới; khởi động app với ddl-auto:validate.
4. Chạy seed local giả nếu cần, refresh datasource IntelliJ.
5. Đối chiếu flyway_schema_history và chạy test module.

Người dùng cùng Supabase chỉ cần kết nối/refresh sau khi runner đã migrate;
không tự chạy lại SQL. Mỗi PR đổi entity phải kèm migration và test PostgreSQL.

Commit: DDL tham chiếu đã review, migration, code đồng bộ, cấu hình mẫu, test,
tài liệu và seed giả. Không commit archive có dữ liệu thật, mật khẩu, OTP/token
hoặc file cấu hình local.

## Nguồn tham khảo

- https://docs.spring.io/spring-boot/how-to/data-initialization.html
- https://documentation.red-gate.com/flyway/reference/commands/baseline
- https://www.postgresql.org/docs/17/app-pgrestore.html
- https://supabase.com/docs/guides/api/securing-your-api
