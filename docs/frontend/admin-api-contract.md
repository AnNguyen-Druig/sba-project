# TroPro Admin — hợp đồng API và bàn giao BE

Ngày 16/10/2026, branch `feature/fe-rebuild`. Phạm vi A01–A13, quyết định D41–D50. BE/schema được đọc để đối chiếu, không chỉnh sửa. Các endpoint `/api/admin/*` dưới đây là **contract mock FE**, chưa phải API BE đã triển khai.

## Quy ước và nguồn thực tế

- Tái sử dụng Auth, HTTP client, Zustand session, TanStack Query và MSW hiện có. Mọi request cần actor được xác thực; API quản trị cần canonical account `ACTIVE` + role `ADMIN`. Role chọn trên workspace không cấp quyền.
- Envelope hiện có: `{status, code, message, data}`. Đọc qua Zod, lỗi API thật hiển thị trực tiếp, không fallback mock.
- Query page API bắt đầu từ 0, size 1–100; URL giao diện bắt đầu từ 1. `/api/users` giữ Spring Page `number` từ 0; adapter chuyển thành page UI từ 1. Các danh sách mock Admin khác trả `number` từ 1 theo convention FE hiện có.
- `AdminUser`: `id, fullName, email nullable, phone nullable, status, createdAt`; mock bổ sung optional `roles, version, canActivate, contactVerified, ownerVerificationStatus, ownerReviewedAt, scope{ownedBranches, assignedBranches, tenancies}`. Real không tự điền các trường BE chưa trả.
- `AdminReview`: `{user: AdminUser, verification, version, reviewerName nullable, history: AdminAudit[]}`. `verification` dùng DTO Owner hiện có: status, thông tin người nộp, xác minh liên hệ, thời gian nộp/xét duyệt, reviewedBy, reason, metadata tài liệu, eligible.
- `AdminAudit`: `id, actorUserId nullable, actorName, tableName, entityId, action, reason nullable, requestKey nullable, occurredAt, summary, detail`. `detail` whitelist duy nhất `previousStatus, status, verificationStatus, provisionedOwner, branchName`. Không trả JSONB thô, password/hash/token, bí mật email, giấy tờ, QR, chứng từ thuê hay ảnh Base64.

Nguồn BE: `be-sba-project/src/main/java/com/sba/project/modules/identity/controller/UserController.java`, `service/impl/UserServiceImpl.java`, `dto/request/UserCreateRequest.java`, `dto/response/UserResponse.java`, `dto/response/AuthResponse.java`; bảo mật `common/security/JwtAuthenticationFilter.java`; audit `common/audit/entity/AuditLog.java`.

## API User đang có trên BE

Class `UserController` kiểm tra `hasRole('ADMIN')`. FE dùng feature provider `adminUsers`.

| Tính năng | Method / endpoint | Request | Response / hành vi | Khoảng trống BE |
| --- | --- | --- | --- | --- |
| A02 danh sách | GET `/api/users?page=0&size=20` | Chỉ page/size được BE hỗ trợ | Spring Page<UserResponse>; FE không lọc riêng trang hiện tại | Chưa global search/status/role/date/sort; chưa roles/verification/scope |
| A03 chi tiết | GET `/api/users/:id` | UUID | UserResponse; thiếu dữ liệu hiển thị “API chưa cung cấp” | Chưa contact eligibility, liên kết Branch/tenancy, version/activity |
| A04 tạo/mời | POST `/api/users` | `fullName`, `email` hoặc `phone`; optional `dateOfBirth`, `gender`; không password/roles | UserResponse status INVITED; email/phone nullable | Chưa workflow gửi/nhận lời mời, thiết lập credential và xác minh hoàn chỉnh |
| A05 trạng thái | PATCH `/api/users/:id/active-status` | **Chỉ `{active:boolean}`** với real | UserResponse theo kết quả máy chủ | Cần điều kiện lifecycle, self/lastAdmin, concurrency, revocation, audit/notification |

Validation real: fullName không trống, max255; email hợp lệ max320; phone E.164 `+` và 8–15 chữ số, chữ số đầu khác 0; ít nhất một kênh liên hệ; DOB optional; gender max30. FE chủ động chặt hơn: tên min2, DOB không tương lai, gender chọn Nam/Nữ/Khác; không thay đổi BE DTO. Email được chuẩn hóa chữ thường trong mock; duplicate email không phân biệt hoa thường và duplicate phone bị từ chối. Optional không nhập được adapter gửi `null`. BE `UserResponse` **chỉ có** id/email/phone/fullName/status/createdAt.

