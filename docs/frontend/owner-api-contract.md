# TroPro Owner — API contract và bản đồ tích hợp

Ngày kiểm tra: 16/10/2026. Phạm vi O01–O20, frontend `fe-sba-project`; không sửa Backend hoặc database.

## 1. Kết quả kiểm tra Backend

Đã đối chiếu `docs/all-feature.txt`, `docs/be_tasks.md`, ERD `All_Diagrams.drawio`, `docs/backend/contract-policies.md`, `docs/database/rental/{TABLE_CATALOG,REFACTOR_REPORT}.md`, các hợp đồng FE trước và mã Java thực tế.

| Nguồn hiện có | Ý nghĩa và giới hạn |
| --- | --- |
| `identity/controller/AuthController.java` | Auth dùng chung `/api/auth`, register không cấp quyền Owner |
| `identity/controller/UserController.java` | `/api/users` dành cho ADMIN; không dùng thay API quản lý Manager của Owner |
| `identity/entity/User.java` | Có trạng thái xác minh Owner, mã tài liệu, thông tin xét duyệt; chưa có controller workflow Owner đầy đủ |
| `identity/entity/OwnerManager.java` | Owner–Manager qua USERS; một Manager thuộc một Owner hiện tại |
| `property/entity/Branch.java` | ownerId, managerId, code, vị trí, ảnh, public contact, QR, Zalo, trạng thái, version; chưa có Owner Branch controller phù hợp |
| `property/entity/BranchPolicy.java` | Phiên bản, thời điểm hiệu lực, thu tiền/gia hạn, Booking Hold và lịch xem; entity không đồng nghĩa có API |
| Property, utilities, maintenance controllers | Có một số API chuyên biệt; chưa đáp ứng DTO/phân quyền/quy trình của toàn bộ workspace Owner/Manager |
| NotificationController | Dùng hạ tầng thông báo đã có; không tạo inbox Owner riêng |
| Rental schema D05/D06/D26 | ACTIVE Branch cần Manager phù hợp; Owner duyệt điều chỉnh sau phát hành; workflow điều chỉnh HTTP chưa hoàn chỉnh |

Toàn bộ domain API Owner dưới đây là **hợp đồng đề xuất, được thực thi bởi MSW**, chưa được xác nhận hoạt động trên BE. Real auth/notifications tiếp tục dùng adapter sẵn có khi cấu hình phù hợp. Không gọi API ADMIN bằng tài khoản Owner.

## 2. Provider và envelope

`services/config.ts`: thêm feature `owner` vào provider hiện có. `mock` mặc định; `hybrid` dùng auth/notifications thật và Owner mock; `VITE_OWNER_PROVIDER=real|mock` ghi đè rõ ràng. `real` gọi endpoint cấu hình và hiển thị lỗi nếu BE chưa có; **không fallback mock khi HTTP lỗi**.

Mock prefix: `/__mock/api/owner`. Real/proposed prefix: `${VITE_API_BASE_URL}/api/owner`.

Envelope: `{status, code, message, data}`. Đọc dữ liệu bằng Zod trước khi đưa vào UI. HTTP 401: thiếu/hết phiên; 403: sai entitlement/scope; 404: hồ sơ không thuộc danh sách được phép hoặc endpoint thiếu; 409: stale/duplicate/transition không hợp lệ; 422: input không hợp lệ. Sử dụng chung HTTP client, token trong memory và refresh flow hiện có.

List: `page` zero-based, `size` 1–100; response `{content,number,size,totalElements,totalPages}`, `number` one-based theo convention FE. `q`, `status`, `branchId` khi phù hợp. Owner Branch selector tải hết các trang, không chỉ trang đầu.

## 3. Endpoint đề xuất

