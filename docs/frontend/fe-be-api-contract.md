# API FE còn thiếu và hợp đồng cần thống nhất với BE

Cập nhật: **20/10/2026**, đối chiếu code trên dev và working tree sau ba merge. Bản kiểm kê này phục vụ giao việc; **URL FE đang gọi hoặc URL đề xuất không có nghĩa BE đã triển khai**. Đọc cùng [real-api-integration-audit.md](real-api-integration-audit.md).

## Cách sử dụng và phạm vi

- Nguồn nghiệp vụ: [be_tasks.md](../be_tasks.md), [all-feature.txt](../all-feature.txt), [coding_rules.md](../coding_rules.md), [refactor report](../database/rental/REFACTOR_REPORT.md), [contract policies](../backend/contract-policies.md), [Student 6 API](../backend/STUDENT6_API.md). Router, service và schema Zod trong `fe-sba-project/src` là bằng chứng UI đang cần gì, không tự trở thành business rules.
- Mỗi nhóm C01–C38 có bốn trường theo mẫu giao việc. **THIẾU**: chưa có controller tương ứng. **MỞ RỘNG**: API đã có, thiếu field/khả năng. **LỆCH/BỊ CHẶN**: có BE nhưng URL/DTO/quyền hoặc vòng đời không khớp FE. **ĐÃ NỐI**: không cần viết API trùng, chỉ liệt kê phần còn thiếu.
- Có thể sửa FE, BE hoặc cả hai. Ưu tiên sửa FE khi BE đã đúng. Không tạo API facade mới chỉ để giữ prefix demo nếu adapter có thể dùng controller hiện hữu. Các đường đề xuất cần được owner chốt; đổi đường dẫn phải sửa caller, test và docs cùng lúc.
- Student thực hiện là owner BE theo phân công hiện hành, kèm bên phối hợp; owner phối hợp người sửa FE, vì be_tasks.md chưa giao FE chính thức. Không đẩy nghiệp vụ tiền của Student 5 sang Student 2 chỉ vì Student 2 làm read API.
- Phạm vi P1 trước; phần P2/P3 và nghiệp vụ còn OPEN được ghi để theo dõi, không tự nâng thành điều kiện bắt buộc cho P1. Không yêu cầu thêm Thread/eKYC/PDF/OTP khi chưa có màn hình và chưa duyệt phase. Không yêu cầu BE triển khai `/api/demo/mailbox`, các reset/revoke/demo/scenario hoặc client `outcome` mô phỏng.

## Constraint chung áp dụng cho mọi nhóm

1. ID là UUID theo đúng thực thể. `userId`, `contractId`, `contractTenantId`, `roomId`, `roomSlotId`, `chargeId`, `paymentId` không được hoán đổi. Actor và Owner/Manager/Tenant scope lấy từ JWT + quan hệ BE; không tin role, ownerId hoặc userId do client tự gửi.
2. Dùng Request/Response DTO, `ApiResponse<T>` và `PagedResponse<T>` theo coding rules; không trả entity nội bộ. List production cần filter/sort/page có giới hạn. Phải ghi rõ request page bắt đầu từ 0 hay 1, response page bắt đầu từ đâu, size tối đa và sort được phép. FE đang dùng UI page từ 1, Manager table nội bộ có nơi từ 0; không áp một phép cộng/trừ cho mọi API. `/api/users` hiện trả Spring Page: hỗ trợ adapter trong thời gian chuyển tiếp, không đổi BE âm thầm.
3. Phân biệt `null`/unknown với 0, danh sách rỗng, false. Không tạo timestamp hiện tại thay dữ liệu thiếu, không dựng tên/roles/availability/history/version giả. Nếu field demo không thuộc nghiệp vụ, sửa FE thay vì bắt BE sản xuất dữ liệu giả.
4. Tiền dùng VND chính xác, không lỗi số thực; FE chỉ hiển thị số BE đã kiểm tra. Với số vượt `Number.MAX_SAFE_INTEGER`, chốt decimal-string/adapter trước khi đổi DTO. BE trả enum chuẩn từ module/DDL; FE map nhãn, không tự phát minh enum chung giữa các domain.
5. `PaymentEnvironment` BE là `REAL | SANDBOX`; FE cũ còn `LIVE`, `PRODUCTION`, `TRANSFER` hoặc `SANDBOX` ở các trường khác nhau. Phân biệt **method**, **environment**, **status**; sửa schema/adapter có test, không đổi REAL thành SANDBOX hoặc ngược lại để parse được.
6. READ không lộ secrets, giấy tờ, thông tin Tenant, nợ hoặc QR/Zalo riêng ngoài scope. Command thay trạng thái/tiền cần validate state, concurrent update/idempotency phù hợp, lưu audit và trả lỗi rõ. Kiểm tra quyền BE là bắt buộc dù FE đã ẩn nút.
7. Contract chính theo Room; nghĩa vụ/Invoice theo ContractTenant; Booking Hold khác PREPAID_RENT FIRST/LAST. Không có ví Tenant, security deposit tự tạo hoặc bảng ngoài thiết kế chỉ để hợp UI cũ. RENT/agreed terms lấy từ leasing, phí SERVICE từ Student 4, REPAIR đã duyệt từ Student 6.
8. Mọi thay đổi code phải cập nhật docs tương ứng, ví dụ request/response không chứa secrets, quyền/state/error, kết quả test và quyết định nghiệp vụ. Cập nhật hai tài liệu này sau mỗi merge để người pull code nhận biết API nào thật sự đã dùng được.

## C01 — Đăng nhập, đăng ký và chuyển vai trò

- **Tên màn hình (nêu rõ endpoints):** `/login`, `/register?redirect=%2Fuser%2Fdashboard`, `/verify-email`, các layout chọn vai trò.
- **API chi tiết cần thêm:** **ĐÃ NỐI** POST `/api/auth/login`, `/register`, `/verify-email`, `/refresh`, `/logout`; không tạo lại. **MỞ RỘNG** login bằng email **hoặc phone** theo P1-03; request field chính xác cần chốt với FE. Mở rộng login/refresh hoặc read session để có danh sách roles được cấp và activeRole; command đổi vai trò nếu cần là hợp đồng mới cần chốt, chưa có caller chính thức.
- **Các constraint, field mà BE cần sản xuất:** Giữ `accessToken, tokenType, expiresIn, accountId, email, fullName, role` cho adapter hiện tại; bổ sung roles/scope chỉ từ quan hệ thật. Registration không tự cấp OWNER; không cho role tùy ý từ client. Refresh token chỉ qua HttpOnly cookie; register 202 không tự tạo session. P1-01–05, P1-09–12, D03/BR-03. FE dùng `redirect` để điều hướng, không gửi nó thành quyền.
- **Student thực hiện:** Student 1; phối hợp người sửa FE auth/layout và Student 6 test tích hợp.

## C02 — Quên và đặt lại mật khẩu

- **Tên màn hình (nêu rõ endpoints):** `/forgot-password`, `/reset-password`.
- **API chi tiết cần thêm:** **THIẾU** POST `/api/auth/forgot-password` body `{email}`; POST `/api/auth/reset-password` body `{token,password}`. FE `authService.forgot/reset` đã có caller; response thành công `data:null`. Nếu mở rộng phone thì đồng bộ form và challenge, không đổi ngầm email flow.
- **Các constraint, field mà BE cần sản xuất:** Challenge hiện hành trên USERS: purpose/hash/expiry/attempts/consumed; không thêm bảng token để phục vụ demo. Không tiết lộ email có tồn tại; chống gửi lặp/thử mã; password policy thống nhất và thu hồi phiên khi reset. Chốt link callback tới FE từ cấu hình. P1-05/06/12; không dùng demo mailbox làm email thật.
- **Student thực hiện:** Student 1; Student 6 phối hợp test/cấu hình mail.

## C03 — Hồ sơ cá nhân

