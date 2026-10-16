# Hợp đồng API frontend Manager

Ngày đối chiếu: 16/10/2026. Phạm vi M01–M29, nhánh `feature/fe-rebuild`. Các DTO có schema thực thi tại `fe-sba-project/src/interfaces/manager.ts`, dùng lại mô hình Guest/User/Tenant. Đây là hợp đồng mock và đề xuất bàn giao BE, không khẳng định các endpoint dưới đây đã tồn tại ở BE.

## Provider và phân quyền

- `VITE_API_MODE=mock`: toàn bộ trải nghiệm giả lập; `hybrid`: auth/thông báo thật, các domain còn thiếu dùng mock; `real`: gọi BE và hiển thị lỗi nếu chưa hỗ trợ, không tự chuyển sang mock.
- `VITE_MANAGER_PROVIDER=mock|real` ghi đè riêng Manager. Giữ provider catalog/leasing/tenant/manager cùng phía khi thử nghiệp vụ liên vai trò. Không trộn kho tài nguyên thật với kho tài nguyên demo.
- Auth và thông báo dùng adapter sẵn có. Hybrid chỉ nối tài khoản đã xác thực vào dữ liệu mock với quyền USER ban đầu. Quyền MANAGER/OWNER từ header demo không tạo phân công; không tự cấp quyền dựa trên email/role do client gửi. Tài khoản demo không được giả danh qua cầu nối auth thật.
- Mọi API private kiểm tra actor đã đăng nhập, role và quan hệ Branch.managerId + OwnerManager đúng owner; OWNER chỉ được thao tác chi nhánh thuộc mình. Kiểm tra lại scope mỗi yêu cầu. Chi nhánh ngừng hoạt động chỉ cho đọc lịch sử.
- Chọn Branch ở UI không phải quyền truy cập. Resource ID phải thuộc Branch; sai scope trả 403 hoặc 404. Bị thu hồi quyền: danh sách phân công được kiểm tra định kỳ 2 giây; cache Manager của Branch không còn được phép bị xóa. API từ chối ngay ở lần gọi tiếp theo.
- Phản hồi thống nhất `{status, code, message, data}`. Thành công code 0; lỗi 401/403/404/409/422/500. Danh sách: `page` từ 0, `size` 1–100; `{content,number,size,totalElements,totalPages}`, `number` từ 1 cho pagination UI.
- Đường dẫn client dưới đây được thêm tiền tố `/__mock` khi dùng mock. Nhận Bearer token của session giả lập, không dùng token demo gửi BE.

## Endpoint chung

Gọi `B = /api/manager/branches/{branchId}`. Tất cả endpoint B yêu cầu MANAGER được phân công hoặc OWNER sở hữu Branch.

| Endpoint | Method | Request / response | Kiểm tra và hành vi |
| --- | --- | --- | --- |
| `/api/manager/branches` | GET | `ManagerBranch[]`: id, ownerId, managerId, name, address, status, managerName, zaloUrl, zaloEnabled | Chỉ trả phân công/chi nhánh sở hữu hiện tại; USER nhận danh sách rỗng. |
| `B/{resource}` | GET | Page của schema resource | Filter thực thi ở API: q, status, documentStatus, financialStatus, rentalMode, operationalStatus, availability, roomId, roomSlotId, roomTypeId, contractId, contractTenantId, bookingId, invoiceId, targetType, environment, assetId, month, from/to, minPrice/maxPrice, capacity, startDate/endDate, debtOnly. Chỉ filter có ý nghĩa với resource tương ứng. |
| `B/{resource}/{id}` | GET | DTO resource | Không trả chi tiết resource ở Branch khác. |
| `B/eligible-users` | GET | `{id,fullName,email,phone}[]` | Tài khoản ACTIVE có appointment/booking/tenancy tại Branch; không duyệt toàn bộ tài khoản hệ thống. |
| `B/summary?month=YYYY-MM` | GET | `ManagerSummary` | Tổng phát hành, nợ, quá hạn, tiền nhận thực tế/sandbox riêng; chuỗi 4 tháng; hàng đợi; số phòng và số SLOT riêng. |
| `B/settings/zalo` | PUT | `{url,enabled}` → `ManagerBranch` | HTTPS hostname chính xác zalo.me, đường dẫn `/g/…`, không credentials; URL bắt buộc nếu bật; chỉ Tenant hiệu lực tại đúng Branch được xem. |
| `/api/manager/demo/scenario` | POST | `{scenario:'reset'|'revoke',branchId}` → null | Chỉ DEV + Manager demo. Revoke mô phỏng chủ nhà, không cấp quyền thay đổi phân công cho Manager thường. Reset toàn bộ kho liên kết, giữ session/refresh/tài khoản gốc. |