| Method | Path sau `/api/owner` | DTO / chức năng | Quyền |
| --- | --- | --- | --- |
| GET | `/verification` | OwnerVerification hiện tại, document metadata, eligible | User ACTIVE, chỉ chính mình |
| POST | `/verification` | fullName, phone, document `{name,mime,size,url}` → PENDING | User ACTIVE; chỉ NOT_SUBMITTED/REJECTED |
| GET | `/my-invitations` | ManagerInvitation[] theo email đã xác minh | Chính người nhận |
| POST | `/my-invitations/:id/accept` | `{invitation,user}` canonical sau xác nhận | Đúng email ACTIVE, lời mời còn hạn, inviter còn đủ quyền |
| GET | `/branches` | Page<OwnedBranch>; q/status/managerId/branchId | Owner đủ điều kiện, chỉ owned |
| POST | `/branches` | BranchInput → OwnedBranch | Owner đủ điều kiện; ownerId do server xác định |
| GET | `/branches/:id` | OwnedBranch + thông tin công khai/cấu hình | Ownership |
| PATCH | `/branches/:id` | `{value:BranchInput,version}` | Ownership + optimistic version |
| DELETE | `/branches/:id` | `{version}` → null | Nháp chưa có tài nguyên/lịch sử liên quan |
| PUT | `/branches/:id/assignment` | `{managerId:null|UUID,version,reason}` | Owned Branch + eligible OwnerManager |
| GET | `/branches/:id/policies` | OwnerPolicy[] mới nhất trước | Ownership |
| POST | `/branches/:id/policies` | PolicyInput → OwnerPolicy | Ownership; append phiên bản |
| PUT | `/branches/:id/qr` | `{image,version}` → OwnedBranch | Ownership + image validation + version |
| GET | `/managers` | Page<ManagerRelationship> | Owner đủ điều kiện; quan hệ thuộc Owner |
| GET | `/managers/:id` | User projection, relationship date, assignments | Quan hệ thuộc Owner |
| DELETE | `/managers/:id` | `{reason}` | Không còn Branch được phân công; không khóa USER |
| GET | `/invitations` | Page<ManagerInvitation> | Inviter Owner |
| POST | `/invitations` | InvitationInput → INVITED | Owner đủ điều kiện |
| POST | `/invitations/:id/cancel` | INVITED → CANCELLED | Inviter; chưa accepted |
| POST | `/invitations/:id/resend` | Gia hạn lời mời chưa xử lý | Inviter; cooldown 60 giây |
| GET | `/summary` | OwnerSummary; month YYYY-MM, branchId optional | Owned Branch hoặc tất cả owned |
| GET | `/payments` | Page<OwnerPayment>; environment/targetType/month | Chỉ payment thuộc owned Branch |
| GET | `/approvals` | Page<InvoiceAdjustmentProposal> | Owner sở hữu Branch của Invoice |
| POST | `/approvals/:id` | `{decision:APPROVED|REJECTED,reason}` | Owner, request PENDING, Invoice fingerprint còn khớp |
| GET | `/activity` | Page<OwnerActivity>; type/month | Chỉ feed của Owner |

Các mutation hiện chưa có trên BE. Schema DTO nằm tại `src/interfaces/owner.ts`; tái sử dụng BranchPolicy/Branch/User/ManagerPayment/ManagerSummary hiện có, không tạo mô hình tài chính cạnh tranh.

### Phần mở rộng workspace dùng chung

| Method | Proposed path | Chức năng |
| --- | --- | --- |
| POST | `/api/manager/branches/:branchId/adjustment-requests` | Manager/Owner đúng Branch gửi `{invoiceId,proposedTotal,reason,evidence}` |
| POST | `/api/tenant/tenancies/:tenancyId/invoices/:invoiceId/manual-payments` | Tenant gửi chứng cứ QR, tiền vẫn PENDING; idempotencyKey bắt buộc |
| GET | `/api/tenant/tenancies/:id/invoices[/invoiceId]` | Thêm optional `manualQr:{qrUrl,resourceId,reference}`; sandbox instructions cũ giữ riêng |

Đối soát vẫn sử dụng API và module Manager sẵn có. Request operational có `X-TroPro-Workspace: OWNER` khi mở qua `?workspace=owner`; mock kiểm tra cả Owner eligibility và ownership trước khi dùng nghiệp vụ chung. Header chỉ chọn ngữ cảnh, không phải bằng chứng phân quyền. Backend tương lai phải kiểm tra canonical roles/verification/resource ownership tương tự.

## 4. Authorization và workflow

### Xác minh

Mock fail-closed: account ACTIVE + canonical OWNER role + verification APPROVED mới đủ entitlement. Không suy ra Owner từ role claim do client gửi, hồ sơ tự khai, Booking hoặc tenant relationship. APPROVED không tự cấp role nếu trusted ADMIN decision không chỉ định grant.