**Giới hạn quan trọng của API trạng thái thật:** `UserServiceImpl` yêu cầu passwordHash khi active=true, chỉ kiểm tra emailVerifiedAt khi trạng thái hiện tại là PENDING_VERIFICATION rồi đặt ACTIVE. active=false đặt DISABLED. Chưa có self-disable guard, last effective Admin guard, version hoặc thu hồi session tại thao tác này. `JwtAuthenticationFilter` kiểm tra chữ ký/invalidated token, không đọc current User.status; **JWT đang sống không được bảo đảm mất quyền chỉ vì PATCH DISABLED**. FE không thể vá bảo mật máy chủ bằng việc ẩn nút.

FE real chặn tự disable ở UI. Nút kích hoạt lại bị khóa khi DTO không cung cấp `canActivate`; cần BE trả eligibility có căn cứ thay vì FE đoán credential/xác minh. Không expose DELETE dù BE có endpoint. Reason/version chỉ gửi trong mock; không thêm trường không hỗ trợ vào real PATCH.

## API Admin cần BE triển khai

Tất cả cần trusted ADMIN authorization, validation phía server, projection an toàn và không trao quyền tài chính mặc định. GET không tạo audit giả. POST/PATCH ghi kết quả sau transaction thành công.

| Phạm vi | Method / endpoint mock | Request / filter | Response | Validation, concurrency, audit, notification |
| --- | --- | --- | --- | --- |
| Layout | GET `/api/admin/entitlements` | Actor session | `{user: safe User, permissions:[USERS,OWNER_REVIEW,DIRECTORIES,AUDIT]}` | Mock kiểm tra canonical status/roles; không đọc role header để cấp ADMIN. FE hiện chỉ gọi trong fully mock auth/Admin |
| A01 | GET `/api/admin/dashboard` | Không | totalUsers, account-status counts, pendingOwners, eligible Owners, Branch counts, recent safe events, issues | Tổng hợp từ cùng nguồn User/Owner/Branch; không revenue/debt hay tenant financial |
| A06 | GET `/api/admin/roles` | Không | Năm role `{id,name,description,scope,requirement,userCount}` | Read-only USER/TENANT/MANAGER/OWNER/ADMIN; GUEST không stored role; không grant/revoke API |
| A07 | GET `/api/admin/owner-verifications` | q, status, from/to theo ngày nộp, page/size | Page<AdminReview> | Mặc định UI PENDING; date hợp lệ và from≤to; mọi tìm kiếm mock lọc toàn bộ tập trước phân trang |
| A08 | GET `/api/admin/owner-verifications/:userId` | UUID | AdminReview | Xem đúng canonical applicant; metadata tài liệu, latest reviewer/time/reason và lịch sử audit; đọc được disabled applicant nhưng không duyệt |
| A08 tài liệu | GET `/api/admin/owner-verifications/:userId/document` | Actor + applicant | Mock `{mime:'image/svg+xml',content,expiresAt}` | Chỉ mẫu SVG cố định vô danh, TTL60s. BE cần private resource authorization + signed URL/stream ngắn hạn, Cache-Control phù hợp và safe MIME; không dùng mock SVG contract làm API giấy tờ production |
| A08 quyết định | POST `/api/admin/owner-verifications/:userId/decision` | `{decision:APPROVED\|REJECTED,reason,version}` | AdminReview cập nhật | ACTIVE applicant, PENDING, có doc/contact verified, không tự duyệt, reason 10–2000 ký tự; version khớp; stale/duplicate409. Một transaction metadata + trusted entitlement transition + audit + inbox |
| A09 | GET `/api/admin/owners` | q, verification status, page/size | Page<AdminUser> cho canonical OWNER | Account status, verification status, role và eligibility là các khái niệm riêng; không xem dữ liệu thuê riêng tư. ownerReviewedAt nếu có; không suy diễn từ ngày account |
| A10 | GET `/api/admin/branches` | q tên/code/address, status, province, ownerId, page/size | Page<AdminBranch> | Chỉ id/code/name/province/address/status/ownerId/name/managerId/name/createdAt. Mock chưa có nguồn createdAt nên trả null. Không policy/giá/QR/payment/tenant data; không write route |
| A11 | GET `/api/admin/activity` | q, type, userId (entity), from/to, page/size | Page<AdminAudit> | Date hợp lệ; safe summary; nhập Owner assignment events một lần; không dựng login/SIEM history giả |
| A12 | GET `/api/admin/activity/:eventId` | UUID | AdminAudit | Không edit/delete API/UI; whitelist hai lần ở domain/schema; UI hiển thị trường có tên, không dump JSON |
| A13 | Auth/Profile/Notifications hiện có | Service chung, chủ thể chính mình | DTO hiện có | Không tạo API/bộ lưu Admin thứ hai. ADMIN_VERIFICATION → hồ sơ; ADMIN_USER → tài khoản; ACCOUNT_STATUS → profile |
| DEV công cụ | POST `/api/admin/demo/reset` | `{}` | null | Chỉ DEV + main demoAdmin; không production route. Reset domain chung, khôi phục 3 seed Admin/applicant; không phục hồi token bị thu hồi |

### Các transition mock đã thực hiện

