# Đối chiếu FE → BE và các quyết định còn mở

Ngày kiểm tra: 20/10/2026. Phạm vi: controller/service/DTO BE trên dev và các service/UI hiện có ở FE.

Mốc Git khi đối chiếu: `dev` và `origin/dev` tại `9d5ef87`, sau các merge `8da523f` (Student 4), `d3f198e` (Student 6), `9d5ef87` (Student 2 API). Kết quả dưới đây bao gồm **các thay đổi working tree chưa commit** của đợt tích hợp API thật; không khẳng định riêng commit `9d5ef87` đã chứa các sửa đổi đó. Khi tạo commit bàn giao, cập nhật mốc này ở đợt audit tiếp theo.

Đây là tài liệu bàn giao cho toàn nhóm sau merge, không phải cam kết mọi API đã hoàn thành. Đọc cùng [danh mục API FE còn thiếu hoặc lệch hợp đồng](fe-be-api-contract.md). Mỗi vấn đề dùng năm trường: Tên vấn đề; Nguyên nhân; Tên màn hình hoặc endpoints trên FE bị sai; Student cần sửa; Hướng đề xuất giải quyết. Một endpoint nằm trong danh sách bị ảnh hưởng không nhất thiết sai URL: có thể thiếu dữ liệu, thiếu quyền hoặc chưa được BE triển khai.

## Nguyên tắc cùng sửa và bàn giao sau mỗi đợt merge

- **Có thể sửa FE, BE hoặc cả hai.** Không bắt BE sao chép nguyên DTO/business rules từ UI demo. Nếu BE đã đúng và phù hợp hệ thống, ưu tiên sửa FE adapter/validation/copy; nếu BE thiếu hoặc trái nghiệp vụ thì sửa BE và test, rồi đồng bộ FE.
- Bảo đảm đúng business rules đã thống nhất và phù hợp toàn dự án; giữ Modular Monolith `common`, `modules`, ranh giới Service Interface/read DTO; không sửa schema, trạng thái hay quy tắc tiền chỉ để hết lỗi giao diện. Quyết định OPEN trong tài liệu không tự biến thành quy tắc đã chốt.
- Phân công Student dưới đây theo **owner BE trong be_tasks.md**. Tài liệu đó chưa phân công FE chính thức; owner BE điều phối với người phụ trách FE của màn hình. Phối hợp không có nghĩa đổi owner hoặc giao hai người cùng viết một nghiệp vụ.
- Khi sửa code, **thành viên bắt buộc cập nhật docs trong cùng PR/đợt merge**: tài liệu API của module, hai báo cáo này, business rules liên quan và hướng dẫn cấu hình/test. Nêu request/response trước–sau, enum/nullability/pagination, mã lỗi, quyền/scope, nguồn dữ liệu và các quyết định mới để người pull code tiếp tục làm được.
- Nếu thay đổi nghiệp vụ, ghi quyết định, lý do, người duyệt và ảnh hưởng tới Student khác vào đặc tả tương ứng; đồng bộ `be_tasks.md`, `all-feature.txt`, `database/rental/REFACTOR_REPORT.md`, `backend/contract-policies.md` khi thực sự liên quan. Không sửa trạng thái checklist thành xong chỉ vì có entity/controller.
- Sau mỗi merge: đối chiếu lại router/services FE với controller/service/DTO BE; cập nhật trạng thái thiếu/đã có/lệch hợp đồng/bị chặn; kiểm tra test; cập nhật `services/real-api.ts` **chỉ khi contract và quyền đã đủ**, không mở toàn bộ endpoint để vượt lỗi 501.
- Tiêu chí bàn giao một vấn đề: owner xác nhận nghiệp vụ; FE/BE có DTO thống nhất; test cả thành công, lỗi, quyền và request lặp nếu có tiền/trạng thái; docs có ví dụ không chứa secrets; các bên phụ thuộc đã biết thay đổi. Tài liệu frontend đời mock được giữ làm lịch sử và không được ưu tiên hơn đặc tả đã chốt.

