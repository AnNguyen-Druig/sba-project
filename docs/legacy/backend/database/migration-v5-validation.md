> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../../backend/README.md).

# Kiểm chứng migration V1–V5 sau merge Auth/Student 2

Ngày 10/10/2026. Docker PostgreSQL 17.11, container `sba-invoice-postgres`,
localhost:5433. Không chạy migration hoặc thay đổi Supabase.

Lệnh: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/Test-DatabaseMigration.ps1`.
Lượt chạy `20261010140440`, exit code 0, `ALL CHECKS PASSED`.
Các database `sba_uuid_test_20261010140440_*` và log `target/uuid-20261010140440-*.log`
được giữ để kiểm tra; không xóa database local cũ.

Lượt `mvn test` toàn bộ sau cùng: 102 test, 98 đạt, 4 test PostgreSQL bỏ qua theo
điều kiện môi trường, không failure/error. Cả 4 test PostgreSQL này đã chạy đạt
riêng trong script ở trên (3 test schema/Auth/Invoice và 1 test startup; startup
được chạy thêm lần hai). Script inventory 007 cũng đã chạy thành công trong
transaction READ ONLY trên PostgreSQL V5 và kết thúc bằng ROLLBACK.

| Tình huống | Kết quả |
| --- | --- |
| Database trống → V1–V5; migrate lần hai | Đạt; history giữ đúng 5 version |
| Schema 16 bảng legacy → baseline 1 → V2–V5 | Đạt; cấu trúc cột khớp bootstrap mới |
| V3 có account/branch/contract/tenant assignment mẫu → V5 | Đạt; checksum giá trị hàng trước/sau không đổi, trừ cột manager_id mới nullable |
| Email legacy trùng khác hoa/thường | V5 từ chối; giữ cả 2 account và rollback V4 |
| Có dữ liệu trước baseline hoặc phát sinh sau baseline | DATA_GUARD, dữ liệu giữ nguyên, ID vẫn BIGINT |
| Schema drift thêm cột ngoài snapshot | SCHEMA_GUARD, không baseline sai |
| Lỗi cưỡng bức ở V3 trong bản sao target | Rollback V2/V3; ID vẫn BIGINT, chưa có Invoice, history chỉ baseline |
| Hibernate validate toàn bộ entity trên PostgreSQL V5 | Đạt |
| Invoice create/update/refresh/cancel và thu hồi quyền Manager | Đạt |
| Email login → JWT UUID → create/get Invoice | Đạt trên PostgreSQL thật |
| Đổi email sau khi có token; khóa account | Token vẫn nhận diện đúng UUID sau đổi email; khóa chặn access/refresh/login |
| Ghi Manager/Tenant/RoomType/Room/Slot/Appointment | Đạt với FK PostgreSQL thật |
| Profile local-dev khởi động database trống và lần hai | Tự migrate trước Hibernate validate; không thêm lịch sử lần hai |

V5 có 31 bảng ứng dụng + `flyway_schema_history` = 32 bảng public. Business ID/FK
là UUID; `invalidate_tokens.id` giữ varchar(36) cho JWT jti và tiền BIGINT giữ nguyên.
V1–V3 không sửa. Không backfill hoặc đoán liên kết Manager/Tenant profile với Account.

## Giới hạn

- Không xác nhận trạng thái/schema/data Supabase trực tiếp ở thời điểm triển khai.
- Đường BIGINT có dữ liệu được kiểm thử **từ chối an toàn**, chưa có migration ánh xạ
  cho dữ liệu BIGINT thực. Nếu 007/004 báo dữ liệu, giữ nguyên và xuất snapshot để review.
- Kiểm thử giữ dữ liệu V3 dùng fixture có kiểm soát; vẫn phải backup và diễn tập
  restore bản mới của Supabase trước cutover.
- Các bảng/column do Hibernate update tạo thủ công ngoài history phải được review,
  không tự baseline hoặc xóa để vượt guard.