- Create chỉ INVITED + USER, không password mặc định, không login, không tự chuyển ACTIVE. BE cần bổ sung invitation acceptance/setup; không mô phỏng gửi thư thành công khi chưa gửi.
- Disable chỉ ACTIVE→DISABLED; cấm self-disable và mất Admin hiệu lực cuối. Activation chỉ DISABLED→ACTIVE khi credentials + verified contact có sẵn. INVITED/PENDING_VERIFICATION/LOCKED không dùng Boolean toggle để vượt quy trình. Có optimistic numeric version trong mock UI; API Boolean tương thích vẫn chấp nhận khi version không gửi, BE cần chuẩn hóa version bắt buộc cho quản trị production.
- Disable xóa toàn bộ target access sessions/refresh principal trong mock; mọi domain kiểm tra canonical ACTIVE. Reactivation cần login mới, token cũ không sống lại; giữ OwnerManager/Branch/tenancy và lịch sử nghiệp vụ. Ghi USER_DISABLED/USER_ACTIVATED, thông báo account và Admin đang active.
- Owner submission qua service Owner đưa PENDING, giữ USER. Approval là **quy trình danh tính cố định phía trusted mock API**: APPROVED và provision OWNER trong cùng transaction, ghi `provisionedOwner:true`; không nhận roles từ request. Owner eligibility vẫn yêu cầu ACTIVE + APPROVED + canonical OWNER. Engine Owner riêng vẫn phân biệt approval không provision và provision rõ ràng; test chứng minh APPROVED đơn lẻ không cấp quyền.
- Rejection giữ USER, ghi reviewer/time/reason, thông báo applicant; có thể resubmit tài liệu mới. Fingerprint version gắn submission/document/status/reviewedAt; dialog giữ version tại lúc mở, không âm thầm thay bằng version poll mới. Hai review cạnh tranh chỉ một success, còn lại409, một audit và một notification applicant.
- Pending submission thông báo mỗi active Admin một lần theo user+documentResource. Decision ghi OWNER_APPROVED/OWNER_REJECTED; history là safe immutable audit trong phiên. Không công bố doc URL trong notification/audit.

### Bàn giao production ưu tiên

1. Thêm server lifecycle guards và revocation thực sự trên mọi protected request; thu hồi refresh/access phù hợp, không tin status từ FE/JWT cũ. Bảo vệ self/last effective Admin trong transaction đồng thời.
2. User global search/filter/page + DTO optional eligibility/roles/contact/scope/version; invitation completion và delivery status thật.
3. Verification list/detail + private documents; approve/reject với expected version, reviewer, atomic audit/notification và trusted Owner provisioning policy. Chặn self-review, unauthorized actor và tài khoản không hợp lệ ở server.
4. Role read summaries, Owner/Branch administrative projections và audit endpoints có whitelist. AuditLog entity tồn tại không đồng nghĩa có REST API; không expose JSONB tùy ý.
5. Multi-role identity contract đáng tin: AuthResponse BE hiện trả một `role` string. Không dùng client/hybrid header để tự provision các quyền khác. Chốt server authorization cho ADMIN + OWNER/Manager/Tenant theo tài nguyên.

## Provider và độ trung thực mock

`VITE_API_MODE=mock` chạy tất cả qua `/__mock`, đầy đủ demo. `hybrid` dùng real Auth/Notifications/**adminUsers**; các domain Admin chưa có vẫn chọn mock. Vì chưa có trusted real→mock ADMIN bridge, request Admin mock của identity thật mặc định bị từ chối; không chép role ADMIN từ header. User management thật dùng authenticated BE ADMIN và bốn API hiện có. `real` gửi Admin domain endpoint thật, endpoint thiếu báo lỗi. Feature overrides: `VITE_ADMIN_USERS_PROVIDER`, `VITE_ADMIN_PROVIDER`; override không tạo quyền hoặc tạo endpoint BE.

State Admin là phần `ManagerState.admin`, dùng cùng User database, Owner verification, OwnerManager/Branch, Tenant và inventory; transaction/save rollback chung. SessionStorage chỉ phục vụ demo trong một tab; cạnh tranh tests là trên cùng engine, không chứng minh tính nhất quán đa server/tab. Metadata createdAt cho seed/mock account cũ là thời điểm engine ghi nhận, không phải lịch sử DB thật. Phone-only canonical mock User dùng email chuỗi rỗng do Auth DTO cũ; projection Admin trả null, không tạo identity thứ hai. Raw upload Owner không được giữ; Admin chỉ preview synthetic sample, không phải giấy tờ người nộp thực tế. Không dùng dữ liệu định danh hoặc tiền thật.

Implementation: `fe-sba-project/src/interfaces/admin.ts`, `services/admin.ts`, `mocks/admin-engine.ts`, `mocks/admin-handler.ts`. Kiểm thử adapter real dùng HTTP stubs; chưa xác minh live BE/DB/email/private storage.