| Student | Trách nhiệm chính dùng để giao việc |
|---|---|
| 1 | Auth/Security/User/Role; Owner–Manager–Branch, QR cấu hình, audit/quyền dùng chung |
| 2 | Room/RoomType/Slot, tìm phòng công khai, Appointment, read API/dashboard |
| 3 | Booking/Contract/ContractTenant, vòng đời và nguồn thỏa thuận thuê, Zalo |
| 4 | Service/RoomService/ContractService, charge/allocation, Notification |
| 5 | Invoice/công nợ/Payment/QR nhận tiền hoặc Sandbox, xác minh và đối soát |
| 6 | Tenant/Occupancy, Furniture/Maintenance, review Owner, tích hợp/test/deploy |

Nguồn quyết định nghiệp vụ: [be_tasks.md](../be_tasks.md), [all-feature.txt](../all-feature.txt), [coding_rules.md](../coding_rules.md), [REFACTOR_REPORT.md](../database/rental/REFACTOR_REPORT.md), [contract-policies.md](../backend/contract-policies.md), [STUDENT6_API.md](../backend/STUDENT6_API.md). Không có file tên REQUIREMENTS trong repository; báo cáo dùng các đặc tả nghiệp vụ này và DDL rental hiện hành.

## Phần đã được bật

Ứng dụng development/production mặc định real, không khởi động mock worker. Mock/hybrid chỉ được dùng trong unit test. `services/real-api.ts` giới hạn request tới các hợp đồng đã kiểm tra. Chức năng chưa được bật trả trạng thái chưa khả dụng (lỗi cục bộ 501), không gửi dữ liệu tới endpoint phỏng đoán, không thay bằng dữ liệu mock.

| UI/luồng | API thật được bật | Dữ liệu/hành vi |
|---|---|---|
| `/`, `/rooms` | GET `/api/v1/public/rooms` | Gửi page từ 0, giữ số trang phản hồi từ 1. Địa chỉ, giá, sức chứa, branchId, roomTypeId và các sort BE hỗ trợ. Không suy ra phòng trống từ AVAILABLE. |
| `/rooms/:roomId` | GET `/api/v1/public/rooms/{id}` | Map RoomResponse + thông tin Branch/RoomType sang view model. Giá là giá tham chiếu; số slot trống chưa biết. Trạng thái INACTIVE/MAINTENANCE hiển thị đúng dù không có dữ liệu khả dụng. |
| `/login` | POST `/api/auth/login` | Map AuthResponse sang session; access token chỉ ở bộ nhớ, cookie refresh HttpOnly do BE đặt. |
| `/register` | POST `/api/auth/register` | HTTP 202 không tự tạo phiên; chuyển sang bước xác minh email. |
| Xác minh email | POST `/api/auth/verify-email` | Gửi token, sau thành công quay về đăng nhập. |
| Khôi phục phiên / đăng xuất | POST `/api/auth/refresh`, POST `/api/auth/logout` | Cookie credentials, không đọc/lưu refresh token trong JS. |
| Thông báo cá nhân | GET `/api/notifications`, GET `/api/notifications/unread-count` | Lọc read, page từ 0. Map unreadCount → count; không gửi lọc type BE chưa hỗ trợ. |
| Đánh dấu thông báo | PUT `/api/notifications/mark-read`, PUT `/api/notifications/mark-all-read` | Gửi notificationIds; dùng danh tính JWT phía BE. |
| Admin quản lý User | GET `/api/users`, GET `/api/users/{id}`, POST `/api/users`, PATCH `/api/users/{id}/active-status` | Danh sách Spring Page từ 0 → UI từ 1. Chỉ gửi field BE nhận; tạo User INVITED, không tự cấp role/password. Kích hoạt do BE kiểm tra điều kiện. |

Không bật DELETE User: UI chưa có luồng xóa phù hợp; không suy diễn xóa vật lý là ngừng hoạt động.