`OwnerEngine.adminDecision()` là test harness gọi trực tiếp trong unit test: actor phải tồn tại canonical ACTIVE ADMIN và khác người được xét duyệt. Không có HTTP endpoint/Owner button tự duyệt. Test inject ADMIN fixture riêng; không thêm Admin dashboard hoặc tài khoản Admin đăng nhập thông thường.

Mock chỉ giữ `{name,mime,size,resourceId}` của tài liệu. Raw image chỉ tồn tại khi chọn/gửi hồ sơ, bị loại khỏi persistent state. Production cần upload chứng cứ vào kho private với ACL, retention, ADMIN review và role provisioning server-side; mock resource ID không phải Cloudinary upload thật.

### Branch

Code unique không phân biệt hoa/thường. Required code/name/address, tọa độ phải đầy đủ cặp và đúng biên; tối đa ba ảnh PNG/JPEG/WebP ≤1 MB/ảnh, kiểm tra signature thực tế. Chi nhánh mới nên lưu DRAFT. ACTIVE yêu cầu Manager canonical ACTIVE có OwnerManager cùng Owner và chính sách đã hiệu lực.

Ngừng hoạt động bị chặn khi còn draft/active contracts, active bookings, pending/confirmed appointments, invoices/receivables, pending/review payments, unused prepaid, verified Hold chưa có settlement workflow, tiền chưa phân bổ, unbilled charges hoặc maintenance chưa xử lý. Không dùng chuyển status để vượt các quy tắc nghiệp vụ. ACTIVE không quay về DRAFT. DELETE chỉ nháp rỗng; tài sản có lịch sử cần quy trình ngừng hoạt động có kiểm soát.

`version` chống ghi đè phân công/cấu hình stale trong mock runtime. BE cần optimistic locking/transaction và constraint đồng thời từ DB; frontend không thay thế chúng.

### Invitations và Manager

INVITED → ACCEPTED/CANCELLED; quá hạn được hiển thị EXPIRED. Mời chưa tạo USER hoặc role, không đặt mật khẩu của người khác. User mới đăng ký/xác minh qua auth chung, sau đó đăng nhập đúng email để chấp nhận. Existing User được dùng lại ID, không duplicate danh tính.

Acceptance tạo canonical MANAGER role + OwnerManager sau kiểm tra identity; **proposed Branches chưa tự phân công**. Owner xác nhận riêng bằng trạng thái và version mới nhất. Một Manager nhiều Branch cùng Owner; cross-Owner linkage bị chặn. Kết thúc quan hệ cần hết assignments; không khóa global USER, không gỡ TENANT/OWNER/USER khác.

### Policy

Append-only version tăng dần; effectiveFrom từ hiện tại (dung sai clock 60 giây) và sau phiên bản cuối. Current policy là phiên bản có hiệu lực tại thời điểm tạo Booking. Future version chưa hiệu lực không được dùng sớm. Không sửa object/snapshot lịch sử.

collectionDay 1–28, graceDays 0–90; VND nguyên không âm; payment/temporary hold 1–10080 phút và temporary ≥ payment; confirmed hold 1–525600 phút. Opening 0–23, closing 1–24 và sau opening; duration 15–480 phút, cancellationHours 0–168. Đây là giới hạn validation của mock proposal, cần BE xác nhận trước production.

### QR / manual receipt

QR gắn Branch qua qrPublicId/qrUrl, không thêm bank account, wallet hoặc payment_requests. Mock dùng data URL ảnh đã kiểm tra và resource ID giả; ảnh QR public khác giấy tờ private.

Tenant invoice reference là `TP-MQR-<invoice UUID>`, riêng cho nghĩa vụ; thay QR không đổi reference hoặc tạo payment. Sandbox booking/invoice flow cũ vẫn SANDBOX, không gắn ảnh QR thật vào giao dịch sandbox.

Manual claim yêu cầu active tenancy, own ISSUED invoice, amount ≤ availableToPay, matching reference, ảnh chứng cứ, receipt ID và idempotency key. Tạo TRANSFER/LIVE/PENDING, allocation 0. Manager/Owner phải đối chiếu độc lập trong workflow mock sẵn có; không tự xác minh claim của chính mình, không double-verify hoặc dùng lại provider transaction đã VERIFIED. Thông tin Tenant khai và ảnh không chứng minh ngân hàng thật đã nhận tiền. Production cần upload/API kiểm tra chứng cứ, audit và transaction server-side; demo không kết nối bank.

