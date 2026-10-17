# TroPro Admin — chạy và demo

## Khởi động và provider

Trong `fe-sba-project`:

```powershell
npm.cmd install
npm.cmd run dev -- --host 127.0.0.1 --port 5173
```

`.env.local` cho full demo:

```dotenv
VITE_API_MODE=mock
```

Mở `http://127.0.0.1:5173/login`, nút điền Admin demo, đăng nhập rồi `/admin/dashboard`. Những tài khoản dưới đây hoàn toàn hư cấu, chỉ dùng mock. Không dùng giấy tờ hoặc chuyển tiền thật.

| Email | Password demo | Vai trò / ngữ cảnh |
| --- | --- | --- |
| `admin@tropro.demo` | `TroPro@123` | Nguyễn Thảo Vy, USER + ADMIN; main demo reset |
| `admin-reviewer@tropro.demo` | `TroPro@123` | Trần Đức Khang, USER + ADMIN; reviewer cạnh tranh |
| `applicant@tropro.demo` | `TroPro@123` | Lâm Ngọc Mai, USER; có hồ sơ PENDING mẫu |
| `an@tropro.demo` | `TroPro@123` | Nguyễn Minh An, USER; thực hiện Owner onboarding |
| `owner@tropro.demo` | `TroPro@123` | Phạm Thanh Hà, USER + OWNER, APPROVED |
| `manager@tropro.demo` | `TroPro@123` | Nguyễn Quang Minh, USER + MANAGER, Branch đã phân công |
| `tenant@tropro.demo` | `TroPro@123` | Trần Gia Linh, USER + TENANT, tenancy demo |

`VITE_API_MODE=hybrid`: Auth/Notifications và **adminUsers** đi BE thật; Admin còn thiếu dùng mock. Real account không được tự cấp ADMIN vào mock; missing-domain mock sẽ trả403 do chưa có trusted bridge. Full Admin review demo dùng mode mock. `VITE_API_MODE=real` gửi endpoint thật, phần chưa có BE báo lỗi; không fallback. Có thể override `VITE_ADMIN_USERS_PROVIDER=real`/`VITE_ADMIN_PROVIDER=real`, nhưng cần auth BE và API thực sự tương ứng. Khởi động lại Vite sau đổi env.

BE User API hiện chỉ page/size; search/status/role/date global bị ẩn khi real. User roles/scope/eligibility không có trong UserResponse được hiển thị thiếu nguồn. Real reactivation chưa mở vì thiếu eligibility; disable real **không bảo đảm JWT cũ bị thu hồi**, xem [contract](admin-api-contract.md).

## Demo Owner verification

1. Login main Admin, chọn Xác minh chủ trọ. Hồ sơ Lâm Ngọc Mai đang Chờ xét duyệt. Mở chi tiết, xem identity/metadata và “Xem tài liệu được phép”. Đây là **ảnh SVG mẫu vô danh**, không phải giấy tờ applicant. Preview hết hạn60s; Escape đóng và trả focus về nút mở.
2. Phê duyệt, nhập lý do ít nhất10 ký tự, xác nhận. Kết quả hiển thị sau response; có reviewer/time/reason, audit link, notification và trusted OWNER provisioning. Không có nút grant role tùy ý.
3. Đăng xuất qua profile, login `applicant@tropro.demo` **trong cùng tab**. Mở `/owner/verification`, thấy Approved và đủ điều kiện; vào Owner workspace. Tạo Branch qua flow Owner thông thường.
4. Đổi về Admin cùng tab: Owner directory/Branch directory/dashboard nhận dữ liệu vừa tạo; Branch detail chỉ đọc, không có sửa policy/giá/invoice/payment.

Luồng tự nộp: login `an@tropro.demo`, `/owner/onboarding`, dùng ảnh PNG/JPEG/WebP demo nhỏ, kiểm tra/gửi; User vẫn USER khi PENDING. Đổi Admin duyệt rồi đổi User xem kết quả. Có thể chọn Từ chối thay vì duyệt; lý do bắt buộc được trả Owner view; người nộp sửa/resubmit thành hồ sơ mới. Không self-review hoặc tự bật OWNER trong Zustand/DevTools.

Review cạnh tranh dùng tests E2E/HTTP trên cùng engine: Admin mở dialog với version cũ; reviewer khác quyết định trước; xác nhận cũ trả409 và không tạo quyết định/notification thứ hai. Hai browser tabs không phải shared server, không dùng để chứng minh locking production.

## Demo lời mời User