## Tài sản, phòng và cư trú

| Resource / mutation | DTO / trạng thái | Validation và chuyển trạng thái |
| --- | --- | --- |
| `rooms`: POST collection, PATCH detail, DELETE detail | RoomInput: code, title, roomTypeId, rentalMode, capacity, sizeM2, floor, price, description, amenities, images, operationalStatus, isPublic. Read bổ sung availability, occupiedResources, heldResources. | Mã duy nhất không phân biệt hoa thường trong Branch; RoomType đúng Branch đang dùng; số hợp lệ; ảnh hợp lệ. Publication độc lập vận hành và khả dụng. Bảo vệ hợp đồng/giữ chỗ/số SLOT khi đổi mode, sức chứa hoặc ngừng hoạt động. Xóa bị từ chối nếu còn hồ sơ thuê/tài sản liên quan; phòng mới có điện/nước bắt buộc. |
| `room-types`: POST, PATCH, DELETE | code, name, defaultCapacity, description, active | Mã duy nhất trong Branch; không xóa loại phòng được tham chiếu. |
| `slots`: POST, PATCH | roomId, code, name, operationalStatus | Chỉ parent SLOT đúng Branch; mã duy nhất trong phòng; không vượt sức chứa; không ngừng chỗ còn người thuê/giữ chỗ. Read gồm khả dụng theo khoảng ngày và lịch sử cư trú. Xóa trực tiếp không hỗ trợ để giữ lịch sử. |
| `tenants`: GET | ManagerTenancy mở rộng Tenancy với userName/email/phone/sourceBookingId | Mỗi ContractTenant là một nghĩa vụ riêng; không gộp nợ người cùng phòng. Hồ sơ tài khoản là nguồn gốc, không sửa thông tin tài khoản qua tenancy. |
| `tenants`: POST / PATCH `{id}` | `{contractId,userId,roomSlotId,role,startDate,endDate,monthlyRent,firstMonthRent,lastMonthRent,prepaidFirstRequired,prepaidLastRequired,terms}` | User ACTIVE đủ điều kiện tiếp nhận; ngày nằm trong hợp đồng; ROOM không có slot; SLOT đúng phòng; không trùng người/chỗ/cư trú, không vượt sức chứa. Trả trước bằng giá thỏa thuận kỳ đầu/cuối. Chỉ sửa nghĩa vụ DRAFT. |
| `tenants/{id}/representative`: POST | `{contractId,reason}` | Đổi đại diện giữa thành viên ACTIVE trong cùng hợp đồng ACTIVE, nguyên tử, đúng một đại diện. |
| `tenants/{id}/activate`: POST | `{contractId,reason}` | Hợp đồng ACTIVE, thành viên DRAFT, đủ trả trước VERIFIED; không tạo thêm đại diện. |

Ảnh phòng cho phép ảnh data PNG/JPEG/WebP đã kiểm tra nội dung, tối đa 1 MB/ảnh; ảnh HTTPS hoặc asset demo cũng được dùng. Attachment cho charge/maintenance gồm `{id,name,mime,size,url}`, tối đa 3 ảnh, 1 MB/ảnh; API kiểm tra MIME, chữ ký nội dung và kích thước, không chỉ `accept` ở UI. Hết dung lượng sessionStorage trả 422 và hoàn tác mutation trên toàn bộ kho liên quan.

## Lịch xem, giữ phòng, hợp đồng