### Approval

Chỉ ISSUED invoice thuộc authorized Branch, tổng đề xuất khác hiện tại, một PENDING request/invoice, reason ≥10 ký tự, evidence hợp lệ. Lưu snapshot/fingerprint chứng từ. Owner quyết định một lần, stale fingerprint bị từ chối. Manager không duyệt Owner queue.

`status=APPROVED|REJECTED` và `executionStatus=NOT_APPLIED` độc lập. Không thay invoice items, payments, prepaid applications hoặc debt. Chưa có approved replacement/correction algorithm; Q1–Q5 vẫn OPEN. BE cần request/decision/audit/concurrency/execution contract và policy được duyệt trước khi triển khai phần thực thi.

## 5. Tính toán và nguồn dữ liệu

OwnerSummary tái dùng `manager-finance.reportSummary`, `paymentViews`, Tenant invoice financialSummary và BigInt `sumVnd`; không tính tiền trong JSX.

| Chỉ số | Nguồn và kỳ |
| --- | --- |
| billed | Tổng ISSUED invoice đúng billingMonth, không gồm DRAFT/CANCELLED |
| receipts | VERIFIED, non-SANDBOX, không BOOKING_HOLD, theo verifiedAt trong tháng; gồm prepaid thật nhận trong tháng |
| sandboxReceipts | Giống receipts nhưng SANDBOX, trình bày riêng |
| debt/overdue | Outstanding/overdue của ISSUED invoice trong kỳ báo cáo; gồm allocation hợp lệ và prepaid đã áp dụng |
| prepaidReceived/Applied/Unused | Lũy kế VERIFIED non-sandbox PREPAID_RENT, applied FIRST+LAST, unused received−applied |
| holdReceived/holdSandbox | Lũy kế tiền giữ phòng VERIFIED, tách thực/sandbox; không phải prepaid hoặc doanh thu |
| unmatched | Lũy kế thực nhận VERIFIED INVOICE còn vượt allocatedAmount; không phải ví Tenant |
| occupancy | Hiện tại; total physical Rooms riêng; ROOM whole-room và SLOT active slot là hai đơn vị sử dụng riêng. SLOT trong parent Room/Branch ngừng hoạt động không được tính trống, có nhóm unavailable/không phục vụ |
| bookingCount | Yêu cầu tạo trong tháng; trạng thái nguồn vẫn từ domain dùng chung |
| contracts/tenancies/maintenance | Trạng thái hiện tại trong authorized Branch |

Không cộng prepaid applied như cash mới; không suy ra revenue. Các snapshot seed có nghĩa vụ lịch sử nhưng không dựng ledger thiếu thành dòng tiền giả. Đối chiếu từng Branch phải cộng ra đúng all-owned totals.

## 6. Cache, persistence, activity

Owner query keys `['owner',userId,scope,resource,...filters]`; verification/Branch entitlements theo user. Server state ở TanStack Query; Zustand chỉ selection theo user. Logout/account switch dùng cơ chế clear cache trước đó. Mutation invalidates; polling refresh Branch permissions 3 giây, Manager 2 giây, Owner resource 5 giây. Khi entitlement/scope mất, không render outlet riêng tư và xóa cache không còn quyền.

Owner mock projection nằm trong `ManagerState.owner` và được commit/rollback cùng inventory, tenants, leasing DB; không có Owner fixture universe độc lập. Public discovery đọc visibility/metadata qua provider binding; Booking đọc policy provider chung. Reset Manager/Tenant khôi phục cả Owner, giữ auth sessions theo cơ chế demo cũ.

Activity feed chỉ whitelist summary/actor/time/reference; không raw payload, password, token, document content. Các chứng từ seed được hệ thống nhập vào feed với thời điểm tiếp nhận trong phiên; đây là basic activity summary, không phải Audit Search hay historical event store hoàn chỉnh. Không chứng nhận production multi-client synchronization: mock state theo browser session/tab, dùng đổi tài khoản trong cùng tab để demo liên vai trò.