## Xung đột 1 — Guest bị chặn ở API phòng công khai (đã giải quyết theo xác nhận)

- **Tên vấn đề:** SecurityConfig chặn Guest đọc phòng.
- **Nguyên nhân:** BE có PublicRoomController nhưng cấu hình ban đầu chỉ permitAll cho auth, còn lại authenticated; FE `/` và `/rooms` dành cho Guest. Trái P1-11, P1-37, P1-39 và BR-04.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/`, `/rooms`, `/rooms/:roomId`; GET `/api/v1/public/rooms`, GET `/api/v1/public/rooms/{id}`.
- **Student cần sửa:** Student 1 sửa SecurityConfig; Student 2 xác nhận public DTO và scope dữ liệu; Student 6 phối hợp kiểm thử tích hợp.
- **Hướng đề xuất giải quyết:** Sửa BE mở đúng GET `/api/v1/public/rooms` và GET `/api/v1/public/rooms/{id}`. Người dùng đã đồng ý trong phiên làm việc. Đã thực hiện; các phương thức ghi, API quản trị và URL public khác vẫn yêu cầu xác thực. Có test MockMvc xác nhận giới hạn này.

## Xung đột 2 — Tìm phòng và dữ liệu khả dụng chưa cùng hợp đồng

- **Tên vấn đề:** UI cần khu vực/hình thức thuê/số chỗ trống mà Public Room API chưa cung cấp đầy đủ.
- **Nguyên nhân:** BE nhận address, branchId, roomTypeId, giá, minCapacity, status; chưa nhận rentalMode, availableOnly, area hoặc sort diện tích. Chưa có public room-types, availability, Branch detail/policy. DTO chỉ có operationalStatus, không có số slot đã giữ/đang thuê/có thể đặt. UI cũ có các bộ lọc và luồng đặt chỗ đó. BR-06, P1-26–29, P1-37–39 yêu cầu khả dụng dựa trên dữ liệu chiếm dụng/giữ chỗ, không được đồng nhất AVAILABLE với còn trống.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/`, `/rooms`, `/rooms/:roomId`, `/branches/:branchId`; FE gọi các đường `/api/v1/public/room-types`, `/api/v1/public/branches/{id}`, `/policy`, `/api/v1/public/rooms/{id}/availability` chưa có ở BE.
- **Student cần sửa:** Student 2 chủ trì tìm phòng/khả dụng; Student 1 cung cấp Branch/policy công khai; Student 3 và Student 6 cung cấp allocation/cư trú; Student 2 phối hợp người sửa FE.
- **Hướng đề xuất giải quyết:** Ưu tiên FE chỉ gửi bộ lọc BE hỗ trợ và hiển thị chưa biết khả dụng (đã làm). Home không cho chọn bộ lọc chưa hỗ trợ; `/rooms` đã có kiểm soát tương tự. Giữ trạng thái lỗi cho URL cũ chứa bộ lọc không hỗ trợ, không âm thầm bỏ lọc. Đề nghị quyết định tiếp: bổ sung API khả dụng/Branch và bộ lọc thiếu ở BE, hoặc duyệt việc thu gọn UI. Không tự giả định các field thiếu hoặc dùng mock để cho phép booking.

## Xung đột 3 — Các endpoint nghiệp vụ chưa tồn tại

