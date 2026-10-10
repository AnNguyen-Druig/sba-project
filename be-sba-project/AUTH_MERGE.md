# Tích hợp feature/jwt-login vào feat/invoice-draft

Đã merge commit `66bb99a` của `origin/feature/jwt-login`. Giữ Spring Boot 4.1.1,
Java 21, springdoc và các module Room, dịch vụ, Invoice đang có trong workspace.
Không chạy migration hay khởi động backend với database Supabase trong lần merge này.

## 1. Cấu hình trước khi chạy backend

Sử dụng `application.yml` và file riêng `application-local.yml` trong thư mục
`be-sba-project`. Không khôi phục `src/main/resources/application.properties`
của nhánh Auth: file đó có cấu hình datasource và `ddl-auto=update` sẽ ghi đè YAML.
Giữ `ddl-auto: validate` khi làm việc với database dùng chung.

Auth yêu cầu `JWT_SECRET` có ít nhất 32 byte. Tạo giá trị ngẫu nhiên trên PowerShell:

```powershell
$jwtBytes = New-Object byte[] 32
$jwtRng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$jwtRng.GetBytes($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
$jwtRng.Dispose()
```

Nếu chạy Maven từ terminal này, JVM nhận biến môi trường trên. Nếu chạy từ IntelliJ,
đặt `JWT_SECRET` trong Environment variables của Run Configuration hoặc thêm vào
file `application-local.yml` đã được Git ignore. Không commit giá trị thật. Các
backend cần chấp nhận cùng token phải dùng cùng secret được nhóm chia sẻ riêng.
Đổi secret sẽ làm token cũ không còn hợp lệ.

Các thiết lập tùy chọn:

| Biến | Mặc định |
| --- | --- |
| `JWT_EXPIRATION_MS` | `86400000` |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` |
| `CORS_ALLOWED_ORIGINS` | localhost/127.0.0.1, cổng 3000 và 5173 |

Không còn tự tạo tài khoản có mật khẩu `admin` mỗi lần chạy. Chỉ khi cần bootstrap
admin trên database đã đúng schema, bật `app.admin.bootstrap-enabled=true` và cung
cấp `ADMIN_EMAIL`, `ADMIN_PASSWORD`. Đây là thao tác ghi database, không phải migration.

## 2. API Auth và Invoice

- Auth: `POST /api/auth/register`, `/api/auth/login`, `/api/auth/refresh-token`,
  `/api/auth/logout`. Giữ JSON `success/message/data/timestamp` bằng `AuthApiResponse`.
- Invoice: giữ JSON `code/message/result` bằng `ApiResponse`; không đổi contract của
  các request Bruno hiện có.
- Cấu hình thường dùng Bearer JWT, không có form login/Basic Auth cho Invoice.
- Profile `invoice-demo` trong phần Invoice đang làm vẫn dùng danh tính UUID mẫu,
  HTTP Basic và CSRF riêng cho `/api/v1/invoices/**`, `/login`, `/logout`.
  Phải cung cấp hai biến mật khẩu demo như tài liệu Invoice, đồng thời có `JWT_SECRET`.
  Không bật profile demo trên backend dùng chung.
- Swagger `/swagger-ui/index.html`, `/v3/api-docs` và API tìm phòng public vẫn public.
  Các API khác yêu cầu xác thực; phân quyền nghiệp vụ của từng module vẫn do module xử lý.

**Chưa thể dùng tài khoản JWT mới để thao tác Invoice thực tế.** Auth hiện ánh xạ
`users.id` là `Long/BIGINT`, principal là email. Invoice yêu cầu account UUID để
đối chiếu `branches.owner_id` và `manager_branch_assignments.manager_id`. Token hợp
lệ không thay thế được quan hệ này; Invoice sẽ từ chối quyền nếu principal không
đúng UUID/quyền chi nhánh. Không ép email/Long thành UUID hoặc bỏ kiểm tra quyền để
chạy thử. Cần thống nhất mô hình User/Account và mapping dữ liệu trước khi tích hợp
JWT với Invoice trên database thật.

## 3. Frontend

```powershell
cd fe-sba-project
npm.cmd ci
npm.cmd run dev
```

Entry `main.tsx` sử dụng router Auth. Mở `/login` để đăng nhập; màn hình đăng nhập
thành công có nút mở `/dashboard`, sử dụng lại giao diện `App.tsx` đang có.
Đây vẫn là giao diện dashboard hiện tại, chưa phải tích hợp phân quyền theo role cho
tất cả màn hình. CSS Auth được giới hạn trong `.auth-page`, giữ theme Tailwind của dashboard.
Backend mới là nơi bắt buộc kiểm tra quyền; route frontend không thay thế phân quyền backend.

## 4. Trạng thái migration sau merge

**V1–V3 hiện chưa đủ cho toàn bộ entity trên nhánh này.** Ngoài phần Student 2
đã được ghi trong `DATABASE_MIGRATION.md`, Auth thêm `users`, `invalidate_tokens`
và mô hình danh tính chưa thống nhất
với `accounts` của schema Supabase cũ. Không dùng `ddl-auto:update` để tự bù phần thiếu.

Bước migration kế tiếp phải kiểm kê schema/data hiện tại, chốt User/Account và
UUID, viết migration bổ sung rồi thử trên bản restore có dữ liệu. Không sửa checksum
V1–V3 đã áp dụng, không baseline lại database đã có Flyway history. Các guard hiện
có vẫn phải được giữ nguyên để bảo vệ dữ liệu.

`git pull` chỉ cập nhật code. Với Supabase dùng chung, một người phụ trách migrate
một lần, những người khác pull code rồi refresh datasource. Với database local riêng,
mỗi người phải chạy bộ migration đã được kiểm thử trên database của mình.

## 5. Kiểm tra sau merge

```powershell
cd be-sba-project
mvn clean test
cd ../fe-sba-project
npm.cmd run build
npm.cmd run lint
```

Test backend dùng H2; test PostgreSQL chuyên biệt chỉ chạy khi có biến môi trường
được chỉ định trong bộ test migration. H2 đạt không chứng minh schema Supabase đã khớp.
Không commit các file log trong `target` hoặc thông tin kết nối/mật khẩu cá nhân.

Kết quả ngày 10/10/2026: `mvn clean test` có 99 test, 98 đạt, 1 test PostgreSQL
được bỏ qua do chưa cấu hình database kiểm thử; không có failure/error. Frontend
`npm.cmd run build` và `npm.cmd run lint` đều đạt. Build còn cảnh báo kích thước bundle
và `__dirname` trong cấu hình Vite; không chặn build.