- **Tên màn hình (nêu rõ endpoints):** `/profile` (route hiện tại, không phải `/user/profile`).
- **API chi tiết cần thêm:** **THIẾU** GET `/api/me`; PATCH `/api/me`. Input chỉ các trường hồ sơ cho phép; trả User projection canonical sau cập nhật. Caller `profileService.get/update`.
- **Các constraint, field mà BE cần sản xuất:** `id,fullName,email,phone,dateOfBirth,gender,status,roles` nếu có quyền đọc; nullability liên hệ rõ. Chốt trường nào readonly và luồng xác minh khi đổi email/phone. Không cho tự đổi role/status/owner identity. P1-07/10, BR-03; USERS là hồ sơ duy nhất, không tạo hồ sơ Tenant trùng.
- **Student thực hiện:** Student 1; Student 6 phối hợp xác nhận không ghi đè tenant_snapshot lịch sử.

## C04 — Tìm phòng, chi tiết và khả dụng công khai

- **Tên màn hình (nêu rõ endpoints):** `/`, `/rooms`, `/rooms/:roomId`; hai form tìm kiếm Home/RoomSearch.
- **API chi tiết cần thêm:** **MỞ RỘNG** GET `/api/v1/public/rooms` hiện có: cân nhắc `rentalMode`, khả dụng theo ngày, khu vực có định nghĩa và sort diện tích. **THIẾU** GET `/api/v1/public/room-types`; GET `/api/v1/public/rooms/{id}/availability?startDate&endDate`. GET room list/detail đã nối và không cần facade mới.
- **Các constraint, field mà BE cần sản xuất:** Public wrapper hiện `{room,branchId,branchName,address,roomTypeName}`; room có `id,branchId,roomTypeId,code,rentalMode,capacity,areaSqm,floorNumber,referencePrice,description,amenities,imageRefs,operationalStatus`. Availability cần `roomId,startDate,endDate,available,reason,slots[{id,label,available,reason}]` hoặc DTO khác được thống nhất; thêm số held/occupied/available chỉ khi tính được. Không suy AVAILABLE là còn trống, không lọc sau phân trang làm sai total. P1-21–30/37–42, BR-04/06. Quyền GET Guest đã được duyệt; không mở API quản trị cùng wildcard.
- **Student thực hiện:** Student 2 chủ trì; Student 1 Branch/public scope; Student 3/6 cung cấp allocation/occupancy.

## C05 — Chi nhánh công khai và chính sách công khai

- **Tên màn hình (nêu rõ endpoints):** `/branches/:branchId`; liên kết chi nhánh từ `/rooms/:roomId`; form Appointment/Booking đọc policy.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/v1/public/branches/{id}` và GET `/api/v1/public/branches/{id}/policy` theo caller `catalogService.branch/policy`. Nếu tái sử dụng API Branch khác, cần adapter và quyền public rõ ràng.
- **Các constraint, field mà BE cần sản xuất:** Branch: `id,name,address,description,images,publicPhone,publicEmail,latitude,longitude,rules` hoặc structured `publicContact` được chốt. Policy: `id,branchId,version,effectiveFrom,bookingHoldAmount,paymentWindowMinutes,temporaryHoldMinutes,confirmedHoldMinutes,cancellationTerms,appointmentPolicy`. Chỉ public fields; không lộ tenant identities, private QR hoặc tenant-only Zalo. Không trả hardcoded hold/deadline khi policy chưa tồn tại. P1-14/37/39/50/55, BR-04/08/15.
- **Student thực hiện:** Student 1 chủ trì Branch/policy; Student 2 public read; Student 3 và Student 5 xác nhận snapshot Booking/Payment.

## C06 — Đặt lịch xem phòng và lịch hẹn của User

- **Tên màn hình (nêu rõ endpoints):** `/rooms/:roomId/appointments/new`, `/user/appointments`, `/user/appointments/:appointmentId`, phần lịch hẹn `/user/dashboard`.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/appointments`; GET `/api/appointments/{id}`; POST `/api/appointments/{id}/cancel`. BE hiện mới có Appointment service, không có controller public/User đầy đủ. Demo-response không phải API cần sản xuất.
- **Các constraint, field mà BE cần sản xuất:** Input `roomId,startAt,endAt,note`; requester/assigned manager do BE xác định. Read `id,requesterUserId,roomId,roomTitle,branchName,startAt,endAt,note,status,assignedManager,policySnapshot,createdAt,timeline[{status,at,note}]`, field không có dữ liệu phải nullable/đổi UI. D11 cho phép xem nhóm theo policy, không chặn mọi lịch trùng giờ một cách mặc định; thời gian, timezone, thời lượng, phạm vi người đặt/hủy cần test. P1-43–48.
- **Student thực hiện:** Student 2; Student 1 scope; Student 4 thông báo lịch; Student 6 kiểm thử.

## C07 — Yêu cầu Booking của User

- **Tên màn hình (nêu rõ endpoints):** `/rooms/:roomId/bookings/new`, `/user/bookings`, `/user/bookings/:bookingId`, phần Booking `/user/dashboard`.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/bookings`, GET `/api/bookings/{id}`, POST `/api/bookings/{id}/cancel`. Caller bookingService; input `roomId,roomSlotId?,startDate,endDate` theo use case, không gửi trạng thái xác nhận.
- **Các constraint, field mà BE cần sản xuất:** `id,requesterUserId,branchId,branchName,roomId,roomTitle,roomSlotId,slotLabel,rentalMode,startDate,endDate,holdAmount,paymentDueAt,temporaryHoldUntil,confirmedHoldUntil,status,policySnapshot,paymentEnvironment,createdAt,timeline,contractId`. Giá giữ chỗ/deadline/snapshot do BE; chỉ CONFIRMED sau đủ Payment VERIFIED đúng môi trường, chống chiếm chỗ trùng và hết hạn nguyên tử. P1-49–60, BR-06–08. Không buộc có Appointment trước Booking.
- **Student thực hiện:** Student 3; Student 2 availability; Student 5 verified hold; Student 4 notification.

## C08 — Thanh toán Booking Hold

- **Tên màn hình (nêu rõ endpoints):** `/user/bookings/:bookingId/payment`, thông tin tiền trên chi tiết Booking User/Manager.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/bookings/{id}/payments` theo bookingService. POST thực là tạo chỉ dẫn/yêu cầu nhận chứng cứ theo phương thức được chọn; không nhận `scenario/outcome` của mock như quyền xác minh. Callback Sandbox cần contract BE riêng, không phải màn hình FE.
- **Các constraint, field mà BE cần sản xuất:** `id,bookingId,amount,reference,status,environment,createdAt,verifiedAt,qrUrl,instructions`, chứng cứ/idempotency khi có command. QR là cấu hình Branch hoặc URL provider thật; Payment không đồng nghĩa đã thu. Một Payment tối đa một đích trực tiếp; tách Hold khỏi PREPAID_RENT; tiền muộn không hồi sinh Booking. P1-52–56/96–106, BR-08/14/15. Map REAL/SANDBOX như constraint chung.
- **Student thực hiện:** Student 5 chủ trì Payment; Student 3 consume kết quả Hold; Student 1 cấu hình QR Branch.

## C09 — Chọn Branch trong workspace Manager/Owner