| Mutation | DTO | Trạng thái / quy tắc |
| --- | --- | --- |
| `appointments/{id}` POST | `{status,reason,startAt?,endAt?}` | PENDING → CONFIRMED/REJECTED/CANCELLED; CONFIRMED → COMPLETED/CANCELLED. Kiểm tra tương lai, giờ tiếp khách, thời lượng, xung đột phòng/lịch Manager; không hoàn tất trước endAt. USER đọc lại cùng bản ghi/timeline và nhận thông báo. |
| `bookings/{id}` POST | `{decision:'confirm'|'reject'|'cancel',reason}` | Chỉ xác nhận REQUESTED/AWAITING_PAYMENT còn hạn với hold VERIFIED đúng môi trường và không xung đột. Từ chối/hủy trước xác minh; trường hợp đã nhận tiền cần quy trình hoàn tiền được duyệt. Không tự xác minh tiền giữ. |
| `bookings/{id}/convert` POST | `{code,terms,monthlyRent,firstMonthRent,lastMonthRent}` | CONFIRMED còn hạn + hold VERIFIED → CONVERTED, tạo Contract DRAFT và ContractTenant DRAFT đúng ROOM/SLOT, đúng một lần. Hold vẫn là hold, không tạo trả trước hay thu tiền mới. |
| `contracts` POST / `{id}` PATCH | `{code,roomId,startDate,endDate,terms}` | Mã duy nhất, thời hạn hợp lệ, phòng đúng Branch; chỉ sửa DRAFT và không làm mất hiệu lực nghĩa vụ đã khai báo. |
| `contracts/{id}/activate` POST | `{reason}` | DRAFT có đúng một REPRESENTATIVE, không xung đột, từng thành viên đủ trả trước VERIFIED → Contract và các tenancy ACTIVE. |
| `contracts/{id}/cancel` POST | `{reason}` | Chỉ DRAFT chưa có tiền VERIFIED; giữ hồ sơ CANCELLED. |
| `contracts/{id}/end` POST | `{reason}` | Chỉ ACTIVE đã hết hạn; kết thúc quan hệ thuê và lưu ngày rời. Không mô phỏng quy trình rời sớm/chuyển phòng. |

Appointment/Booking Manager DTO mở rộng DTO USER bằng thông tin liên hệ người yêu cầu, chỉ cung cấp sau scope guard. Inventory và occupancy dùng chung với public availability; room fixtures đang có tenancy ACTIVE không còn được dùng làm phòng trống trong ca booking thành công. Public availability, tạo lịch xem và tạo booking cũng từ chối phòng nội bộ, kể cả client biết ID.

Hồ sơ nháp đã có tiền PENDING/REVIEW/VERIFIED không được hủy hoặc đổi người chịu tiền, chỗ/ngày/giá/nghĩa vụ trực tiếp. Thuê đúng một tháng có cùng ngày ở hai tháng liền nhau chỉ có trả trước kỳ đầu, lastMonthRent/prepaidLastRequired phải bằng 0; ranh giới cuối tháng/thời hạn lẻ còn cần quyết định Q1. Không suy diễn công thức chia RENT theo tháng dương lịch.

Để hoàn tất trả trước trước kích hoạt, bổ sung **Tenant** endpoint `/api/tenant/tenancies/{id}/prepaid-payments` POST `{bucket:'FIRST_MONTH'|'LAST_MONTH',amount,payerName,idempotencyKey,outcome?}`. Chỉ tài khoản sở hữu tenancy DRAFT còn thời hạn, Contract DRAFT/ACTIVE; không vượt nghĩa vụ còn lại sau VERIFIED/PENDING/REVIEW; idempotent cùng payload. Bộ xử lý sandbox xác minh sau khoảng 5 giây, đặt verifiedAt; không có nút tự duyệt tiền. Không tự áp dụng nguồn này vào tháng lịch khi chưa có hạn mức đã duyệt.

## Dịch vụ, điện nước, phân bổ

