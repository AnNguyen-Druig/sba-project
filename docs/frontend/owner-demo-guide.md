# TroPro Owner — chạy và demo

## Chạy frontend

Tại `fe-sba-project`, dùng dependencies đã có:

```powershell
npm.cmd install
npm.cmd run dev -- --host 127.0.0.1 --port 5173
```

`.env.local` cấu hình demo đầy đủ:

```dotenv
VITE_API_MODE=mock
```

Mở `http://127.0.0.1:5173/login`, có nút điền Owner demo. Không chuyển tiền thật hoặc dùng giấy tờ thật. Seed data dùng danh tính và receipt giả lập.

Hybrid: `VITE_API_MODE=hybrid` dùng auth/notifications thật, Owner mock; chưa có trusted Owner provisioning bridge cho account thật. Identity từ auth real vào mock chỉ có USER mặc định; không dùng header để tự cấp quyền. Demo Owner trọn vẹn dùng auth mock. `VITE_OWNER_PROVIDER=real` chỉ dùng khi BE đã triển khai đúng contract; lỗi không tự thay bằng dữ liệu demo.

## Tài khoản

Tất cả mật khẩu seed dưới đây: `TroPro@123`.

| Email | Ngữ cảnh | Phạm vi |
| --- | --- | --- |
| `owner@tropro.demo` | Phạm Thanh Hà, USER + OWNER, mock verification APPROVED | Thủ Đức (ID suffix 10) và Làng Đại học (12) |
| `owner-other@tropro.demo` | Lê Hoàng Phúc, USER + OWNER, APPROVED | Bình Thạnh (11) |
| `manager@tropro.demo` | Nguyễn Quang Minh, USER + MANAGER | Thủ Đức và Làng Đại học |
| `manager-other@tropro.demo` | Vũ Hoàng Anh, USER + MANAGER | Bình Thạnh |
| `an@tropro.demo` | Nguyễn Minh An, USER | Onboarding/invitation recipient, User bookings |
| `tenant@tropro.demo` | Trần Gia Linh, USER + TENANT | Các nghĩa vụ cá nhân đã có |
| `roommate@tropro.demo` | Lê Phương Vy, USER + TENANT | SLOT tenancy trong Làng Đại học |

Canonical fixture UUID: `10000000-0000-4000-8000-` + số suffix đủ 12 chữ số. Ví dụ Branch 10: `10000000-0000-4000-8000-000000000010`.

Đổi tài khoản bằng `/profile` → Đăng xuất → Đăng nhập trong **cùng tab**. State nghiệp vụ dùng chung được giữ, cache phiên cũ được xóa. Tab/browser session khác là phiên demo khác.

## Luồng demo

### 1. Tổng quan và quyền sở hữu

1. Đăng nhập Owner Hà, mở `/owner/dashboard`; mặc định tất cả hai Branch sở hữu.
2. Chọn Làng Đại học: tổng tài chính/tài nguyên/approvals refresh theo Branch.
3. Mở `/owner/finance?branchId=10000000-0000-4000-8000-000000000011`: không có quyền; không render dữ liệu Bình Thạnh.
4. Đổi sang Owner Phúc: selector chỉ có Bình Thạnh, không thấy các Manager/payment thuộc Hà.

Chỉ số billed/debt theo billing month; actual receipts theo verifiedAt. Tiền sandbox, Hold và prepaid lifetime được tách/ghi nhãn. Seed actual receipts có thể bằng 0; không dựng dòng tiền giả từ invoice snapshots.

### 2. Tạo Branch

1. Owner Hà → Chi nhánh → Tạo chi nhánh.
2. Nhập code duy nhất, name/address, public metadata; giữ status Nháp, không cần bịa tọa độ.
3. Kiểm tra thay đổi → Xác nhận lưu.
4. Branch Detail → Chính sách và lịch sử → Tạo phiên bản. Chọn effectiveFrom có timezone từ hiện tại; đọc bản preview rồi lưu.
5. Sửa Branch: chọn quản lý hợp lệ, trạng thái Hoạt động, xác nhận. Nếu policy còn ở tương lai, cần chờ hiệu lực hoặc tạo bản hiệu lực hợp lệ trước đó theo thứ tự phiên bản.
6. Mở Vận hành: dùng Manager modules để tạo loại phòng, services, phòng và slots; không có bản sao CRUD Owner.

Không thể ACTIVE thiếu Manager/policy. Không thể deactivate Branch seed còn contract/booking/nghĩa vụ; thông báo nêu cần xử lý trước. Nháp rỗng có thể xóa từ Detail, kèm xác nhận.

### 3. Mời Manager và phân công

1. Owner Hà → Quản lý → Mời quản lý, nhập `an@tropro.demo`, họ tên và lời nhắn. Proposed Branch chỉ là dự kiến.
2. Sau khi gửi: INVITED; An chưa có access. Demo không gửi email thật; lời mời đến inbox/profile recipient.
3. Đăng xuất Owner; đăng nhập An → `/manager-invitations` hoặc Hồ sơ → Lời mời quản lý → Xác nhận tham gia.
4. An được liên kết cùng canonical User ID và role MANAGER, chưa tự được phân công Branch.
5. Đăng nhập lại Hà → Phân công → chọn An cho Thủ Đức, kiểm tra, nhập lý do và xác nhận.
6. Đăng nhập Manager Minh: Thủ Đức đã bị thu hồi, chỉ còn Làng Đại học. Deep link cũ không truy cập được. An chỉ có Branch được Owner phân công.

Một Manager có thể được chọn cho nhiều Branch cùng Owner. Manager Anh thuộc Owner Phúc không xuất hiện trong eligible list Hà. Không đặt lại mật khẩu, chiếm identity hoặc khóa global User từ Owner.