- **Tên màn hình (nêu rõ endpoints):** Toàn bộ `/manager/*`, kể cả vận hành Owner qua `?branchId=...&workspace=owner`.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/manager/branches` theo managerApi.branches, hoặc reuse read scope BE được chốt. FE không thể thay bằng `/api/users` hay danh sách Branch demo.
- **Các constraint, field mà BE cần sản xuất:** `id,name,address,status,ownerId,managerId,managerName,zaloUrl,zaloEnabled` và capability/version nếu thực có; contact riêng của Manager thuộc C27. Field UI cần nhưng không có phải nullable/readonly. Chỉ Branch được phân công hiện tại hoặc do Owner sở hữu; chuyển/hủy phân công thu hồi quyền ngay. Không tin `workspace=owner` hoặc branchId URL để cấp quyền. P1-13–20, BR-02/03.
- **Student thực hiện:** Student 1; Student 2 phối hợp read context; owner từng module dùng chung scope.

## C10 — Room/RoomType/Slot quản trị

- **Tên màn hình (nêu rõ endpoints):** `/manager/rooms`, `/manager/rooms/new`, `/manager/rooms/:roomId`, `/manager/rooms/:roomId/edit`, `/manager/rooms/:roomId/slots`, `/manager/room-types`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**, BE đã có GET/POST `/api/v1/rooms`, GET/PUT/DELETE `/api/v1/rooms/{id}`; GET/POST `/api/v1/room-types`, GET/PUT/DELETE `/api/v1/room-types/{id}`; GET `/api/v1/rooms/{roomId}/slots`, POST `/api/v1/room-slots`, GET/PUT/DELETE `/api/v1/room-slots/{id}`. Ưu tiên sửa FE dùng các API này thay vì tạo lại `/api/manager/branches/{branchId}/rooms|room-types|slots`.
- **Các constraint, field mà BE cần sản xuất:** Room fields C04 + `isPublic,createdAt,updatedAt,version`; Type `id,branchId,code,name,defaultCapacity,description,active`; Slot `id,roomId,code,name,status`. Map FE `roomCode/typeName/slotCode/slotName`, `ACTIVE` và BE `AVAILABLE` theo từng domain; không đổi nghĩa enum. Chốt danh sách Type theo Branch có phân trang, không fetch 1000 rồi lọc sai total. Chống trùng code, vượt capacity và thay đổi tài nguyên đang có nghĩa vụ; BE enforce Owner/Manager scope. P1-21–30.
- **Student thực hiện:** Student 2; Student 1 quyền; Student 3/6 điều kiện allocation/cư trú.

## C11 — Danh mục Tenant và chi tiết quan hệ thuê cho Manager

- **Tên màn hình (nêu rõ endpoints):** `/manager/tenants`, `/manager/tenancies/:contractTenantId`, chọn Tenant trong Invoice/Contract.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: FE đang GET `/api/manager/branches/{branchId}/tenants` và `/{contractTenantId}`; BE có GET `/api/tenants`, GET `/api/tenants/me`, GET `/api/tenants/{contractTenantId}/occupancies`. Cần read projection đúng quan hệ hoặc đổi FE, không coi danh mục User là ContractTenant.
- **Các constraint, field mà BE cần sản xuất:** `contractTenantId,tenantUserId,contractId,branchId,roomId,roomSlotId,fullName,contact` theo quyền, ngày vào/ra dự kiến/thực tế, representative/member, trạng thái Contract/quan hệ/cư trú, lịch sử Occupancy. Snapshot lúc ký không ghi đè profile chung. Một User có thể thuê nhiều Owner. Không trả thông tin riêng của hợp đồng khác. P1-31–36, BR-03.
- **Student thực hiện:** Student 6 chủ trì read/occupancy; Student 3 quan hệ ContractTenant; Student 1 profile/quyền.

## C12 — Quản lý lịch hẹn và Booking

- **Tên màn hình (nêu rõ endpoints):** `/manager/appointments`, `/manager/appointments/:appointmentId`, `/manager/bookings`, `/manager/bookings/:bookingId`.
- **API chi tiết cần thêm:** **THIẾU** list/detail scoped theo prefix FE `/api/manager/branches/{branchId}/appointments` và `/bookings`; POST `appointments/{id}` để status/đổi lịch và POST `bookings/{id}` để confirm/reject/cancel. Có thể dùng canonical APIs C06/C07 với scope server, không bắt giữ prefix. `bookings/{id}/convert` thuộc C13.
- **Các constraint, field mà BE cần sản xuất:** DTO C06/C07 thêm contact requester được phép, timeline, lý do và capability thao tác; command không tin requester/branch từ body. Policy D11 xem nhóm, trạng thái hợp lệ, thanh toán verified/expiry và tài nguyên khi confirm Booking phải được recheck. Không cho Manager tự xác minh Hold bằng đổi status Booking. P1-43–60.
- **Student thực hiện:** Student 2 Appointment; Student 3 Booking; Student 5 kết quả Payment; Student 1 scope, Student 4 sự kiện.

## C13 — Contract và chuyển Booking thành Contract

- **Tên màn hình (nêu rõ endpoints):** `/manager/contracts`, `/manager/contracts/new`, `/manager/contracts/:contractId`, `/manager/contracts/:contractId/edit`, chuyển Booking trên `/manager/bookings/:bookingId`.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/manager/branches/{branchId}/contracts`, GET/PATCH `contracts/{id}`; POST `contracts/{id}/activate`, `/cancel`, `/end`; POST `bookings/{id}/convert`. Đây là đường FE dự kiến, có thể đổi sang canonical leasing controller khi chốt.
- **Các constraint, field mà BE cần sản xuất:** Contract `id,code,branchId,roomId,startDate,endDate,terms,status,documentStatus,representative,participants` và version/capability nếu có. Convert idempotent, Contract chính theo Room, nhiều Booking có thể thêm thành viên, không thu Hold lần hai. RENT/first/last không mặc định bằng giá Room; các quyết định OPEN trong contract-policies phải chốt trước activation/invoice. PDF/OTP là Phase 3, document nullable ở P1. P1-57–67, D12–15.
- **Student thực hiện:** Student 3; Student 5 prepaid/financial constraints; Student 2/6 allocation/occupancy.

## C14 — Thành viên hợp đồng và eligible users

