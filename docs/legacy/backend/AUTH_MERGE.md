> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../backend/README.md).

# Tích hợp feature/jwt-login vào feat/invoice-draft

> **Cập nhật sau khi thống nhất migration:** Auth đã chuyển sang `accounts.id UUID`;
> `UserResponse.id` là chuỗi UUID, JWT subject là account UUID. Các API login vẫn nhận email.
> V4/V5 bổ sung schema cho Student 2/Auth; làm theo [DATABASE_MIGRATION.md](DATABASE_MIGRATION.md).

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

**JWT đã dùng chung danh tính với Invoice.** Auth ánh xạ `accounts.id UUID`; đăng nhập
bằng email không phân biệt hoa thường, principal/JWT subject là UUID. Invoice kiểm
tra UUID với `branches.owner_id` hoặc `manager_branch_assignments.manager_id` đang
active. Account phải ACTIVE và không đang bị khóa; trạng thái được kiểm tra lại khi
dùng access token hoặc refresh. Vai trò lấy từ database, không chỉ tin claim của JWT.

Frontend/Bruno phải xem `data.account.id` là chuỗi UUID, không parse thành số.
Xóa session/token cũ và đăng nhập lại sau cutover: token có email subject bị từ chối.
Account cũ không có email được giữ nguyên nhưng không dùng để đăng nhập bằng email;
không tự gán email hay sửa password/status của account có sẵn trong migration.

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

**Dùng V1–V5 cho code hiện tại.** V4 bổ sung các bảng Student 2; V5 tạo
`invalidate_tokens`, index email không phân biệt hoa thường, dùng lại `accounts`.
Không tạo bảng `users` song song. Nếu database đã có `users`, migration dừng để
review mapping, không tự xóa hay gộp tài khoản. V1–V3 và các guard giữ nguyên.
Schema BIGINT cũ phải đúng snapshot và trống để V2 chạy; xem giới hạn dữ liệu trong
DATABASE_MIGRATION.md. Không dùng `ddl-auto:update` để bù schema thiếu.

`git pull` chỉ cập nhật code. Với Supabase dùng chung, một người phụ trách migrate
một lần, những người khác pull code rồi refresh datasource. Với database local riêng,
profile `local-dev` tự chạy migration khi backend khởi động, sau lần cấu hình đầu tiên.

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

Kết quả ngay sau merge, trước tích hợp UUID: `mvn clean test` có 99 test, 98 đạt, 1 test PostgreSQL
được bỏ qua do chưa cấu hình database kiểm thử; không có failure/error. Frontend
`npm.cmd run build` và `npm.cmd run lint` đều đạt. Build còn cảnh báo kích thước bundle
và `__dirname` trong cấu hình Vite; không chặn build.
Kết quả mới sau migration UUID: [biên bản V5](database/migration-v5-validation.md).