| Endpoint | DTO / kết quả | Validation và lịch sử |
| --- | --- | --- |
| `services` POST / `{id}` PATCH | `{code,name,unit,type,billingMethod,unitPrice,mandatory,active}` | Type ELECTRICITY/WATER/INTERNET/PARKING/CLEANING/SECURITY/OTHER; billing METERED/PER_PERSON/PER_ROOM/FIXED/PER_USE. Điện/nước không được bỏ bắt buộc hay ngừng. Mã duy nhất; không ngừng catalog còn đăng ký hiệu lực/chờ xử lý. |
| `room-services` POST / `{id}` PATCH | `{roomId,serviceId,startDate,endDate,unitPrice}` | Room/catalog đúng Branch; không chồng lấn. Update tạo giai đoạn mới, đóng giai đoạn cũ từ ngày mới, giữ giá lịch sử; không cập nhật phí đã chốt. |
| `service-requests/{id}` POST | `{contractTenantId,decision:'approve'|'reject',reason}` | PENDING → APPROVED/REJECTED; CANCEL_REQUESTED → CANCELLED/APPROVED. Không xử lý lại hay hủy điện/nước. DTO có id riêng của subscription, serviceId là catalog ID. |
| `meters` GET | `{id,roomId,serviceId,branchId,label}[]` | Registry demo cố định gồm công tơ chính/thay thế cho mỗi phòng/dịch vụ đo. Chỉ đọc; BE cần registry/lifecycle thực. Không nhập UUID kỹ thuật trong UI. |
| `charge-preview` POST | ChargeInput → ManagerCharge DRAFT tính thử | Cùng validation/tính toán với lưu thật; không thêm bản ghi, không ghi storage, không tạo notification. |
| `utility-charges` POST / `{id}` PATCH | `{roomId,serviceId,billingMonth,segments,evidence}` | Mỗi segment: startDate/endDate, previousReading/currentReading nullable, quantity, unitPrice, reason, meterId nullable. Tháng ngày 01; các đoạn liên tiếp trong tháng; mỗi đoạn thuộc cấu hình phòng/dịch vụ hiệu lực. Chỉ sửa DRAFT. |
| `utility-charges/{id}/finalize` POST | `{reason}` → FINALIZED | Không chồng khoản đã chốt cùng phòng/dịch vụ/giai đoạn; cần cư trú/dịch vụ đủ điều kiện. Tạo projections riêng cho từng ContractTenant; không tự lập hóa đơn. |
| `cost-allocations/{id}` POST | `{decision:'approve'|'reject',reason}` | Chỉ PENDING; đúng danh sách cư trú, phần không âm, tổng chính xác bằng nguồn. Duyệt thay thế đề xuất đã duyệt trước đó, lưu quyết định và thông báo; giữ nguyên hóa đơn đã phát hành. |

Tính tiền: chỉ số/số lượng tối đa 3 chữ số thập phân, chuyển thành số nguyên phần nghìn bằng BigInt; tiền VND làm tròn nửa lên. Chỉ số không âm; số cuối ≥ số đầu; reset tách đoạn đầu 0; thay công tơ dùng thiết bị đăng ký khác đúng phòng/dịch vụ; đoạn thường liên tục chỉ số/công tơ. Mỗi đoạn phân bổ theo ngày cư trú giao nhau `[start,end)`, phần dư lớn nhất, hòa theo UUID. Dịch vụ tùy chọn cắt theo thời gian subscription; yêu cầu ngừng còn chờ duyệt vẫn được xem là hiệu lực. Đoạn không có ngày đủ điều kiện bị từ chối. Tổng phân bổ đúng tổng phí. Đơn giá và diễn giải từng đoạn được chốt thành snapshot, không suy diễn lại từ catalog khi đọc lịch sử. Tenant không đăng ký được catalog đã ngừng hoặc phòng không còn cấu hình hiệu lực; giá đăng ký lấy cấu hình phòng ở ngày bắt đầu.

Đề xuất phân bổ có liên kết nguồn phí/hóa đơn. Khi nguồn đã liên quan **bất kỳ** hóa đơn ISSUED nào, quyết định đề xuất được lưu nhưng toàn bộ projections/số tiền đã chốt giữ nguyên; cần quy trình điều chỉnh được duyệt sau này. Nếu chưa có ISSUED, projections được cập nhật và invoice nháp phải qua kiểm tra lại nguồn khi issue. Đây là lựa chọn mock từ chối tự sửa lịch sử, không phải chính sách điều chỉnh cuối cùng của BE.

## Hóa đơn và thanh toán