- **Tên màn hình (nêu rõ endpoints):** `/manager/contracts/:contractId/tenants`, editor ContractTenant và chi tiết quan hệ thuê.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/manager/branches/{branchId}/eligible-users`; list/detail quan hệ ở C11; dưới prefix Branch hiện có FE POST `tenants`, PATCH `tenants/{contractTenantId}`, POST `tenants/{id}/activate`, POST `tenants/{id}/representative`. Canonical commands có thể khác sau khi chốt; không dùng CRUD User để sửa nghĩa vụ thuê. Leave/transfer là nghiệp vụ nâng cao cần quyết định riêng, không khẳng định đang có nút/caller tương ứng.
- **Các constraint, field mà BE cần sản xuất:** `tenantUserId,contractId,roomSlotId,role,startDate,endDate,actualLeaveDate,agreed rent/terms,prepaidFirstRequired,prepaidLastRequired,status` theo DTO nghiệp vụ được duyệt. Đại diện không sở hữu nợ của thành viên; không chiếm tài khoản khác khi mời; không vượt capacity/overlap và không đổi nghĩa vụ đã phát sinh tiền một cách trực tiếp. Transfer/rời sớm có phần P2, không coi rule demo là đã chốt. P1-09/31–36/61–66, BE-02.6/03.8/06.6.
- **Student thực hiện:** Student 3 chủ trì member lifecycle; Student 6 occupancy; Student 1 định danh/mời; Student 5 nghĩa vụ đã thu.

## C15 — Furniture, bảo hành và bàn giao

- **Tên màn hình (nêu rõ endpoints):** `/manager/furnitures`, `/manager/furnitures/:furnitureId`; tài sản hiển thị trong `/tenant/my-room`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: reuse GET/POST `/api/furnitures`, GET/PUT `/api/furnitures/{id}`, GET/POST `/api/furnitures/{id}/warranties`, PUT/DELETE `/api/furnitures/{id}/warranties/{warrantyId}`. GET/POST `/api/contract-tenants/{contractTenantId}/handover` đã có nếu luồng bàn giao được dùng. FE còn POST `/api/manager/branches/{branchId}/furnitures/{id}/warranty-save`, POST `/api/manager/branches/{branchId}/furnitures/{id}/warranty-delete` và DELETE catalog resource; map sang canonical warranty/status commands sau khi chốt thay vì tạo API trùng.
- **Các constraint, field mà BE cần sản xuất:** `id,roomId,assetCode,name,furnitureType,serialNumber,purchaseDate,assetValue,condition,status`, warranties đúng date/provider/terms, handover đúng member. UI `roomSlotId` chưa được entity Furniture biểu diễn; không tự thêm FK hay coi tài sản theo Room là tài sản riêng Slot. Map status RETIRED/DAMAGED theo BE thay vì ép ACTIVE/INACTIVE. Security adapter phải hoạt động; bảo hành hết hạn và open repair không được làm mất lịch sử. P1-107–110, BE-06.3/06.7.
- **Student thực hiện:** Student 6; Student 1 security; Student 3 xác nhận handover/member.

## C16 — Danh mục và cấu hình dịch vụ

- **Tên màn hình (nêu rõ endpoints):** `/manager/services`, `/manager/room-services`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: GET/POST `/api/branches/{branchId}/services`, GET/PUT `/api/services/{serviceId}`, GET `/api/rooms/{roomId}/services`, POST `/api/branches/{branchId}/room-services`, PUT `/api/room-services/{id}` đã có. Thiếu projection list theo Branch nếu UI cần quản lý nhiều Room một lần; ưu tiên filter/page thực thay vì nối nhiều danh sách thiếu total.
- **Các constraint, field mà BE cần sản xuất:** Catalog `id,branchId,code,name,unit,serviceType,billingMethod,referencePrice,mandatory,active`; RoomService `id,roomId,serviceId,serviceName,startDate,endDate,unitPrice,billingConfig,status`. Điện/nước mặc định, đơn giá theo hiệu lực, không đổi Invoice snapshot cũ. Request page Services hiện từ 0; sửa wrapper gửi 1 và normalize sai, hoặc thống nhất BE có migration contract. BE cần Branch scope ngoài role. P1-68–70, BR-09/12.
- **Student thực hiện:** Student 4; Student 1 scope; Student 2 cung cấp Room/Branch read context.

## C17 — Đăng ký/hủy/duyệt dịch vụ Tenant

- **Tên màn hình (nêu rõ endpoints):** `/tenant/services`, `/manager/service-requests`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: BE đã có GET `/api/contracts/{contractId}/services`, POST `/api/contract-services/register`, PUT `/api/contract-services/{id}/cancel`, PUT `/{id}/process`. FE cũ GET/POST `/api/tenant/tenancies/{contractTenantId}/services`, POST `services/{serviceId}/cancel` và branch service-requests cần read projection/adapter; phân biệt serviceId và subscriptionId.
- **Các constraint, field mà BE cần sản xuất:** `contractId,contractTenantId,roomServiceId,serviceName,unit,mandatory,agreedUnitPrice,startDate,endDate,status,reason,requestedBy,processedBy,processedAt` và history nếu hiển thị. Actor từ AuthPrincipal, không UUID hệ thống; giá do BE, Tenant không gửi giá để tự được duyệt. Không hủy điện/nước mandatory; cancellation là request trước khi manager duyệt. P1-71–73, BR-03/09/10/12.
- **Student thực hiện:** Student 4; Student 1 actor/scope; Student 3/6 xác thực membership.

## C18 — Meter, charge và nhiều segment

- **Tên màn hình (nêu rõ endpoints):** `/manager/utility-charges`, `/manager/utility-charges/new`, `/manager/utility-charges/:chargeId`, `/manager/utility-charges/:chargeId/edit`, `/tenant/utilities`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: POST `/api/service-charges`, PUT `/api/service-charges/finalize`, GET `/api/service-charges/{id}`, GET `/api/service-charges/{id}/detail`, GET `/api/rooms/{roomId}/service-charges` đã có. FE còn cần GET `/api/manager/branches/{branchId}/utility-charges`, GET `/api/manager/branches/{branchId}/meters`, POST `/api/manager/branches/{branchId}/charge-preview`, POST `/api/manager/branches/{branchId}/utility-charges/{id}` cho sửa draft theo caller hiện tại; tên canonical cần chốt. Void là quyết định nghiệp vụ bổ sung, không khẳng định đã có nút UI. Nếu ghi nhiều segment, phải có hợp đồng transaction/batch hoặc FE chỉ cho command đơn segment được duyệt.
- **Các constraint, field mà BE cần sản xuất:** `roomServiceId,billingMonth,startDate,endDate,segmentNumber,meterReference,segmentReason,startReading,endReading,quantity,unitPrice,amount,status,finalizedAt` + chi tiết nguồn. Tenant read cần `totalAmount,myAmount,segments,evidence,approved snapshot,invoiceId` và participants được ẩn danh. Không bỏ segment sau index 0, không tự tính lại phí finalized để khớp UI. Giá/range/readings/reset/midmonth đúng nghiệp vụ. P1-74–78/82–84, BR-11/12.
- **Student thực hiện:** Student 4; Student 6 resident-days; Student 5 tiêu thụ nguồn phí chốt.

## C19 — Đề xuất phân bổ chi phí

- **Tên màn hình (nêu rõ endpoints):** `/tenant/utilities/allocations`, `/manager/cost-allocations`, phần chia phí trong chi tiết charge.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: POST `/api/cost-allocations/propose`, PUT `/api/cost-allocations/{id}/process`, GET `/{id}`, GET `/api/service-charges/{chargeId}/allocations`, GET `/{chargeId}/default-allocation` đã có. FE còn thiếu list theo branch/quan hệ thuê; không tải một charge rồi giả là toàn bộ list.
- **Các constraint, field mà BE cần sản xuất:** Chốt `roomServiceChargeId,method,allocationDetails,reason` so với FE `chargeId,shares[{id,amount}]`. Enum method **đã khớp** `RESIDENT_DAYS | APPROVED_CUSTOM`; khác biệt là cấu trúc payload/IDs, không cần đổi enum chỉ để nối. Response cần proposal ID, charge ID, **contractTenantId thực**, proposedBy User ID, shares/day inputs, trạng thái/timestamps/audit. Không gán proposedBy thành ContractTenant ID hoặc tự thêm createdAt. Tổng shares bằng phí gốc sau làm tròn; chỉ người liên quan đề xuất, Owner/Manager đúng scope duyệt, không tự sửa phí đã lên hóa đơn. P1-79–84, BR-11/12.
- **Student thực hiện:** Student 4; Student 6 lịch cư trú; Student 1 actor/scope; Student 5 invoice snapshot.

## C20 — Maintenance của Manager và Tenant

- **Tên màn hình (nêu rõ endpoints):** `/manager/maintenance`, `/manager/maintenance/:reportId`, `/tenant/maintenance`, `/tenant/maintenance/new`, `/tenant/maintenance/:reportId`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: reuse POST/GET `/api/maintenance-requests`, GET `/my`, GET `/{requestId}`, POST `/{id}/accept`, `/reject`, `/start`, `/complete`, `/assign`, `/evidence`, `/records`, `/records/{recordId}/approve`, `/records/{recordId}/reject`, `/records/{costRecordId}/liabilities`; POST `/{id}/rating` và GET `/cost-stats` thuộc phần nâng cao. FE đang gọi branch maintenance và `/api/tenant/tenancies/{id}/maintenance`; đổi adapter/projection sau khi security hoạt động. BE chưa có cancel command tương ứng nút Tenant cancel.
- **Các constraint, field mà BE cần sản xuất:** Read `id,roomId,contractTenantId/requesterUserId,title,description,status,assignee,items,furniture/service refs,evidence,records,costs,liabilities,approval timestamps,timeline`; creation phải biểu diễn nhiều item nếu nghiệp vụ hỗ trợ. FE command `record`, `cost-approve`, `cost-reject` phải map tới `/records` và đúng recordId ở BE. Chốt enum priority/category UI với DTO BE, không bỏ field im lặng. Không tự thêm cancel khi đặc tả Student6 chưa chốt; FE bỏ/khóa nút hoặc duyệt rule mới. Owner/Tenant liability được duyệt trước nguồn REPAIR vào Invoice; hoàn tất không đồng nghĩa thu tiền. P1-111–119, BR-16/17.
- **Student thực hiện:** Student 6; Student 1 security; Student 4 notification; Student 5 nguồn phí sửa chữa được duyệt.

## C21 — Lập, xem và phát hành Invoice

- **Tên màn hình (nêu rõ endpoints):** `/manager/invoices`, `/manager/invoices/new`, `/manager/invoices/:invoiceId`, `/manager/invoices/:invoiceId/edit`.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/manager/branches/{branchId}/invoices`, GET/PATCH `invoices/{id}`, POST `invoices/{id}/issue`, `/cancel`; GET `invoices/{id}/editor`; GET `/api/manager/branches/{branchId}/invoice-sources?contractTenantId&month&invoiceId`. Canonical billing controller có thể thay các URL này nếu sửa FE đồng bộ.
- **Các constraint, field mà BE cần sản xuất:** `id,code,contractTenantId,contractId,branchId,billingMonth,dueDate,documentStatus,issuedAt,items[{id,type,label,quantity,unitPrice,amount,sourceRef,snapshot}],totalAmount,prepaidApplied,verifiedAllocated,outstanding,availableToPay,financialStatus`; editor/source response cần nguồn khả dụng và lý do không dùng được. Một Invoice hiệu lực/member/tháng, dueDate từ policy snapshot, tổng khớp dòng, chỉ ISSUED mới tạo công nợ. RENT từ thỏa thuận/lịch leasing, không lấy Room.referencePrice; PREPAID_APPLICATION không cộng phí hay tạo thu mới. P1-85–93; D12/D22/D26; contract-policies còn OPEN phải giải quyết trước phần phụ thuộc.
- **Student thực hiện:** Student 5 chủ trì command/amount; Student 2 read projection; Student 3 RENT; Student 4 SERVICE; Student 6 REPAIR; Student 1 quyền.

