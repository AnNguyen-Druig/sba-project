> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../../backend/README.md).

# Kết quả kiểm tra trước migration UUID

**Cập nhật sau review:** bộ Flyway V1–V3 đã được implement và kiểm thử local.
Các bước dự kiến bên dưới ghi lại trạng thái tại thời điểm review ban đầu;
để thao tác, dùng [hướng dẫn hiện hành](../DATABASE_MIGRATION.md).

Đối chiếu các CSV và log Query Console do người dùng cung cấp, không phải kết quả
kết nối trực tiếp từ công cụ vào Supabase. Thời điểm snapshot: 2026-10-10 khoảng
10:50–10:51, UTC+7. Yêu cầu giữ toàn bộ dữ liệu vẫn áp dụng.

## Kết luận

Script `../sql/003_uuid_migration_preflight.sql` đã chạy đầy đủ trong transaction
REPEATABLE READ READ ONLY. Log ghi COMMIT lúc 10:51:08, hoàn thành lúc 10:51:09;
không thấy lỗi SQL trong lượt chạy này. Script không thay đổi schema hoặc dữ liệu.

| Nội dung | Kết quả |
| --- | --- |
| Database / user / server | postgres / postgres / PostgreSQL 17.11 |
| Số bản ghi của 16 bảng ứng dụng | Cả 16 bảng đều bằng 0 |
| Các cột được truy vấn ở mục 2 | 50 dòng; có cả BIGINT và UUID |
| FK liên quan public | 14; đều validated; source và target đều thuộc public |
| Các tham chiếu UUID được kiểm tra | 14 cột; non_null_rows và distinct_ids đều 0 |
| Nhóm audit entity, VietQR entity, notification reference | Cả ba truy vấn đều trả 0 dòng |
| RLS | 16 bảng đều rls_enabled=false và rls_forced=false |
| Policy trong public | 0 dòng |
| Quyền anon/authenticated | 224 dòng: 16 bảng × 2 role × 7 quyền |

Nguồn trong thư mục CSV người dùng cung cấp:

- `Result_1_3.csv`: số bản ghi của từng bảng.
- `1__Confirm_the_targeon_stale_UI_metadata.csv`: database, user và phiên bản.
- `2__Actual_key_like_ceclared_foreign_keys.csv`: kiểu cột.
- `3__Include_inbound_other_schemas_as_well.csv`: khóa ngoại.
- `by_a_cast__Obtain_th_that_wrote_the_data.csv`: tham chiếu UUID.
- `7__Compare_actual_RLmission_changes_here.csv`: RLS.
- `postgres_informationhema_table_privileges.csv`: quyền bảng.

CSV của `audit_rows_with_unresolved_actor` chưa được gửi, dù log ghi truy vấn trả
**1 dòng**. COUNT(*) vẫn trả một dòng chứa số 0 khi không có bản ghi phù hợp.
Do audit_logs có 0 bản ghi trong cùng snapshot, có thể suy ra giá trị này là 0;
đây là suy luận, không phải đọc trực tiếp ô kết quả chưa được xuất.

Không cần chạy lại toàn bộ chỉ để bổ sung CSV đó. Những result grid GROUP BY
và pg_policies trả 0 dòng là kết quả hợp lệ, không phải lỗi thực thi.

## Phạm vi kết luận

- Chỉ xác nhận 16 bảng public được liệt kê tại thời điểm snapshot; không xác nhận
  auth/storage hay toàn bộ dự án Supabase rỗng, hoặc không có dữ liệu mới sau đó.
- Không thấy FK khai báo xuyên schema trong kết quả; điều này không chứng minh
  không có dependency qua view, function, trigger hoặc ứng dụng bên ngoài.
- Dữ liệu Invoice đã test ở sba_invoice_dev là dữ liệu local, không thuộc kết quả
  đếm bảng của Supabase này.
- Không có thay đổi quyền, schema, dữ liệu hay cấu hình Data API trong lượt review.

## Điều chỉnh hướng migration