| Endpoint | DTO / kết quả | Quy tắc |
| --- | --- | --- |
| `invoice-sources?contractTenantId=&month=YYYY-MM-01&invoiceId?=` GET | `{rentAmount,policyNote,dueDate,prepaid,sources}` | Chỉ phí chưa tiêu thụ bởi invoice chưa hủy của cùng tenancy: utility/service đã chốt, repair TENANT đã APPROVED thuộc report COMPLETED. Trả trước chỉ VERIFIED còn số dư, trừ phần đã áp dụng/được nháp khác đặt trước và hạn mức tháng đã duyệt. |
| `invoices/{id}/editor` GET | InvoiceInput của DRAFT | Dùng nguồn phí/snapshot gốc để sửa; không đổi tenancy/kỳ đã tạo. |
| `invoices` POST / `{id}` PATCH | `{contractTenantId,billingMonth,dueDate,rentAmount,rentApprovalNote,sources:[{type,id}],adjustment,adjustmentReason,prepaidAmount,prepaidSourcePaymentId,prepaidBucket}` | Một invoice chưa hủy/tenancy/tháng; nguồn không trùng/không bị tiêu thụ; giá thuê khớp lịch đã duyệt; nếu chưa có lịch phải ghi nhận số thỏa thuận và căn cứ rõ ràng. Adjustment cần lý do; tổng không âm, integer VND; trả trước không vượt tổng/hạn mức. |
| `invoices/{id}/issue` POST | `{reason}` | DRAFT → ISSUED đúng một lần; kiểm tra lại eligibility, amount, prepaid reservation ngay lúc phát hành. Cập nhật prepaid applied ở nguồn cũ, không tạo receipt. Link charge với invoice cá nhân; gửi thông báo Tenant. |
| `invoices/{id}/cancel` POST | `{reason}` | Chỉ DRAFT → CANCELLED. Manager không sửa/hủy tài chính ISSUED; trả 403 cho quy trình Owner chưa triển khai. |
| `payments` / `{id}` GET | ManagerPayment: reference, roomId (nội bộ filter), branchId, targetType, invoiceId, bookingId, contractTenantId, payerName, tenantName, amountReceived, allocatedAmount, environment, method, status, createdAt, verifiedAt, providerTransactionId, evidence, note, history | Hợp nhất view từ Booking Hold và Tenant payment; dữ liệu nghĩa vụ vẫn dùng một kho gốc. Target BOOKING_HOLD/INVOICE/PREPAID_RENT tách riêng. |
| `payments/{id}` POST | `{decision:'verify'|'review',note,reference?,amount?,providerTransactionId?}` | Chỉ PENDING/REVIEW; verify chỉ TRANSFER ngoài SANDBOX có invoice/biên nhận hợp lệ. Đối chiếu đủ tham chiếu, amount, provider ID, bằng chứng và người chịu nghĩa vụ; không xác minh nghĩa vụ của chính actor. Không vượt outstanding; mã nhà cung cấp duy nhất; verify/phân bổ một lần. REVIEW không giảm nợ. |

Chứng từ `DRAFT/ISSUED/CANCELLED` độc lập `financialStatus` **suy ra**: UNPAID/PARTIAL/PAID/OVERDUE. Nợ = tổng ISSUED − trả trước đã áp dụng − tiền VERIFIED đã phân bổ đúng invoice. PENDING/REVIEW/FAILED không giảm nợ. Không gộp nợ theo phòng chung. Khi issue phải kiểm tra lại cả lịch RENT; provider ID không được trùng với thanh toán Tenant hay Booking Hold. Prepaid rent không áp dụng vượt phần RENT để thanh toán thay utility/service/repair. UI có liên kết mở nguồn utility/repair đã được kiểm tra đường dẫn an toàn.

Allocation đọc chung: tenancy DRAFT đã gán phòng/chỗ giữ tài nguyên trong khoảng thuê, ACTIVE là cư trú; booking CONVERTED trở thành lịch sử và không giữ tài nguyên lần nữa. Chỉ khi contract/tenancy đóng hợp lệ mới giải phóng allocation. Room có tham chiếu contract, kể cả nháp chưa thêm Tenant, không được xóa làm mồ côi hồ sơ.