1. Admin → Người dùng → Mời người dùng.
2. Nhập họ tên và email hoặc phone E.164, ví dụ `+84912345678`; DOB/gender optional.
3. Submit, xem detail **Đã mời (INVITED)**. Không có default password, không có roles dropdown hay thông báo đã gửi email/SMS. Duplicate email/phone hiện lỗi API; thiếu cả hai liên hệ bị form chặn.
4. INVITED không login hoặc force activate. Full invite acceptance/credential setup/delivery của Admin User invitation chưa có BE/mock flow; không dùng reset mật khẩu để giả lập ACTIVE. Registration+verify-email User thông thường là workflow khác và vẫn được giữ.

## Demo disable / reactivate

1. Admin tìm `manager@tropro.demo`, mở detail, Ngừng kích hoạt → optional note → xác nhận. Trạng thái chuyển DISABLED; không xóa quan hệ OwnerManager/Branch/tenancy.
2. Mock target sessions và refresh bị thu hồi. Đăng nhập Manager bị từ chối; direct request với token cũ trả401. Các User/Tenant/Manager/Owner protected scopes đều kiểm tra canonical account status.
3. Main Admin kích hoạt lại Manager demo có credential/contact verified. Manager login mới được phép; token cũ vẫn vô hiệu. Không dùng toggle cho INVITED/PENDING_VERIFICATION/LOCKED.
4. Mở own Admin detail: self-disable bị khóa. API cũng chặn; last effective Admin có guard riêng trong mock. Không có DELETE User UI.

## Shared cross-role workflow

Sau USER→ADMIN→OWNER, Owner có thể tạo Branch/policy, mời một verified User làm Manager; người nhận tự accept `/manager-invitations`, sau đó Owner phân công Branch. Manager vận hành đúng Branch; User booking/hold/contract và Tenant payment/service/maintenance tiếp tục dùng nguồn shared hiện có. Admin chỉ quan sát directory/account/verification/activity.

Hướng dẫn thao tác nghiệp vụ dài được tái sử dụng: [Owner](owner-demo-guide.md), [Manager](manager-demo-guide.md), [Tenant](tenant-demo-guide.md). Integration test S15 trong `src/mocks/admin-engine.test.ts` kiểm chứng toàn bộ chain tới issued invoice, sandbox payment và maintenance. Sandbox không phải actual receipts; không tự invent financial rules còn OPEN.

Role dropdown “Không gian làm việc” chỉ liệt kê roles đã có và thay active workspace, không grant quyền. Đổi account bằng profile → logout → login **cùng tab** để giữ shared domain; query cache/session cũ được xóa. Truy cập `/admin/*` bằng role khác nhận từ chối, kể cả direct API.

## Audit và trạng thái lỗi

- Activity lọc sự kiện/thời gian/search, mở audit detail, copy ID/link tài nguyên. Chỉ whitelisted fields, không raw JSON, không edit/delete. Owner Manager assignment tạo safe event một lần.
- Notifications dùng inbox chung, đúng Admin deep links, read/read-all; applicant nhận decision riêng. Không đẩy private document vào inbox.
- Thêm `?demo=loading`, `?demo=empty`, `?demo=error` vào list route mock để xem loading/empty/error/retry. Những flags không biến API real thành dữ liệu demo.

## Reset

Trong DEV, main `admin@tropro.demo` → dashboard → “Công cụ dữ liệu demo” → “Khôi phục dữ liệu demo” → confirm. Công cụ này reset Branch/Room/Booking/Contract/Tenant/Owner/Admin event domain về fixtures, khôi phục mainAdmin/reviewer/applicant defaults và applicant PENDING. Giữ các identity/credentials đã đăng ký ngoài3 seed này, trạng thái/roles của chúng, session còn hợp lệ; không phục hồi token bị thu hồi. Metadata mock createdAt được ghi lại; không phải timestamp DB thật.

Để có **fixture sạch hoàn toàn**, chỉ với demo mock trên localhost, mở DevTools Console và xóa đúng4 keys của ứng dụng rồi reload:

```js
for (const key of ['tropro-demo-v2', 'tropro-inventory-v1', 'tropro-manager-v1', 'tropro-tenant-v1']) {
  sessionStorage.removeItem(key)
}
location.reload()
```

Hoặc mở một browser session mới không kế thừa storage. Không dùng `sessionStorage.clear()` nếu origin còn dữ liệu khác cần giữ. Không có API reset production và không động đến BE database.

## Kiểm tra

```powershell
npm.cmd run typecheck
npm.cmd run lint
npm.cmd test
npm.cmd run build
npm.cmd run test:e2e -- tests/e2e/admin.spec.ts --workers=3
```

Playwright dùng Edge đã cài. Kết quả thực tế và ảnh desktop/mobile/tablet: [Implementation status](admin-implementation-status.md), [screenshots](admin-screenshots/). Các giới hạn BE/private-document/JWT được ghi trong [API contract](admin-api-contract.md).