## C22 — Thanh toán, xác minh và đối soát của Manager

- **Tên màn hình (nêu rõ endpoints):** `/manager/payments`, `/manager/payments/:paymentId`, nhận thanh toán từ chi tiết Invoice/Booking.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/manager/branches/{branchId}/payments`, GET `payments/{id}`, POST `payments/{id}` body gồm `decision: review | verify`, `note/reason` và field cần xác minh theo dialog hiện tại. Nếu BE chọn command `/verify`, `/review` riêng thì sửa caller; `/reject`, match/refund là đề xuất cần chốt nghiệp vụ/phase, không khẳng định đã có nút UI. Không dùng DELETE Payment để sửa sai.
- **Các constraint, field mà BE cần sản xuất:** `paymentId,branchId,invoiceId?,bookingId?,contractTenantId?,payerUserId/payerName,beneficiary,amountReceived,allocatedAmount,unmatchedAmount,method,environment,reference,providerTransactionId,status,receivedAt,verifiedAt,evidence,reviewReason,audit`. Một đích trực tiếp Invoice hoặc Booking; verify phải recheck scope/reference/amount/environment, chống lặp, transaction nguyên tử. PENDING/REVIEW không giảm nợ; tiền dư/chưa khớp nằm ở Payment, không ví. P1-99–106, BR-13–15; refund nâng cao P2-27 cần duyệt.
- **Student thực hiện:** Student 5; Student 1 scope/QR; Student 3 Booking transition; Student 2 báo cáo read.

## C23 — Công nợ Manager

- **Tên màn hình (nêu rõ endpoints):** `/manager/debts`, danh sách nợ trong Invoice/ContractTenant detail.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/manager/branches/{branchId}/debts` với page/filter theo member/kỳ/trạng thái; có thể reuse read Invoice/tenant aggregate nếu trả đủ dữ liệu và total chính xác.
- **Các constraint, field mà BE cần sản xuất:** `contractTenantId,tenantName,contractId,roomId,billed,prepaidApplied,verifiedAllocated,outstanding,overdue,oldestDueDate,invoices` theo quyền. Phân biệt người trả và người chịu nợ; chỉ trừ tiền verified đã phân bổ; không gộp nghĩa vụ Roommates; bucket tuổi nợ Phase2 phải ghi rõ tiêu chí. P1-89–93/104–106, BE-02.5, P2-25.
- **Student thực hiện:** Student 2 chủ trì read API; Student 5 chuẩn số tiền/công nợ; Student 6 directory và Student 1 scope.

## C24 — Dashboard và báo cáo vận hành