- **Tên vấn đề:** FE có màn hình và thao tác cho nghiệp vụ BE chưa triển khai controller.
- **Nguyên nhân:** Chưa có `/api/me`, forgot/reset-password, `/api/appointments` CRUD, Booking/Contract CRUD, Invoice/Payment, các tổng hợp Dashboard/report/audit/roles, Owner onboarding/Branch/Manager/QR/policy, hoặc `/api/manager/branches` và `/api/tenant/tenancies`. Entity/DDL hoặc tài liệu kế hoạch không đồng nghĩa có API. UI cần các read model và command cụ thể; không thể thay endpoint chỉ bằng đổi prefix. Tham chiếu BE-01, BE-02, BE-03, BE-05; BR-08, BR-13–15; contract-policies còn quyết định RENT/PREPAID chưa chốt.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/forgot-password`, `/reset-password`, `/profile`, `/user/dashboard`, các trang User Appointment/Booking, workspace `/owner/*`, `/manager/*`, `/tenant/*`, `/admin/*` ngoài các API đã bật. Endpoint và field cụ thể được kiểm kê theo C01–C38 trong [fe-be-api-contract.md](fe-be-api-contract.md).
- **Student cần sửa:** Student 1: identity/Owner/Branch; Student 2: Room/Appointment/read dashboard; Student 3: Booking/Contract/Zalo; Student 4: Utilities/Notification; Student 5: Invoice/Payment; Student 6: Tenant/Maintenance/Owner verification; mỗi owner phối hợp FE tương ứng.
- **Hướng đề xuất giải quyết:** Ưu tiên giữ các màn hình báo chưa khả dụng và không gửi command chưa có hợp đồng (đã làm). Người dùng quyết định thứ tự bổ sung BE hoặc ẩn/thu gọn các mục menu. Không tự tạo endpoints BE, tính số dư, xác minh tiền hoặc sửa contract để làm khớp demo.

## Xung đột 4 — API Student 6 vẫn từ chối mọi người dùng

- **Tên vấn đề:** Maintenance/Tenant/Owner verification/Thread có controller nhưng chưa có security adapter hoạt động.
- **Nguyên nhân:** PendingSecurityCurrentUserProvider, PendingSecurityMaintenanceAccessPolicy, PendingSecurityTenantAccessPolicy và PendingSecurityOwnerVerificationAccessPolicy còn fail-closed; JWT thật không thay đổi kết quả này. FE Admin review còn đòi GET detail/document/history/version và POST decision, trong khi BE Owner verification có list và POST approve/reject, DTO khác. Tenant API trả danh mục User và lịch sử cư trú, không phải tenancy view model của FE. Đối chiếu P1-08, P1-31–36, P1-107–122, P1-132/134; BR-03, BR-16/17.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/manager/tenants`, `/manager/tenancies/:contractTenantId`, `/manager/furnitures`, `/manager/furnitures/:furnitureId`, `/manager/maintenance`, `/manager/maintenance/:reportId`, `/tenant/*`, `/admin/owner-verifications`, `/admin/owner-verifications/:userId`. BE hiện có `/api/tenants`, `/api/furnitures`, `/api/maintenance-requests`, `/api/admin/owner-verifications` nhưng chưa thay thế được toàn bộ read model FE.
- **Student cần sửa:** Student 1 chủ trì adapter danh tính/quyền dùng chung; Student 6 chủ trì Tenant/Maintenance/review Owner; Student 3 phối hợp quan hệ ContractTenant; Student 6 phối hợp chuyển FE sang DTO thật.
- **Hướng đề xuất giải quyết:** Cần người dùng duyệt sửa BE nối AuthPrincipal + kiểm tra quan hệ Owner/Manager/Branch/Tenant. Sau đó sửa FE theo DTO và các command hiện có; nếu UI cần document/history/detail phải thống nhất API bổ sung. Không bỏ kiểm tra quyền hoặc truyền userId từ FE để thay principal. Các adapter này hiện chưa được bật.

## Xung đột 5 — API Room quản trị thiếu kiểm tra phạm vi

- **Tên vấn đề:** Không thể coi API quản trị Room là hợp lệ chỉ vì đã có controller.
- **Nguyên nhân:** Property controller/service hiện không kiểm tra Owner/Manager với Branch; SecurityConfig chỉ yêu cầu đã đăng nhập. UI có Branch selector nhưng BE chưa có endpoint danh sách Branch được phép quản lý. P1-23, P1-30, P1-134, BR-03 yêu cầu BE bảo vệ phạm vi, đồng thời chặn tác động làm mất tính nhất quán khi còn thuê/giữ chỗ/nghĩa vụ.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/manager/rooms`, `/manager/rooms/new`, `/manager/rooms/:roomId`, `/manager/rooms/:roomId/edit`, `/manager/rooms/:roomId/slots`, `/manager/room-types`; owner vận hành qua `/manager/*?workspace=owner`. FE bootstrap `/api/manager/branches` còn thiếu; BE CRUD nằm ở `/api/v1/rooms`, `/api/v1/room-types`, `/api/v1/room-slots`.
- **Student cần sửa:** Student 2 chủ trì property service/FE adapter; Student 1 bổ sung quyền và danh sách Branch có scope; Student 3/6 phối hợp kiểm tra tài nguyên đang thuê/giữ chỗ.
- **Hướng đề xuất giải quyết:** Ưu tiên sửa BE về authorization và các điều kiện thay đổi tài nguyên theo nghiệp vụ, cung cấp Branch read model có scope. Sau đó FE map Room/Type/Slot đúng DTO và pagination. Không dùng bộ lọc branchId phía FE thay cho phân quyền. Adapter quản trị hiện bị chặn, không cấp quyền giả từ mock.

## Xung đột 6 — Utilities sai actor, scope và payload phân bổ

- **Tên vấn đề:** Các thao tác Services/Charge/Allocation chưa thể nối an toàn chỉ bằng đổi provider.
- **Nguyên nhân:** Utilities SecurityUtils parse Authentication.getName() như UUID, nhưng JWT dùng AuthPrincipal record; lỗi parse dẫn đến UUID hệ thống cố định. ContractService/MeterCharge/CostAllocation controllers dùng helper đó. Kiểm tra role chưa thay thế kiểm tra quan hệ Branch. FE proposal dùng chargeId/shares, BE dùng roomServiceChargeId/allocationDetails; method enum hiện đã khớp RESIDENT_DAYS/APPROVED_CUSTOM; adapter tenant còn gán proposedBy thành contractTenantId và dùng thời gian hiện tại khi thiếu createdAt. Manager charge adapter chỉ gửi segment đầu tiên của UI nhiều segment. Services dùng request page từ 0, còn wrapper cũ gửi từ 1; Room/Notification phản hồi số trang từ 1 trong khi Services phản hồi pageNumber từ 0. Tham chiếu BE-04, BR-03, BR-09–12; RULE 03/05 về DTO/PagedResponse.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/manager/services`, `/manager/room-services`, `/manager/service-requests`, `/manager/utility-charges`, `/manager/utility-charges/new`, `/manager/utility-charges/:chargeId`, `/manager/utility-charges/:chargeId/edit`, `/manager/cost-allocations`, `/tenant/services`, `/tenant/utilities`, `/tenant/utilities/allocations`. FE dùng `managerApi`, `tenantApi`, `utilitiesService`; endpoint thật `/api/contract-services`, `/api/service-charges`, `/api/cost-allocations` khác endpoint projection cũ.
- **Student cần sửa:** Student 4 chủ trì DTO/charge/allocation và FE adapter; Student 1 sửa actor/scope dùng chung; Student 6 cung cấp dữ liệu cư trú; Student 5 xác nhận nguồn phí chốt cho Invoice.
- **Hướng đề xuất giải quyết:** Cần duyệt sửa BE lấy actor từ AuthPrincipal và kiểm tra scope; không dùng UUID mặc định. Ưu tiên sửa FE adapter/pagination theo từng endpoint, chỉ gửi payload có thể biểu diễn đầy đủ, giữ unknown nếu không có thời gian/dữ liệu. Chốt hợp đồng nhiều segment và allocationDetails trước khi bật command; nếu cần transaction nhiều segment thì thống nhất API BE. Hiện không gửi request Utilities từ ứng dụng. Các unit test adapter cũ chỉ kiểm tra serialization riêng; không chứng minh endpoint đã được bật.

## Xung đột 7 — Gửi thông báo thiếu giới hạn người gửi/người nhận

- **Tên vấn đề:** UI gửi single/bulk không được coi là hoàn chỉnh về quyền.
- **Nguyên nhân:** BE POST send/send-bulk chưa có kiểm tra role và phạm vi người nhận ở controller/service; mọi tài khoản đã đăng nhập có thể tới endpoint. FE chỉ hiện nút với ADMIN/OWNER/MANAGER, nhưng ẩn nút không bảo vệ BE. Tham chiếu P1-123/124/134 và BR-03.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/notifications` (dialog gửi single/bulk); POST `/api/notifications/send`, POST `/api/notifications/send-bulk`. Đọc và đánh dấu thông báo cá nhân đã bật; gửi đang bị chặn.
- **Student cần sửa:** Student 4 chủ trì Notification controller/service và dialog FE; Student 1 phối hợp quyền người gửi/phạm vi người nhận; owner từng module xác định sự kiện nghiệp vụ hợp lệ.
- **Hướng đề xuất giải quyết:** Duyệt quy tắc người gửi/người nhận, sửa BE enforce quyền và scope, sau đó bật FE gửi single/bulk theo DTO hiện có. Đọc/đếm/đánh dấu thông báo cá nhân đã được nối thật; gửi thông báo đang bị chặn, không giả báo thành công.

## Xung đột 8 — Nhiều role và các trường User chưa được trả đầy đủ

- **Tên vấn đề:** UI chuyển vai trò và các thông tin bổ sung chưa có read contract đầy đủ.
- **Nguyên nhân:** D03/P1-10/BR-03 cho phép User nhiều role, nhưng AuthResponse chỉ trả role chính, JWT cũng được phát theo vai trò chính. UserResponse chưa trả roles, scope, contactVerified, canActivate, version hoặc lịch sử audit. FE không được tự gán các quyền còn lại hay dựng history/version.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/login`, mọi layout có chọn vai trò, `/admin/users`, `/admin/users/:userId`, `/admin/roles`, `/owner/onboarding`; POST `/api/auth/login`, POST `/api/auth/refresh`, GET `/api/users`, GET `/api/users/{id}`, GET `/api/admin/roles` (chưa có).
- **Student cần sửa:** Student 1 chủ trì Auth/User/Role DTO và quyền; Student 6 phối hợp Owner verification/audit; người phụ trách FE auth/admin sửa view model theo DTO được duyệt.
- **Hướng đề xuất giải quyết:** Hiện FE chỉ sử dụng đúng role BE cấp và hiện thông tin chưa được cung cấp. Thao tác active-status gửi Boolean active, để BE xác nhận password/verification. Đề nghị chốt read DTO/switch-role và quyền theo scope ở BE trước khi bật chuyển vai trò hoặc mở các màn hình tương ứng. Không tự suy ra quyền từ User ID hoặc danh sách demo.

## Xung đột 9 — Enum và read model tài chính từ demo chưa khớp BE

- **Tên vấn đề:** Payment environment, chỉ dẫn thanh toán và các giá trị thỏa thuận cần hợp đồng thống nhất.
- **Nguyên nhân:** BE PaymentEnvironment là REAL/SANDBOX; schema FE cũ dùng LIVE, PRODUCTION ở các vị trí khác nhau và Tenant invoice instructions còn literal SANDBOX. FE còn cần rent/prepaid/financial summaries mà Entity/DDL không tự tạo được đúng nguồn. UI field không được dùng để ép BE đổi enum hoặc lấy Room.referencePrice làm tiền thuê đã thỏa thuận. AllocationMethod hiện đã khớp RESIDENT_DAYS/APPROVED_CUSTOM; vấn đề Allocation là payload/IDs, không phải bắt đổi enum.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/user/bookings/:bookingId/payment`, `/manager/contracts/:contractId/tenants`, `/manager/invoices/*`, `/manager/payments/*`, `/manager/debts`, `/tenant/invoices/*`, `/tenant/payments`, `/tenant/contracts/:contractId/my-terms`, `/owner/finance`, `/owner/payments`; các caller bookingService, tenantApi và managerApi. Chi tiết C08, C13–14, C21–24, C26–28, C33 trong contract.
- **Student cần sửa:** Student 5 chủ trì money/payment DTO; Student 3 chốt nguồn thỏa thuận thuê; Student 2 read/report; Student 1 QR/BranchPolicy; Student 4/6 nguồn phí. Owner từng phần phối hợp người sửa FE schema/adapter.
- **Hướng đề xuất giải quyết:** Ưu tiên sửa FE map enum và nullability theo BE đúng nghiệp vụ. Chốt RENT/FIRST–LAST theo contract-policies, trả số tiền/nguồn đã được BE tính và kiểm tra; không dựng tiền từ text, không tự xác minh bằng outcome của mock, không đếm lại prepaid application như thu mới. Chỉ mở API khi read model và commands có nguồn đúng, quyền và test phù hợp; giữ các quyết định OPEN để nhóm duyệt.

## Xung đột 10 — Authentication mới chỉ đáp ứng email flow

- **Tên vấn đề:** API login đã nối nhưng chưa đủ phạm vi email hoặc phone theo P1-03.
- **Nguyên nhân:** Form FE và LoginRequest/AuthService hiện dùng email + password. all-feature.txt/be_tasks.md P1-03 yêu cầu email hoặc số điện thoại. Việc flow email hoạt động không chứng minh phone đã được hỗ trợ; đây là gap scope chứ không cần tạo endpoint login thứ hai.
- **Tên màn hình hoặc endpoints trên FE bị sai:** `/login`, `/register`, `/forgot-password`, `/reset-password`; POST `/api/auth/login`, POST `/api/auth/register` và recovery endpoints còn thiếu; C01–C03.
- **Student cần sửa:** Student 1 chủ trì request/validation/normalization và identity lookup; người sửa FE auth phối hợp form/copy; Student 6 test case email/phone, trùng liên hệ và trạng thái tài khoản.
- **Hướng đề xuất giải quyết:** Chốt dùng identifier hoặc hai field email/phone với quy tắc exactly-one, chuẩn hóa số điện thoại và xác minh liên hệ. Sửa BE hoặc cả FE/BE theo nguồn nghiệp vụ; giữ backward compatibility email flow, không tự cho đăng nhập bằng liên hệ chưa được xác minh hoặc thêm phone OTP ngoài phạm vi đã duyệt.

## Kiểm tra và giới hạn

Kiểm tra lại trong đợt cập nhật tài liệu: toàn bộ backend **167 tests, 0 failures/errors/skipped — BUILD SUCCESS**; frontend **332 tests, 14 files — PASS**, `npm run build` **PASS**, `git diff --check` **PASS**. Đây là kiểm tra bản tích hợp để chia sẻ cho nhóm, không phải xác nhận hoàn tất mọi feature hoặc sẵn sàng release production. Những API đã bị chặn và ghi nhận rõ không tự động là blocker push dev; lỗi compile/test bắt buộc, conflict chưa xử lý hoặc secrets mới là các blocker cần có bằng chứng cụ thể.

- Kiểm tra frontend bằng build/TypeScript và unit tests, có request tests cho real contracts và việc chặn endpoint chưa được duyệt.
- Backend có MockMvc kiểm tra Guest chỉ đọc hai endpoint công khai; database/service được mock trong test security này. Hai test lifecycle/logical removal từ đợt trước vẫn được giữ.
- Chưa xác nhận login/email/database/ghi dữ liệu với một backend triển khai thực tế; kiểm tra request dùng response đúng DTO trong test và kiểm tra security dùng MockMvc.
- Cần restart BE để SecurityConfig mới có hiệu lực, restart Vite nếu đổi env. Production khác origin cần VITE_API_BASE_URL, CORS và cookie phù hợp host thực tế.
- Không stage, commit, push hoặc tạo branch trong đợt này.