### 4. Onboarding và trusted review

1. Đăng nhập An → `/owner/onboarding`; nhập tên/điện thoại và ảnh PNG/JPEG/WebP giả ≤1 MB.
2. Kiểm tra hồ sơ → Gửi xác minh → `/owner/verification`: PENDING.
3. User vẫn không truy cập Branch/Owner finance. Không có nút Owner tự duyệt.
4. Trusted ADMIN approve/reject/explicit entitlement được chứng minh qua `src/mocks/owner-engine.test.ts` (canonical ADMIN test fixture, direct engine call). Chạy:

```powershell
npm.cmd test -- --run src/mocks/owner-engine.test.ts
```

APPROVED không kèm explicit role grant vẫn chưa eligible. REJECTED cho sửa/gửi lại. Chưa có normal Admin interface hoặc production eKYC/API trong phạm vi này.

### 5. Chính sách mới và snapshot cũ

1. Owner → Branch → Chính sách, xem phiên bản hiện hành và history.
2. Tạo phiên bản tiếp theo với ngày hiệu lực sau bản cuối, thay Hold amount/collection/grace/appointment/cancellation khi hợp lệ; preview rồi lưu.
3. Existing Booking giữ nguyên policyId/version/Hold snapshot.
4. New Booking dùng policy đang hiệu lực; version tương lai chưa áp dụng sớm.

Không sửa version cũ, không thay các invoice hay lịch RENT đã chốt. Q1–Q5 vẫn OPEN; Owner policy UI không giải quyết bằng thuật toán thuê tự suy diễn.

### 6. QR → Tenant evidence → Manager verify → Owner receipts

1. Owner Hà → Branch Thủ Đức → QR nhận tiền, upload **QR giả lập**, preview và Lưu QR.
2. Đăng nhập Tenant → `/tenant/invoices/10000000-0000-4000-8000-000000005300?tenancyId=10000000-0000-4000-8000-000000005100`.
3. Panel QR thủ công hiển thị ảnh Branch và reference riêng `TP-MQR-<invoice UUID>`. Sandbox vẫn là lựa chọn mô phỏng riêng.
4. Gửi chứng cứ: số tiền ≤ available, payer, fake receipt ID, ảnh giả. Kết quả PENDING, allocated 0; dư nợ chưa giảm.
5. Đăng nhập Manager Minh, Branch Thủ Đức → Thanh toán → mở claim → Đối chiếu & xác minh. Nhập chính xác reference/amount/receipt ID sau khi kiểm tra chứng cứ giả và reason.
6. Sau trusted mock verification: VERIFIED, allocated đúng invoice, Tenant debt giảm; Owner Finance actual receipts tăng đúng số tiền. Xác minh lại hoặc dùng trùng provider transaction bị từ chối.

Có receipt seed `TP-QR-DEMO-01`, Branch Làng Đại học, payment suffix 7900, amount 50.000 VND, provider `BANK-DEMO-0001` để demo đối soát sẵn. Đây cũng là bank-like receipt **giả**, không có kết nối bank. Chỉ Owner/Manager đúng Branch và khác beneficiary mới xác minh.

### 7. Duyệt điều chỉnh

1. Manager → ISSUED Invoice → Yêu cầu điều chỉnh.
2. Nhập proposed total khác hiện tại, reason ≥10 ký tự và evidence khi cần; gửi.
3. Owner → Duyệt điều chỉnh → mở chi tiết, xem before/after, tác động dự kiến và nguồn Invoice.
4. Chấp thuận hoặc từ chối bằng reason; history và status cập nhật.
5. Execution vẫn “Chưa áp dụng”; invoice items/amount/debt/payment không bị thay đổi. Manager không có quyền phê duyệt Owner queue; stale/duplicate decisions bị chặn.

### 8. Operations, reports, activity, notifications

Từ Owner Branch detail hoặc selector → Vận hành. URL dùng `/manager/...?...&workspace=owner`; selector chỉ owned Branch, có đường về Owner Dashboard. Không đổi account hoặc giả thành MANAGER.

Reports là Phase 1: counts/Branch comparison/utilization, link về source records. Activity là safe summary và source link, không expose raw audit payload. Notifications/Profile dùng hệ thống chung; source routes tiếp tục kiểm tra quyền khi mở.

## Reset và trạng thái thử lỗi

- Manager demo → bảng điều khiển DEV demo → Khôi phục dữ liệu; hoặc Tenant demo reset có sẵn. Reset restores inventory/tenant/leasing/Manager/Owner cùng lúc, gồm policies/invitations/approval/QR; không reset riêng một universe Owner.
- Có thể kết thúc browser session để khởi tạo phiên mock mới. Không xóa toàn bộ storage của các dự án khác.
- Owner list thêm `?demo=empty`, `?demo=error`, `?demo=loading` để xem trạng thái; HTTP client có thể normalize message lỗi chung, không báo fake success.
- Raw identity document không được giữ trong persistent mock state; ảnh public/QR/receipt demo có thể dùng sessionStorage và bị giới hạn dung lượng. Lỗi quota rollback shared mutation và giữ feedback lỗi.

## Kiểm tra chất lượng

```powershell
npm.cmd run typecheck
npm.cmd run lint
npm.cmd test -- --reporter=dot
npm.cmd run build
npm.cmd run test:e2e -- --workers=3
```

Playwright dùng Edge đã có, desktop 1440×1000 và iPhone 13 viewport, giờ Việt Nam. Không cần cài browser mới. Kết quả thực tế và limitations được cập nhật trong [owner-implementation-status.md](owner-implementation-status.md).