- **Tên màn hình (nêu rõ endpoints):** `/manager/dashboard`, `/manager/reports`, `/owner/dashboard`, `/owner/finance`, `/owner/reports`, `/user/dashboard`, `/tenant/dashboard`.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/manager/branches/{branchId}/summary?month` và GET `/api/owner/summary?month&branchId` theo caller. User dashboard có thể tổng hợp API C06/C07/Notifications; Tenant dashboard tổng hợp C26–C29; không bắt thêm endpoint dashboard nếu không cần. Read report nâng cao cần chốt range/export riêng theo phase.
- **Các constraint, field mà BE cần sản xuất:** ManagerSummary hiện cần `period,rooms{total,available,occupied,held,maintenance},slots{total,occupied,held,available},billed,receipts,sandboxReceipts,debt,overdue,overdueCount,activeContracts,activeTenancies,pending,series,bookingCounts,maintenanceCounts`; Owner thêm rows theo Branch, totals và `prepaidReceived,prepaidApplied,prepaidUnused,holdReceived,holdSandbox,unmatched,exceptions,invoiceCount,bookingCount,maintenanceCount`, thông tin Branch/Manager và slots.unavailable khi thực sự có dữ liệu. Mỗi chỉ số cần định nghĩa nguồn/thời điểm/timezone; thu thật tách Sandbox, Hold và prepaid application, không đếm tiền hai lần. Scope server, không return số 0 khi chưa có nguồn. BE-02.5, BE-01.7, P1-128–131; P2-34 phần nâng cao.
- **Student thực hiện:** Student 2 read/dashboard; Student 1 Owner report/scope; Student 5 định nghĩa tài chính; Students 3/4/6 cung cấp read DTO nguồn.

## C25 — Zalo riêng cho người thuê

- **Tên màn hình (nêu rõ endpoints):** `/manager/settings/zalo`, liên kết nhóm trong `/tenant/my-room`.
- **API chi tiết cần thêm:** **THIẾU** PUT `/api/manager/branches/{branchId}/settings/zalo` body `url/enabled` cần thống nhất field chính xác; read qua Branch authorized và C27.
- **Các constraint, field mà BE cần sản xuất:** `branchId,zaloGroupUrl,zaloEnabled`, capability/version nếu dùng concurrency. URL hợp lệ và bật/tắt do Owner/Manager đúng Branch; chỉ Tenant có quan hệ ACTIVE được thấy link. Không đưa link vào API Branch public hoặc notification không đúng recipient. P1-120–122, BE-03.6; không đồng nhất liên hệ công khai với tài nguyên nhóm riêng.
- **Student thực hiện:** Student 3; Student 1 quyền Branch; Student 6 xác nhận ACTIVE membership.

## C26 — Chọn quan hệ thuê Tenant

- **Tên màn hình (nêu rõ endpoints):** Layout `/tenant/*`, `/tenant/dashboard`, context được chọn bằng `tenancyId`.
- **API chi tiết cần thêm:** **THIẾU/LỆCH** GET `/api/tenant/tenancies` đang được tenantApi.tenancies gọi. BE GET `/api/tenants/me` hiện không bảo đảm cùng shape/meaning; ưu tiên DTO read chung rồi adapter, không gán User ID thành tenancy ID.
- **Các constraint, field mà BE cần sản xuất:** Tenancy read `id=contractTenantId,userId,contractId,branchId,roomId,roomSlotId,branchName,roomName,slotName,rentalMode,status,contractStatus,code,role,startDate,endDate,actualLeaveDate,monthlyRent,firstMonthRent,lastMonthRent,prepaidFirstRequired,prepaidLastRequired,terms,sharedTerms,document?`. Đây là nhu cầu UI, các giá/terms chưa có phải sửa UI hoặc bổ sung nguồn leasing được duyệt; không parse text thành số. Chỉ quan hệ của caller, gồm lịch sử được phép đọc; không có tenancy tự cấp từ role TENANT. P1-31–36/61–66, BR-03.
- **Student thực hiện:** Student 6 read context; Student 3 thỏa thuận/member; Student 5 nghĩa vụ tài chính; Student 1 danh tính.

## C27 — Phòng riêng và hợp đồng của Tenant

- **Tên màn hình (nêu rõ endpoints):** `/tenant/my-room`, `/tenant/contracts`, `/tenant/contracts/:contractId`, `/tenant/contracts/:contractId/my-terms`.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/tenant/tenancies/{contractTenantId}/room`; GET `/api/tenant/tenancies/{contractTenantId}/contracts/{contractId}`. List contract hiện có thể dùng context C26, không bắt tạo endpoint list trùng nếu không cần.
- **Các constraint, field mà BE cần sản xuất:** C26 + Room view `images,area,floor,roomType,amenities,rules,manager{name,phone},zaloUrl,assets[{id,name,category,condition}]`. Chỉ common contract và điều khoản của chính member; không trả tiền riêng của bạn cùng phòng. Document nullable P1, không dựng PDF. Contact Manager theo scope thật; Zalo theo C25; handover theo C15. P1-31–36/61–67/120–122.
- **Student thực hiện:** Student 3 Contract/terms; Student 6 room/occupancy/assets projection; Student 1 contact/scope.

## C28 — Hóa đơn và thanh toán Tenant

- **Tên màn hình (nêu rõ endpoints):** `/tenant/invoices`, `/tenant/invoices/:invoiceId`, `/tenant/payments`, trả trước trong `/tenant/contracts/:contractId/my-terms`.
- **API chi tiết cần thêm:** **THIẾU** prefix `/api/tenant/tenancies/{contractTenantId}`: GET `/invoices`, GET `/invoices/{invoiceId}`, GET `/payments`, GET `/payments/{paymentId}`, POST `/invoices/{invoiceId}/payments`, POST `/invoices/{invoiceId}/manual-payments`, POST `/prepaid-payments`. Chọn QR thủ công hoặc Sandbox cho P1; không ép làm cả hai vì UI cũ có hai luồng.
- **Các constraint, field mà BE cần sản xuất:** Invoice C21 + `financialStatus,prepaidSourcePaymentId,manualQr{qrUrl,resourceId,reference}?,instructions{environment,reference,qrUrl,text}`; Payment C22 + FIRST/LAST received/applied buckets. POST amount/payer/evidence/idempotency chỉ tạo trạng thái chờ kiểm tra, không nhận outcome tự khai. Chốt quyền thu nợ quan hệ đã LEFT/ENDED và reservation `availableToPay`, không tự áp rule demo. PREPAID_RENT áp vào RENT không tạo Payment nhận tiền mới; sandbox tách REAL. P1-85–106, BR-13–15, contract-policies.
- **Student thực hiện:** Student 5; Student 2 read Invoice; Student 3 first/last terms; Student 1 QR; Student 6 membership.

## C29 — Projection Services/Utilities/Allocation/Maintenance của Tenant

- **Tên màn hình (nêu rõ endpoints):** `/tenant/services`, `/tenant/utilities`, `/tenant/utilities/allocations`, `/tenant/maintenance`, `/tenant/maintenance/new`, `/tenant/maintenance/:reportId`.
- **API chi tiết cần thêm:** **THIẾU/LỆCH** prefix `/api/tenant/tenancies/{id}`: GET `/services`, GET `/utilities`, GET `/allocations`, GET `/maintenance`, GET `/maintenance/{reportId}`; POST `/services`, POST `/services/{serviceId}/cancel`, POST `/allocations`, POST `/maintenance`, POST `/maintenance/{reportId}/cancel` là URL projection/command FE cũ. Reuse canonical C17–C20 khi đủ dữ liệu và quyền; cancel Maintenance chưa là nghiệp vụ được duyệt.
- **Các constraint, field mà BE cần sản xuất:** Service fields C17; utility `id,contractTenantId,name,billingMonth,startDate,endDate,totalAmount,myAmount,invoiceId,snapshot,evidence,segments,participants`; allocation C19; maintenance C20. Phải kiểm tra caller→member→resource, ẩn danh participants, không lộ nợ/giấy tờ người khác. List hiện là array/UI paginate local, cần chuyển production sang server page/filter với adapter rõ ràng, không đổi shape ngầm. P1-71–84/111–119 và BR-10/11/16/17.
- **Student thực hiện:** Student 4 Services/Utilities/Allocation; Student 6 Maintenance/projection; Student 3 member; Student 1 scope; Student 5 link Invoice.

## C30 — Owner onboarding và hồ sơ xác minh

- **Tên màn hình (nêu rõ endpoints):** `/owner-onboarding`, `/owner/onboarding`, `/owner/verification`.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/owner/verification` theo ownerApi.verification/submit; upload/document read theo C38. Có review ADMIN không có nghĩa đã có self-service submission.
- **Các constraint, field mà BE cần sản xuất:** `userId,status,fullName,phone,contactVerified,submittedAt,reviewedAt,reviewedBy,reason,document{name,mime,size,resourceId},eligible`. Đọc/ghi chính người dùng, lưu xác minh hiện hành trên USERS; giấy tờ Cloudinary có kiểm soát truy cập; không tự cấp OWNER khi đăng ký. P1-01/08/09, BE-06.1. P2 nhiều bước/eKYC P3 không tự thành bắt buộc.
- **Student thực hiện:** Student 1 onboarding/role và upload auth; Student 6 review lifecycle.

## C31 — Owner Branch, policy và QR

- **Tên màn hình (nêu rõ endpoints):** `/owner/branches`, `/owner/branches/new`, `/owner/branches/:branchId`, `/owner/branches/:branchId/edit`, `/owner/branches/:branchId/policies`, `/owner/branches/:branchId/payments/settings`.
- **API chi tiết cần thêm:** **THIẾU** GET/POST `/api/owner/branches`, GET/PATCH/DELETE `/api/owner/branches/{id}`, GET/POST `/api/owner/branches/{id}/policies`, PUT `/api/owner/branches/{id}/qr`; reuse canonical Branch endpoints nếu được cung cấp sau.
- **Các constraint, field mà BE cần sản xuất:** Branch `id,ownerId,managerId,code,name,address,province,district,ward,latitude,longitude,description,rules,publicContact,images,isPublic,status,qrPublicId,qrUrl,version,assignedAt,roomCount` theo thiết kế thực; map FE publicPhone/publicEmail/rules-array tới BE đúng hợp đồng. DRAFT có thể chưa manager, ACTIVE phải có manager đúng Owner. Policy version/effective/snapshot C05; QR chỉ asset thật, không bank credential. Ngừng/xóa Branch có tài nguyên/nghĩa vụ phải kiểm tra phụ thuộc, không DELETE mất lịch sử. P1-13–20/96–98, D05–09, BR-02/15.
- **Student thực hiện:** Student 1; Student 3 policy Booking/Contract; Student 5 QR reference/finance; Student 2/6 phụ thuộc tài nguyên.

## C32 — Manager invitation và phân công Branch

- **Tên màn hình (nêu rõ endpoints):** `/owner/managers`, `/owner/managers/invite`, `/owner/managers/:managerId`, `/owner/manager-assignments`, `/manager-invitations`.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/owner/managers`, GET/DELETE `/api/owner/managers/{id}`; GET/POST `/api/owner/invitations`, POST `/api/owner/invitations/{id}/cancel`, `/{id}/resend`; GET `/api/owner/my-invitations`, POST `/api/owner/my-invitations/{id}/accept`; PUT `/api/owner/branches/{id}/assignment`.
- **Các constraint, field mà BE cần sản xuất:** Relationship `id,user,assignedAt,branches`; invitation view `id,ownerId,fullName,email,phone,branchIds,note,status,createdAt,expiresAt,acceptedBy`; assignment `managerId,reason,version` nếu có concurrency. View invitation không yêu cầu thêm bảng invitation: lưu trữ theo USERS challenge/thiết kế đã chốt. Chỉ Owner của quan hệ, kiểm tra danh tính người nhận và hạn/cooldown từ cấu hình; nhận lời mời không tự nhận quyền khi chưa xác minh. Một Manager thuộc một Owner hiện tại, có thể quản nhiều Branch; chuyển/hủy quyền hiệu lực ngay; xóa quan hệ khác xóa User. P1-09/15–20, D05–07.
- **Student thực hiện:** Student 1; Student 6 kiểm thử scope/thu hồi quyền.

## C33 — Tài chính, lịch sử và phê duyệt Owner

- **Tên màn hình (nêu rõ endpoints):** `/owner/payments`, `/owner/approvals/invoices`, `/owner/activity`; đề xuất từ chi tiết Invoice ở workspace Manager/Owner; báo cáo C24.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/owner/payments`, GET `/api/owner/approvals`, POST `/api/owner/approvals/{id}`, GET `/api/owner/activity`; POST `/api/manager/branches/{branchId}/adjustment-requests` body `{invoiceId,proposedTotal,reason,evidence}` là đề xuất FE cần đối chiếu nghiệp vụ, không phải lệnh ghi đè total.
- **Các constraint, field mà BE cần sản xuất:** Payment read C22 thêm branchName; approval `id,ownerId,branchId,contractTenantId,invoiceId,billingMonth,original snapshot/proposal,reason,evidence,status,createdAt,decidedAt,decidedBy,version/fingerprint`; activity `id,ownerId,branchId,actorName,type,summary,at,route`. Chỉ Owner thuộc Branch duyệt điều chỉnh; giữ Invoice ISSUED snapshot/audit, không sửa âm thầm lịch sử tiền; phase nâng cao P2-26/27 cần duyệt. Không suy diễn bỏ phiếu proposal là đã thu tiền. P1-93/106/136, D26, BR-12–15.
- **Student thực hiện:** Student 5 Invoice adjustment/Payment; Student 1 Owner scope/audit; Student 2 read/report.

## C34 — User ADMIN và các trường còn thiếu

- **Tên màn hình (nêu rõ endpoints):** `/admin/users`, `/admin/users/new`, `/admin/users/:userId`.
- **API chi tiết cần thêm:** **ĐÃ NỐI** GET/POST `/api/users`, GET `/api/users/{id}`, PATCH `/api/users/{id}/active-status`. **MỞ RỘNG** search/filter có server pagination và read roles/scope/audit nếu giữ UI tương ứng; không tạo một `/api/admin/users` trùng. DELETE `/api/users/{id}` đã có nhưng không được hiểu là ngừng hoạt động hay yêu cầu mở nút xóa ở FE.
- **Các constraint, field mà BE cần sản xuất:** Hiện `id,email,phone,fullName,status,createdAt`; UI còn cần `roles,contactVerified,ownerVerificationStatus,ownerReviewedAt,scope{ownedBranches,assignedBranches,tenancies},canActivate,version` chỉ nếu xác định nguồn/meaning; có thể bỏ field UI không cần thay vì dựng dữ liệu. Create chỉ fullName/email/phone/dateOfBirth/gender, INVITED không tự đăng nhập; active-status để BE kiểm tra hash/xác minh. Mọi quyền ADMIN enforce BE; audit theo whitelist. P1-07–12/134/136.
- **Student thực hiện:** Student 1; Student 6 verification; Student 2 hỗ trợ các count/read có scope nếu cần.

## C35 — ADMIN review Owner

- **Tên màn hình (nêu rõ endpoints):** `/admin/owner-verifications`, `/admin/owner-verifications/:userId`.
- **API chi tiết cần thêm:** **LỆCH/BỊ CHẶN**: BE có GET `/api/admin/owner-verifications`, POST `/{userId}/approve`, POST `/{userId}/reject`. FE hiện GET detail `/{id}`, GET `/{id}/document`, POST `/{id}/decision`. Ưu tiên đổi FE sang approve/reject; chỉ bổ sung detail/document/history read khi UI cần và quyền đã đủ.
- **Các constraint, field mà BE cần sản xuất:** Review `user` projection, trạng thái xác minh/giấy tờ hiện hành, reason, reviewer/reviewedAt, submittedAt, permitted actions và audit/history từ nguồn có thật. Chốt User ID ở path, sort/1-based request của API review, concurrency/version thực. Không trả SVG demo làm giấy tờ; không ghi nội dung giấy tờ/secret vào audit. PendingSecurityOwnerVerificationAccessPolicy phải thay bằng ADMIN guard thật. P1-08/132/134/136, BE-06.1.
- **Student thực hiện:** Student 6 review/DTO; Student 1 quyền/User/Cloudinary; người sửa FE admin đổi command.

## C36 — Các màn hình nền tảng ADMIN

- **Tên màn hình (nêu rõ endpoints):** `/admin/dashboard`, `/admin/roles`, `/admin/owners`, `/admin/branches`, `/admin/activity`, `/admin/activity/:eventId`.
- **API chi tiết cần thêm:** **THIẾU** GET `/api/admin/dashboard`, `/api/admin/roles`, `/api/admin/owners`, `/api/admin/branches`, `/api/admin/activity`, `/api/admin/activity/{id}`. `/api/admin/entitlements` chỉ được mock caller sử dụng; nếu JWT/server role guard đủ thì không cần endpoint này. Không thêm demo/reset vào BE thật.
- **Các constraint, field mà BE cần sản xuất:** Dashboard counters định nghĩa rõ; Role `{code,name/description,permissions}` nếu hệ thống cung cấp; Owner/User read C34; Branch `id,ownerId,managerId,name,status,verification summary`; audit `id,actorUserId,actorName,tableName,entityId,action,reason,requestKey,occurredAt,summary,detail` theo whitelist. Chỉ ADMIN, không expose secrets/giấy tờ hoặc giả lịch sử assignment; read model dùng Service Interface, không cross-module write. P1-10/131–136; nâng cao audit/export theo BE-01.6/01.7.
- **Student thực hiện:** Student 1 role/audit/scope; Student 2 dashboard/read; Student 6 verification/integration.

## C37 — Notification gửi và deep link đúng quan hệ

- **Tên màn hình (nêu rõ endpoints):** `/notifications` và dialog SendNotificationDialog; badge/read inbox trên các layout; deep link tới Tenant/Manager resources.
- **API chi tiết cần thêm:** **ĐÃ NỐI** GET `/api/notifications`, GET `/unread-count`, PUT `/mark-read`, PUT `/mark-all-read`. **BỊ CHẶN** POST `/api/notifications/send`, POST `/send-bulk` đã có nhưng thiếu guard. Cân nhắc mở rộng read DTO hoặc endpoint resolve reference có quyền để chuyển đúng ContractTenant khi bấm link, không bắt thêm type filter nếu bỏ được UI filter.
- **Các constraint, field mà BE cần sản xuất:** Read `id,recipientUserId,eventKey,type,title,message,referenceType,referenceId,read,readAt,createdAt,channel,deliveryStatus`; deep link cần authorized contractTenantId/branchId hoặc server resolution, không mặc định người nhận có quyền theo referenceId. Send body `recipientUserId(s),type,title,message,referenceType?,referenceId?,channel?`; role + recipient scope enforce BE, dedupe theo thiết kế notification. Không dùng nút FE để giả bảo vệ, không cho User tự gửi sự kiện tiền đã verified. P1-123–127/134, BR-03.
- **Student thực hiện:** Student 4 chủ trì; Student 1 quyền; Student 3/5/6 cung cấp tham chiếu và sự kiện đúng domain.

## C38 — File, ảnh, chứng cứ và giấy tờ

- **Tên màn hình (nêu rõ endpoints):** Owner onboarding/verification, Owner Branch/QR, ADMIN verification document, Manager/Tenant Maintenance evidence, Payment evidence, gallery Room.
- **API chi tiết cần thêm:** **CẦN CHỐT HỢP ĐỒNG**, không khẳng định đã có caller endpoint upload chung. Cần cơ chế upload Cloudinary có xác thực và trả asset metadata; chọn direct signed upload hoặc BE upload theo thiết kế hệ thống. Chốt đường ký/xác nhận upload và đọc private document; không bắt tạo `/api/uploads` nếu đã có cơ chế hợp lệ khác. READ document đang được FE yêu cầu ở C35.
- **Các constraint, field mà BE cần sản xuất:** Asset `{publicId/resourceId,url/secureUrl,mime,size,name}` theo loại, owner/scope và expiry khi private; validate MIME/kích thước/số lượng theo requirements được duyệt, không coi limit demo là mặc định hệ thống. Không trả SVG/timestamp/URL placeholder như dữ liệu thật, không commit Cloudinary secrets. Public Room image khác giấy tờ xác minh/private evidence; có permission trước khi trả URL. P1-08/14/96/116 và coding RULE 12.
- **Student thực hiện:** Student 1 cơ chế auth/Cloudinary/Owner/QR; Student 6 maintenance/document review; Student 4/5 thống nhất evidence theo nguồn phí và Payment.

## Các điểm phải chốt trước khi triển khai theo tài liệu này

| Điểm cần chốt | Owner điều phối | Không được suy diễn |
|---|---|---|
| Canonical URLs và facade/read projections | Owner từng nhóm C01–C38 + người sửa FE | Prefix mock hiện tại không phải yêu cầu bắt buộc BE |
| RENT/tháng lẻ/FIRST–LAST, lịch nghĩa vụ, dueDate | Student 3 + Student 5; Student 1 BranchPolicy | Không lấy Room.referencePrice hoặc tự parse terms |
| Khả dụng theo ngày, Appointment nhóm | Student 2 + Student 3 + Student 6 | Không coi AVAILABLE là trống; không cấm mọi lịch trùng giờ |
| Tenant history/LEFT còn nợ, quyền trả nợ sau khi rời | Student 3 + Student 5 + Student 6 | Rule demo khóa mọi thao tác sau LEFT chưa là quyết định hệ thống |
| Batch segment/atomicity và allocationDetails | Student 4 + Student 5 + Student 6 | Không bỏ segment, không tính lại phí finalized |
| Read/write scope, role switch và lời mời | Student 1 + owner từng module | Không cấp quyền từ URL/User ID hay seed account |
| PDF/OTP/eKYC/refund/export nâng cao | Owner theo be_tasks.md và người duyệt phạm vi | Có UI field không tự nâng Phase 2/3 thành bắt buộc Phase 1 |

## Quy trình cập nhật sau mỗi merge

1. Ghi branch/commit đã merge, owner, ngày kiểm tra; đối chiếu router + service + schema FE và controller + service + DTO + policy BE, không chỉ đọc tên commit.
2. Với từng nhóm Cxx, cập nhật API/field nào đã có và test được, API nào còn thiếu, adapter nào cần đổi; giữ lịch sử quyết định thay vì xóa vấn đề chưa giải quyết.
3. Cập nhật audit năm trường, tài liệu API module, business rules/feature sources và migration/config liên quan trong cùng PR. Nếu đổi nghĩa tiền/trạng thái/quyền, owner các domain phụ thuộc cùng review.
4. Bổ sung contract/authorization/validation/state/idempotency tests phù hợp; chạy kiểm tra được ghi trong README hoặc CI. Ghi rõ unit/mock, MockMvc, DB integration hay live test; không dùng unit pass làm bằng chứng deploy hoạt động.
5. Chỉ bật endpoint trong `services/real-api.ts` sau khi kiểm tra DTO và quyền. Nếu BE đổi path/shape đúng nghiệp vụ thì sửa FE và tests, không thêm mock fallback. Không chờ toàn bộ Cxx xong mới được chia sẻ tiến độ vào dev nếu phạm vi đã được nhóm thống nhất và kiểm tra hiện tại đạt.

## Phụ lục đối chiếu toàn bộ màn hình FE

Bảng bên dưới liệt kê route cụ thể từ `src/router.tsx`; route redirect/layout được gộp với nhóm liên quan. Cột Cxx trỏ tới API, field, constraint và Student ở trên. Wildcard 404 không đòi API nghiệp vụ.

Đã đối chiếu **109 route** (gồm các route redirect và wildcard 404).

| Route FE hiện tại | Nhóm hợp đồng |
|---|---|
| `/` | C04 |
| `/*` | Kh?ng c?n API |
| `/admin` | C36 |
| `/admin/activity` | C36 |
| `/admin/activity/:eventId` | C36 |
| `/admin/branches` | C36 |
| `/admin/dashboard` | C36 |
| `/admin/owner-verifications` | C35 |
| `/admin/owner-verifications/:userId` | C35 |
| `/admin/owners` | C36 |
| `/admin/roles` | C36 |
| `/admin/users` | C34 |
| `/admin/users/:userId` | C34 |
| `/admin/users/new` | C34 |
| `/branches/:branchId` | C05 |
| `/forgot-password` | C02 |
| `/login` | C01 |
| `/manager` | C09 |
| `/manager-invitations` | C32 |
| `/manager/appointments` | C12 |
| `/manager/appointments/:appointmentId` | C12 |
| `/manager/bookings` | C12 |
| `/manager/bookings/:bookingId` | C12 |
| `/manager/contracts` | C13 |
| `/manager/contracts/:contractId` | C13 |
| `/manager/contracts/:contractId/edit` | C13 |
| `/manager/contracts/:contractId/tenants` | C14 |
| `/manager/contracts/new` | C13 |
| `/manager/cost-allocations` | C19 |
| `/manager/dashboard` | C24 |
| `/manager/debts` | C23 |
| `/manager/furnitures` | C15 |
| `/manager/furnitures/:furnitureId` | C15 |
| `/manager/invoices` | C21 |
| `/manager/invoices/:invoiceId` | C21 |
| `/manager/invoices/:invoiceId/edit` | C21 |
| `/manager/invoices/new` | C21 |
| `/manager/maintenance` | C20 |
| `/manager/maintenance/:reportId` | C20 |
| `/manager/payments` | C22 |
| `/manager/payments/:paymentId` | C22 |
| `/manager/reports` | C24 |
| `/manager/room-services` | C16 |
| `/manager/room-types` | C10 |
| `/manager/rooms` | C10 |
| `/manager/rooms/:roomId` | C10 |
| `/manager/rooms/:roomId/edit` | C10 |
| `/manager/rooms/:roomId/slots` | C10 |
| `/manager/rooms/new` | C10 |
| `/manager/service-requests` | C17 |
| `/manager/services` | C16 |
| `/manager/settings/zalo` | C25 |
| `/manager/tenancies/:contractTenantId` | C11 |
| `/manager/tenants` | C11 |
| `/manager/utility-charges` | C18 |
| `/manager/utility-charges/:chargeId` | C18 |
| `/manager/utility-charges/:chargeId/edit` | C18 |
| `/manager/utility-charges/new` | C18 |
| `/notifications` | C37 |
| `/owner` | C24 |
| `/owner-onboarding` | C30 |
| `/owner/activity` | C33 |
| `/owner/approvals/invoices` | C33 |
| `/owner/branches` | C31 |
| `/owner/branches/:branchId` | C31 |
| `/owner/branches/:branchId/edit` | C31 |
| `/owner/branches/:branchId/payments/settings` | C31 |
| `/owner/branches/:branchId/policies` | C31 |
| `/owner/branches/new` | C31 |
| `/owner/dashboard` | C24 |
| `/owner/finance` | C24 |
| `/owner/manager-assignments` | C32 |
| `/owner/managers` | C32 |
| `/owner/managers/:managerId` | C32 |
| `/owner/managers/invite` | C32 |
| `/owner/onboarding` | C30 |
| `/owner/payments` | C33 |
| `/owner/reports` | C24 |
| `/owner/verification` | C30 |
| `/profile` | C03 |
| `/register` | C01 |
| `/reset-password` | C02 |
| `/rooms` | C04 |
| `/rooms/:roomId` | C04 |
| `/rooms/:roomId/appointments/new` | C06 |
| `/rooms/:roomId/bookings/new` | C07 |
| `/tenant` | C26, C24 |
| `/tenant/contracts` | C27 |
| `/tenant/contracts/:contractId` | C27 |
| `/tenant/contracts/:contractId/my-terms` | C27 |
| `/tenant/dashboard` | C26, C24 |
| `/tenant/invoices` | C28 |
| `/tenant/invoices/:invoiceId` | C28 |
| `/tenant/maintenance` | C29 |
| `/tenant/maintenance/:reportId` | C29 |
| `/tenant/maintenance/new` | C29 |
| `/tenant/my-room` | C27 |
| `/tenant/payments` | C28 |
| `/tenant/services` | C29 |
| `/tenant/utilities` | C29 |
| `/tenant/utilities/allocations` | C29 |
| `/user` | C06, C07, C24 |
| `/user/appointments` | C06 |
| `/user/appointments/:appointmentId` | C06 |
| `/user/bookings` | C07 |
| `/user/bookings/:bookingId` | C07 |
| `/user/bookings/:bookingId/payment` | C08 |
| `/user/dashboard` | C06, C07, C24 |
| `/verify-email` | C01 |
