> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../../backend/README.md).

# Biên bản kiểm thử migration V1–V3

Ngày kiểm thử: 10/10/2026 (UTC+7). Môi trường: PostgreSQL 17.11 trong container
local `sba-invoice-postgres`, Flyway Maven 12.4.0. Không thực hiện migration trên
Supabase thật trong lượt này; database `sba_invoice_dev` được giữ nguyên.

## Kết quả

| Kiểm thử | Kết quả |
| --- | --- |
| Local trống chạy V1→V2→V3 | PASS, 24 bảng ứng dụng + history |
| Migrate lần hai | PASS, không chạy lại các migration đã áp dụng |
| Schema legacy V1 → baseline 1 → V2/V3 | PASS |
| So sánh cột/kiểu/nullability của bootstrap và upgrade | PASS, giống nhau |
| Có account trước baseline | DATA_GUARD, giữ bản ghi và kiểu BIGINT |
| Có cột ngoài schema đã review | SCHEMA_GUARD, từ chối baseline |
| Thêm account giữa baseline và migrate | DATA_GUARD, giữ dữ liệu, history chỉ có baseline |
| V3 bị gây lỗi sau khi V2 đổi kiểu ID | Rollback cả V2/V3; accounts.id trở lại BIGINT, chưa có invoices, history chỉ có baseline |
| Hibernate validate trên PostgreSQL đã migrate | PASS |
| Tạo/sửa/refresh/hủy/xem Invoice bằng Manager được phân công | PASS trên PostgreSQL |
| Thu hồi phân công Manager | PASS, không còn quyền đọc Invoice |
| Manager active nhưng đã có unassignedAt | PASS trong integration test bổ sung |
| Bộ test backend hiện có | PASS; test PG được chạy riêng bằng biến môi trường dành cho local test |

Luồng rollback dùng bản sao migration trong `target`, cố ý thêm lỗi vào V3 của
bản sao; không thay đổi file migration thật để kiểm thử.

## Kiểm chứng backup người dùng cung cấp

Đã đọc và khôi phục archive `public-before-uuid.dump` được xuất ngày 10/10/2026
lúc khoảng 10:36 vào database local mới `sba_uuid_restore_20261010`. Restore dùng
`--no-owner --no-privileges --exit-on-error --single-transaction`, bỏ đúng entry
CREATE/COMMENT SCHEMA public khỏi TOC để giữ schema public của database mới.

Trên bản khôi phục thực tế này, callback kiểm tra legacy schema thành công;
baseline 1 → V2 → V3 → Flyway validate đều thành công. Không chỉ kiểm thử trên
schema tự tạo từ V1. Bản backup này không chứng minh trạng thái Supabase tại
thời điểm cutover sau đó; người triển khai vẫn cần backup mới và chạy guard.

## Tái chạy

Tại `be-sba-project`, với PostgreSQL local đã cấu hình theo hướng dẫn:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-DatabaseMigration.ps1
mvn test
```

Script kiểm chứng đầy đủ đã chạy với prefix database
`sba_uuid_test_20261010123229_*`. Các database và log trong target được giữ để
kiểm tra, không đưa lên Git. Test PostgreSQL riêng là `SharedSchemaPostgresTests`;
nếu không đặt biến môi trường test, Maven mặc định bỏ qua test này và dùng H2
cho các test backend hiện có.

Không kiểm thử xác thực Account thật, Payment, VietQR hoặc module chưa tồn tại
trong checkout. Xem [hướng dẫn triển khai](../DATABASE_MIGRATION.md) để chạy
migration trên Supabase và đồng bộ datasource cho nhóm.