Demo biên nhận `TP-QR-DEMO-01`, `BANK-DEMO-0001`, 50.000 VND là dữ liệu giả, không phải xác nhận giao dịch ngân hàng thật. Thu thực tế trên báo cáo chỉ nhận receipt VERIFIED ngoài sandbox, theo verifiedAt; Booking Hold bị loại khỏi thu thuê. Prepaid receipt và prepaid application không được cộng hai lần. Sandbox được báo cáo riêng.

**Q1–Q5 còn mở:** tháng lẻ, lịch RENT, phân bổ FIRST/LAST, hạn trả trước, giá sau ACTIVE/rời sớm không có thuật toán mới. Demo lưu lịch/thỏa thuận và hạn mức được duyệt rõ ràng; một fixture kỳ sau cho tenancy 5100 cho áp dụng FIRST tối đa 3 triệu. Quan hệ mới có hạn mức tháng 0 đến khi có lịch được duyệt, không tự lấy trả trước trừ tùy ý. Hạn ngày 05 là gợi ý cấu hình demo; BE phải trả due-date snapshot theo BranchPolicy đã phê duyệt. Không đưa gợi ý này thành chính sách dự án.

## Furniture, bảo hành và sửa chữa

| Endpoint | DTO / trạng thái | Validation |
| --- | --- | --- |
| `furnitures` POST / `{id}` PATCH, DELETE | `{roomId,roomSlotId,code,name,category,serial,purchaseDate,value,condition,operationalStatus}` | Mã duy nhất, phòng/chỗ đúng Branch; không chuyển tài sản có hồ sơ sang chỗ khác; không ngừng tài sản có report đang mở. Không xóa tài sản có lịch sử report. |
| `furnitures/{id}/warranty-save` POST | `{warrantyId?,provider,reference,startDate,endDate,terms}` | Tài sản thuộc Branch; thời hạn hợp lệ. |
| `furnitures/{id}/warranty-delete` POST | `{warrantyId}` | Chỉ bảo hành chưa hiệu lực; giữ lịch sử đã có hiệu lực. |
| `maintenance/{id}/{action}` POST | `{reason,handler?,recordId?}` | accept/reject OPEN; assign ACCEPTED/IN_PROGRESS; start ACCEPTED có người phụ trách; complete IN_PROGRESS có nhật ký. Append ReportRecord/timeline mỗi bước, không thay nội dung Tenant đã gửi. |
| `maintenance/{id}/record` POST | `{reason,description,cost,responsibleParty,evidence}` | ACCEPTED/IN_PROGRESS; tiền nguyên không âm, cost>0 cần OWNER/TENANT chịu; luôn PENDING xét chi phí ban đầu. |
| `maintenance/{id}/cost-approve` hoặc `/cost-reject` POST | `{reason,recordId}` | Record cost>0 PENDING; cấm tự duyệt trách nhiệm tài chính chính mình. Chỉ chi phí TENANT APPROVED của report COMPLETED thành nguồn invoice. |

## API BE thực tế và việc bàn giao

Đã đối chiếu controllers/entities/services cùng docs hiện tại. Property `/api/v1/rooms`, room-types, room-slots có CRUD nhưng các đường đọc/ghi đã kiểm tra chưa chứng minh đầy đủ kiểm tra Branch của actor. Không nối các endpoint này vào workspace Manager để tránh giả vờ đã có branch-safe integration.

Utilities có role OWNER/MANAGER ở một số controller nhưng cần kiểm tra ownership/assignment ở tầng service; ServiceCatalog còn STAFF không thuộc bộ role đã duyệt. Furniture có PendingSecurityMaintenanceAccessPolicy từ chối quản lý. Branch assignment list, booking→contract, ContractTenant operations, billing source/issue/reconciliation, báo cáo, private Zalo và maintenance end-to-end cần hoàn thiện API/authorization. Endpoint và DTO trong tài liệu này là ranh giới adapter cho lần nối BE đó.

Cần BE: actor lấy từ JWT/session đáng tin, assignment/owner guards mỗi resource; transaction DB cho chuyển booking, activation, allocation, issue, receipt; unique constraints/idempotency; integer/decimal currency; optimistic concurrency; durable audit; signed uploads; provider webhook/QR reconciliation; lịch rent/BranchPolicy đã phê duyệt; quản lý công tơ thật; thu hồi cache/session scope. Không cần sửa DB hay BE để chạy bản demo frontend hiện tại.
