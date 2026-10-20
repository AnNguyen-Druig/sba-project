> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../backend/README.md).

# SBA Project — Backend

Hướng dẫn database: [DATABASE_MIGRATION.md](DATABASE_MIGRATION.md).
Đọc [AUTH_MERGE.md](AUTH_MERGE.md) để cấu hình `JWT_SECRET` và chạy frontend login.
Auth dùng chung `accounts`/UUID với Invoice; migration V1–V5 bao phủ entity hiện tại.
V2 chỉ chuyển schema BIGINT legacy đúng snapshot, còn trống; có dữ liệu sẽ dừng để
bảo toàn dữ liệu. Giữ `ddl-auto: validate`, không dùng Hibernate update trên Supabase.

Java 21, Spring Boot 4.1.1, Spring Data JPA, Spring Security và PostgreSQL (Supabase).
Phạm vi hiện tại: **Phase 1** theo [phân công backend](../../be_tasks.md) và
[danh sách tính năng](../../all-feature.txt).

## Chạy trên máy cá nhân

1. Cài JDK 21, đặt `JAVA_HOME` và mở terminal trong `be-sba-project`.
2. Sao chép cấu hình mẫu (chỉ làm nếu chưa có file local):

   ```powershell
   Copy-Item application-local.example.yml application-local.yml
   ```

3. Sửa `DB_PASSWORD` trong `application-local.yml` thành mật khẩu PostgreSQL do nhóm
   cung cấp riêng. URL và username mẫu lấy từ thông tin Session Pooler của nhóm;
   đối chiếu lại trong Supabase Dashboard → Connect nếu thông tin kết nối thay đổi.
   Giữ `sslmode=require`. Dùng dấu nháy đơn cho mật khẩu YAML; nếu mật khẩu có dấu
   `'`, viết thành `''`. Không URL-encode mật khẩu trong trường riêng này.
4. Đảm bảo database đã có schema tương ứng với entity hiện tại, rồi chạy:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   macOS/Linux dùng `./mvnw spring-boot:run`. Nếu đã cài Maven, có thể dùng
   `mvn spring-boot:run`. Trong IDE, đặt Working directory là `be-sba-project`.

Server mặc định ở `http://localhost:8080`. API nghiệp vụ dùng `/api/v1`, Auth dùng
`/api/auth`. Cấu hình thường xác thực bằng Bearer JWT; profile `invoice-demo` giữ
Basic Auth và CSRF cho bộ API Invoice. Khởi động được chưa có nghĩa các module đã
hoàn thiện phân quyền cho tất cả chức năng nghiệp vụ.

`application-local.yml` nằm **ngoài** `src/main/resources`, được Git bỏ qua và không
đóng gói vào JAR. Chỉ commit file `.example.yml` với mật khẩu mẫu. Không thêm mật
khẩu, token hoặc webhook secret thật vào file được Git theo dõi.

## Cấu hình dùng chung

Ứng dụng đọc các biến sau từ môi trường hoặc `application-local.yml`. Biến môi
trường có ưu tiên cao hơn file. Khi deploy, khai báo biến môi trường trên nền tảng
chạy ứng dụng; không cần file local. File `.env` **không được tự động đọc** bởi cấu
hình hiện tại.

| Biến | Mặc định / ý nghĩa |
| --- | --- |
| `DB_URL` | Bắt buộc; JDBC PostgreSQL Session Pooler, port 5432, có `sslmode=require` |
| `DB_USERNAME` | Bắt buộc; username pooler dạng `postgres.<project-ref>` |
| `DB_PASSWORD` | Bắt buộc; mật khẩu database, không phải Supabase API key |
| `JWT_SECRET` | Bắt buộc; secret ngẫu nhiên ít nhất 32 byte, chia sẻ riêng giữa các backend cần nhận cùng token |
| `DB_POOL_MAX_SIZE` | `5` kết nối tối đa cho mỗi backend đang chạy |
| `DB_POOL_MIN_IDLE` | `1` kết nối rảnh tối thiểu |
| `PORT` | `8080` |
| `APP_LOG_LEVEL` | `INFO` |