Có thể chuẩn bị V2 dành cho **schema cũ đã có bảng nhưng các bảng ứng dụng còn
rỗng**. Migration phải khóa các bảng liên quan và kiểm tra lại điều kiện rỗng
trong cùng transaction trước khi đổi kiểu. Nếu có dữ liệu hoặc cấu trúc khác
schema dự kiến, phải báo lỗi và dừng; không truncate, xóa dữ liệu hoặc ép null.

Theo hướng này không cần tạo bảng ánh xạ cho snapshot rỗng hiện tại. Nếu có dữ
liệu mới trước cutover, quay lại thiết kế ánh xạ BIGINT → UUID và tham chiếu mềm
được kiểm chứng. Không sửa điều kiện kiểm tra để bỏ qua dữ liệu mới.

V2 cần xử lý PK, FK, identity/default và các ID không khai báo FK cùng nhau.
Giữ cột tiền, số lượng, bộ đếm ở kiểu số; giữ UUID hiện hữu. V1 vẫn là cấu trúc
cũ portable, V3 bổ sung Contract/Invoice sau khi hợp nhất entity với nhóm.

## Các bước tiếp theo

Người dùng xác nhận main là nguồn code đầy đủ nhất và đã merge trước khi làm
Invoice. Kiểm tra các ref hiện có trên máy (chưa fetch trong lượt review này):
HEAD/feat/invoice-draft và main ở `7eed841`, origin/main ở `eb7261e`, phía trước
HEAD một commit `[Add] Add student 4 feature`. Cả cây main và origin/main được
kiểm tra đều chưa có Account/ManagerBranchAssignment. Không tự merge vào working
tree đang có thay đổi. Có bảng trong Supabase không chứng minh code tạo bảng đã
được commit vào main; không thể xác định nguồn tạo bảng chỉ từ các CSV.

1. Đối chiếu code main với schema chung; thống nhất Tenant ID và mọi nơi nhận/trả
   ID. Dùng main làm nguồn tích hợp theo xác nhận của người dùng. Các module chưa
   có trong main cần được chủ module bổ sung/tích hợp với ID UUID, không yêu cầu
   merge lặp lại để tìm các class không tồn tại trong cây code đã kiểm tra.
   Có thể chuẩn bị baseline/chuyển schema từ DDL trước; kiểm chứng end-to-end
   authentication/branch vẫn phải chờ code module tương ứng.
2. Chuẩn bị Flyway V1/V2/V3 và code UUID trong cùng thay đổi có thể review.
   Hiện các file versioned migration này chưa tồn tại, chưa có lệnh migrate
   thực tế để chạy. Không dùng 001/002 của demo Invoice thay cho migration nhóm.
3. Dùng hai database local riêng để kiểm thử: database trống chạy V1→V2→V3;
   bản khôi phục schema cũ được đối chiếu V1, baseline 1 rồi chạy V2→V3.
   Không ghi đè sba_invoice_dev. Backup chỉ được coi là đã kiểm chứng khi đã
   khôi phục thử thành công. Nếu bản khôi phục có dữ liệu, V2 rỗng phải từ chối.
4. Kiểm thử trường hợp V2 gặp dữ liệu và phải dừng, kiểm tra FK/default/index,
   validate mapping Hibernate, rồi chạy API auth/branch/service/invoice.
5. Trước cutover Supabase: phối hợp tạm dừng các nguồn ghi, backup mới, kiểm tra
   lại schema/dữ liệu. Một runner thực hiện baseline có chủ đích và migrate;
   chỉ mở ghi khi code UUID và schema đã được kiểm chứng cùng nhau.
6. Thành viên dùng Supabase refresh datasource và dùng code mới; thành viên dùng
   local pull migration rồi migrate database riêng. Pull Git không tự đổi DB.

Grants và RLS xác nhận từ catalog cần được xử lý cùng nhóm trước khi đưa dữ liệu
thật vào các bảng nhạy cảm. Nếu public được expose qua Data API, các role đang
có quyền rộng và không có RLS hạn chế hàng. Kiểm tra nhu cầu Data API rồi cấu
hình quyền phù hợp; không sao chép nguyên grants vào baseline local mặc định.