Pool 5 là giá trị khởi đầu cho nhóm, không phải giới hạn của Supabase. Cần tính
tổng kết nối từ tất cả máy dev và môi trường demo theo hạn mức project thực tế.

- `ddl-auto: validate`: kiểm tra mapping, không tự tạo/sửa/xóa bảng dùng chung.
  Database trống hoặc lệch entity sẽ khiến ứng dụng dừng khi khởi động. Repo hiện
  có Flyway V1–V5 cho entity hiện tại. Profile `local-dev` tự migrate localhost khi
  khởi động; mặc định không tự migrate Supabase. Xem tài liệu migration trước khi chạy.
- Không dùng `create`/`create-drop` trên Supabase chung. `update` chỉ nên dùng với
  database phát triển riêng có thể bỏ đi, không thay thế migration dùng chung.
- `spring.sql.init.mode: never`: không tự chạy `schema.sql`/`data.sql` lúc mở app.
- `open-in-view: false`: truy xuất quan hệ lazy và chuyển DTO trong transaction
  của service. Không để controller phụ thuộc session JPA còn mở.
- JDBC dùng UTC. Cấu hình này không thay múi giờ JVM, `LocalDateTime.now()` hay
  quy tắc ngày đến hạn; khi làm Invoice/Payment cần thống nhất rõ timestamp và
  múi giờ nghiệp vụ (ví dụ `Asia/Ho_Chi_Minh`).
- Không in SQL mặc định; phản hồi lỗi mặc định không chứa stack trace. Các thuộc
  tính `spring.web.error` không thay đổi response tự tạo trong `GlobalExceptionHandler`;
  handler hiện tại vẫn nối `ex.getMessage()` vào lỗi 500, cần xử lý ở task lỗi API.

Không cần Supabase SDK/API key để Spring Data JPA kết nối PostgreSQL qua JDBC.
JWT/CORS và OpenAPI đã có cấu hình. Storage và thông số nhà cung cấp đối soát sẽ
được bổ sung cùng module sử dụng chúng; chỉ thêm key YAML không tự triển khai chức năng.

## Kiểm tra

```powershell
.\mvnw.cmd clean test
```

Test `contextLoads` dùng profile `test` và H2 trong bộ nhớ; không import file local
hay dùng credential Supabase. H2 chỉ là dependency test và không được đóng gói
vào ứng dụng. Test này kiểm tra Spring context và mapping hiện có, không xác nhận
đường truyền/schema Supabase hoặc hành vi khóa, transaction và SQL đặc thù
PostgreSQL. Invoice/Payment cần thêm integration test PostgreSQL khi triển khai.

## Student 5 — Phase 1

| Task | Mã nguồn | Trách nhiệm |
| --- | --- | --- |
| BE-05.1 | P1-85–89 | Lập hóa đơn/dòng hóa đơn, chống trùng kỳ, lưu giá tại lúc phát hành |
| BE-05.2 | P1-90–93 | Công nợ từng Tenant, trả hộ, trả từng phần, trạng thái hóa đơn |
| BE-05.3 | P1-99–102 | Nhận giao dịch thực tế, xác thực webhook, đối chiếu và chống lặp |
| BE-05.4 | P1-103–106 | Ghi Payment, cập nhật nợ/cọc, lịch sử và ngoại lệ có audit |

Nhận cấu hình tài khoản/QR từ Student 1; bàn giao trạng thái tiền cho Student 2;
xác nhận cọc cho Student 3; lấy phí đã chốt và phối hợp notification với Student 4;
lấy dữ liệu cư trú và phí sửa chữa đã duyệt từ Student 6. Không tự tính lại phí
điện nước, không coi sinh QR là đã nhận tiền, không tự chốt chính sách dư/thiếu.
Phase 2/3 chưa triển khai.

Tham khảo: [Supabase với Spring Boot](https://supabase.com/docs/guides/getting-started/quickstarts/spring-boot),
[cấu hình bên ngoài Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html),
[quản lý khởi tạo database](https://docs.spring.io/spring-boot/how-to/data-initialization.html).
