# Báo cáo thiết kế rental — điều chỉnh chính thức còn 34 bảng

Ngày: 2026-10-12. Đây là bộ cài mới cho schema `rental` rỗng, chuẩn bị cho branch Modular Monolith. Không phải migration dữ liệu từ `public` và không phải bản backend đã refactor.

Quyết định tinh gọn mới được ưu tiên khi khác D01–D40 cũ. Kết quả: **32 bảng P1, thêm 1 bảng P2, thêm 1 bảng P3 = 34**. Không chạy lên Supabase; nếu schema đã chứa bộ 50 bảng thì cần migration riêng có xác nhận, không dùng DROP để đạt số lượng mới.

| Bảng đã bỏ | Dữ liệu / phạm vi thay thế |
|---|---|
| `owner_verifications` | Trạng thái/hồ sơ hiện hành trên USERS, ADMIN duyệt, audit whitelist |
| `tenants` | Hồ sơ chung USERS; quan hệ thuê/snapshot CONTRACT_TENANTS |
| `verification_challenges` | Một challenge hash/expiry/attempts/consumed hiện hành trên USERS |
| `login_attempts` | Counter/locked_until trên USERS; rate limit backend |
| `refresh_sessions` | Một refresh hash + expiry + auth_version trên USERS |
| `handover_records` | Handover snapshot và evidence trên CONTRACT_TENANTS |
| `move_out_requests` | Một yêu cầu hiện hành và disposition trên CONTRACT_TENANTS; audit trạng thái |
| `room_unavailability_periods` | Trạng thái Room/Slot và allocation MAINTENANCE_BLOCK/BLOCKED |
| `notification_preferences` | JSONB trên USERS ở P2 |
| `contract_documents` | PDF/content snapshot chính trên CONTRACTS; phiên bản bổ sung trên CONTRACT_AMENDMENTS |
| `document_signatures` | Confirmation evidence JSONB tối thiểu, không chứa OTP/hash challenge; workflow mở rộng chưa triển khai |
| `moderation_reports` | Hoãn báo cáo vi phạm; Thread chỉ có trạng thái kiểm duyệt cơ bản |
| `notification_outbox` | Một dòng NOTIFICATIONS mỗi recipient/event/type/channel, delivery/retry ở P3 |
| `notification_templates` | Source/config version theo deployment |
| `thread_comments` | Hoãn; không JSONB giả lập |
| `thread_room_suggestions` | Hoãn; không JSONB giả lập |

Không có bảng thay thế tương đương dưới tên khác. Danh sách bảng hợp lệ được kiểm tra chính xác trong verification/runner.

## 1. Tổng quan

Nguồn đã đối chiếu: master prompt D01–D40 do bạn đính kèm, `docs/All_Diagrams.drawio`, `docs/be_tasks.md`, `docs/all-feature.txt`, `docs/coding_rules.md` và các entity/security/invoice hiện có trong backend.

Đã chuẩn bị: DDL theo Phase, constraints/indexes, deferred triggers cho bất biến liên bảng, seed role/demo, verification SQL, negative tests, catalog bảng/cột/FK/index và ERD sinh từ catalog PostgreSQL đã thực thi. Ba trang ERD cũ giữ dưới tên `Legacy — ...`.

Không sửa Java/config backend trong công việc này; không tạo branch và không kết nối hay chạy SQL trên Supabase. Working tree đã có nhiều chỉnh sửa backend từ trước; các chỉnh sửa đó không thuộc bộ bàn giao này.

Các chức năng cần backend như JWT, API authorization, scheduler hết hạn booking, chữ ký callback, tính giá dịch vụ theo thứ tự ưu tiên và gửi notification vẫn phải triển khai trên branch mới. Chi tiết kiểm thử đã chạy/chưa chạy nằm ở [TEST_RESULTS.md](TEST_RESULTS.md).

### Hiện trạng và thay đổi quan trọng

| Nhóm hiện tại | Vấn đề cần giải quyết | Thiết kế mới / ảnh hưởng khi refactor |
|---|---|---|
| Authentication / Manager | Mô hình account, role và Manager cần một nguồn danh tính thống nhất | `users`, `roles`, `user_roles`; Manager là User có role và quan hệ `owner_managers` |
| Branch / Room / danh mục | Cần bảo vệ phạm vi Owner–Manager–Branch, RoomType/Service theo Branch | Composite FK; Branch giữ `manager_id` duy nhất; không thêm manager vào Room |
| Tenant | Hồ sơ được gộp với danh tính chung | USERS hỗ trợ INVITED; `contract_tenants.tenant_user_id` trực tiếp, unique member ACTIVE; một User thuê nhiều Owner, snapshot riêng từng lần ký |
| Booking / Contract | Không được gắn một Booking duy nhất ở Contract chính rồi bỏ mất các lượt thuê slot khác | `contracts` thuộc Room; `contract_tenants.source_booking_id` nullable UNIQUE cho từng lượt tham gia |
| Giữ tài nguyên / cư trú | Cần phân biệt giữ chỗ với số ngày thực tế dùng dịch vụ | `resource_allocations` tách `room_slot_occupancies`; kiểm tra Room–Slot và sức chứa |
| Invoice hiện có | Mô hình backend theo Contract/kỳ không còn phù hợp D23 | Invoice theo ContractTenant/tháng; DTO, repository, authorization và tests phải đổi trên branch mới |
| Payment / cọc | Booking Hold khác tiền thuê trả trước; không thu lại tháng đầu/cuối | Một receipt, các bucket trả trước và dòng áp dụng nội bộ; không thêm SECURITY_DEPOSIT |
| Phân bổ điện/nước | “Chia đều người trong kỳ” không xử lý người vào/ra giữa kỳ | Ngày cư trú giao kỳ/segment, largest remainder, snapshot proposal JSONB |
| Sửa chữa | Phải truy vết nguồn khoản thu Tenant và phần Owner chịu | `report_records` typed COST/LIABILITY và self-FK; chỉ liability Tenant được duyệt mới vào Invoice |
| BaseEntity hiện tại | Không thể dùng chung soft-delete/timestamp tùy tiện cho lịch sử tài chính | UUID; thời điểm `timestamptz`; trạng thái/vô hiệu hóa theo loại bảng; không blanket soft-delete |

Không cần tạo bảng trùng chức năng như `tenant_contracts`, `cost_allocation_items`, `payment_allocations`, `bank_transactions`, `owner_bank_accounts`, `payment_requests`, `file_metadata`, deposit ledger hoặc dashboard table.

## 2. Mapping D01–D40

“DDL” bên dưới nghĩa là cấu trúc/ràng buộc đã có trong bộ SQL, không phải API/use case đã hoàn thành. “Backend tiếp nối” nêu phần không thể thay bằng DDL đơn thuần.

| Decision ID | Quyết định đã áp dụng | Entity/Table/Constraint liên quan | Phase | Trạng thái |
|---|---|---|---|---|
| D01 | ERD đủ ba Phase, cài đặt tách file | 01+02: core; 03: P2; 04: P3 | 1–3 | DDL; P2/3 chỉ chuẩn bị cấu trúc |
| D02 | Một kho danh tính; JWT và xác thực tối giản | `users` chứa password/hash refresh/challenge/counters; một refresh và một challenge/User | 1 | DDL; hash/rotate/CAS/rate limit/identity claiming tiếp nối ở backend; không đa phiên |
| D03 | Users N–N Roles; 5 role, không GUEST | `roles`, `user_roles`, PK `(user_id,role_id)` | 1 | DDL + role seed + test chống trùng |
| D04 | ĐIỀU CHỈNH: hồ sơ User chung, quan hệ thuê theo Owner | `contract_tenants.tenant_user_id` FK USERS, snapshot; INVITED không được đăng nhập | 1 | DDL + demo một User thuê hai Owner; quyền đọc qua phạm vi Contract, không profile Owner riêng |
| D05 | Branch có Owner và Manager phù hợp | FK `(owner_id,manager_id)`; ACTIVE bắt buộc Manager | 1 | DDL + negative tests |
| D06 | Manager chỉ thuộc một Owner hiện tại | `owner_managers`, UNIQUE(manager_id), audit chuyển quan hệ | 1 | DDL; service chuyển Manager phải cập nhật Branch trước trong transaction |
| D07 | Branch là nguồn phân công duy nhất | `branches.manager_id`, audit; Room không giữ manager | 1 | DDL; API lấy quyền từ Branch hiện tại |
| D08 | Danh mục RoomType/Service theo Branch | `room_types`, `services`; composite FK của Room/RoomService | 1 | DDL |
| D09 | Thông tin Room/Branch có cấu trúc | Giá tham chiếu, địa chỉ, amenities, image refs, QR/Zalo trên bảng nghiệp vụ | 1 | DDL; DTO công khai phải loại thông tin riêng tư |
| D10 | User đăng nhập được Appointment/Booking | `requester_user_id`; không cần Tenant trước | 1 | DDL + demo visitor |
| D11 | Appointment theo khoảng giờ, cho phép xem nhóm | `start_at/end_at`, `policy_snapshot`; không unique room/time | 1 | DDL + hai appointment trùng giờ hợp lệ; service kiểm tra chính sách nhóm |
| D12 | Contract chính thuộc Room, thành viên có nghĩa vụ riêng | `contracts`, `contract_tenants`; slot nằm ở Booking/Member | 1 | DDL + ROOM/SLOT demo |
| D13 | Booking tạo Contract hoặc thêm thành viên | `contract_tenants.source_booking_id` nullable UNIQUE; không `contracts.booking_id` | 1 | DDL + hai bookings vào cùng Contract demo |
| D14 | Giữ/thuê tài nguyên tách cư trú | `resource_allocations`, trigger lock Room/Branch và kiểm tra overlap | 1 | DDL + SQL negative; concurrency nhiều kết nối chưa chạy |
| D15 | Policy theo Branch; Booking Hold khác PREPAID_RENT | `branch_policies`, booking snapshot; payment verified, first/last buckets; member ACTIVE cần đủ trả trước | 1 | DDL + demo chuyển tiền giữ chỗ; số tiền/hạn thật chưa cấu hình |
| D16 | Đại diện là vai trò trong Contract; bảo toàn lịch sử | Partial UNIQUE tối đa một đại diện + deferred trigger đủ một; audit member | 1 | DDL; chuyển đại diện đóng/mở vai trò trong một transaction, audit thời điểm/actor |
| D17 | ROOM không bắt buộc slot, SLOT bắt buộc slot | `room_slot_occupancies`; composite FK, overlap/capacity checks | 1 | DDL + demo + negative tests |
| D18 | ĐIỀU CHỈNH: Amendment riêng, move-out/handover gộp vào member | `contract_amendments`, cột `contract_tenants` + audit | 2 | Một yêu cầu/bàn giao hiện hành; không lịch sử hồ sơ nộp độc lập; không tự hoàn/phạt |
| D19 | Giá theo Branch → Room → Contract → Member | `services`, phiên bản `room_services`, `contract_services.contract_tenant_id/agreed_unit_price` | 1 | DDL; service tính precedence/hiệu lực và ghi snapshot, không có trigger tự suy giá |
| D20 | Chỉ số/phí lưu trực tiếp ở charge, có segment | `room_service_charges`; CHECK chỉ số/quantity/amount, không meter table | 1 | DDL; correction theo hủy/thay nguồn có truy vết |
| D21 | Một bảng proposal, chi tiết JSONB | `cost_allocation_proposals`; check shape/member/sum; approved snapshot bất biến | 1 | DDL + SQL tests; backend validate request trước insert |
| D22 | Chia phí theo ngày cư trú, làm tròn xác định | `default_charge_allocation`; giao khoảng `[start,end)`, largest remainder, tie UUID | 1 | DDL + ví dụ 30/10 ngày; zero eligible weights bị từ chối |
| D23 | Invoice riêng member/tháng, tháng đầu/cuối vẫn lập | `invoices`; partial UNIQUE; `invoice_items.PREPAID_APPLICATION` | 1 | DDL + demo invoice tháng đầu/cuối |
| D24 | Ngày thu + gia hạn theo Branch; tháng lẻ thỏa thuận | `branch_policies`, `invoice_due_date`, invoice snapshot, first_month_rent | 1 | DDL + kiểm tra tháng 2/năm nhuận; service lưu snapshot đúng phiên bản |
| D25 | Người nộp khác người có nghĩa vụ; một đích trực tiếp | `payments.payer_user_id`, XOR Invoice/Booking; beneficiary từ Invoice/Member | 1 | DDL + trả hộ; không payment_allocations |
| D26 | Tách trạng thái chứng từ và thanh toán; Owner duyệt sửa sau phát hành | `invoices.document_status`, `invoice_balances`, immutable charge lines, replacement link | 1 | DDL + negative tests; workflow phê duyệt và phân quyền HTTP tiếp nối |
| D27 | QR ảnh trên Branch hoặc sandbox; không ngân hàng thật bắt buộc | Branch QR refs; `payments.method/environment`; không bank account/payment request tables | 1/3 | DDL; seed dùng VNPay giả lập chỉ để test, chưa chốt provider triển khai |
| D28 | Payment + webhook evidence, idempotency | Unique provider transaction, payment request key, webhook event key | 1/3 | DDL + replay test; chữ ký callback thật chưa chạy |
| D29 | Tiền thiếu/dư/chưa khớp/đến muộn có trạng thái và audit | Received/direct allocated/prepaid/refunded tại Payment; view số dư; không ví | 1/2 | DDL + demo partial/over/late |
| D30 | Một receipt, dùng lại Payments/Member/Items/Audit | Prepaid buckets + immutable application/reversal; refund counters + unique audit request | 1/2 | DDL + test số dư; không thêm ledger; hoàn/chuyển tiền thực qua service |
| D31 | ReportRequest + ReportRecord chứa lịch sử/phân công | `report_requests.assigned_to`, `report_records` types WORK/ASSIGNMENT/STATUS | 1/2 | DDL; không assignment-history table riêng |
| D32 | Chi phí và trách nhiệm sửa chữa có nguồn | `report_records` COST/LIABILITY self-FK; tổng phần duyệt đúng cost; invoice source unique theo khoản còn hiệu lực | 1 | DDL + Owner 40k/Tenant 60k demo |
| D33 | ĐIỀU CHỈNH: một bảng thông báo | `notifications`; USERS.preferences P2; channel/delivery/retry P3; template source/config | 1–3 | DDL; worker gửi sau commit từ notification, không bảng outbox; không bảo đảm exactly-once với provider |
| D34 | Cloudinary reference tại bảng nghiệp vụ; không binary | `*_public_id`, `resource_type`, image/evidence JSON; audit bỏ evidence nhạy cảm | 1–3 | DDL; signed URL và redaction nội dung tự do do backend bảo vệ |
| D35 | UUID, VND nguyên, numeric lượng/giá, timestamptz/date | Types và CHECK trong DDL; status varchar CHECK; khoảng `[)` | 1–3 | DDL + catalog cột/types |
| D36 | Constraint/index đúng vai trò, trigger có chọn lọc | Composite FK, partial unique, deferred checks và parent-row serialization | 1–3 | DDL; index dư invoice_items(invoice_id) đã bỏ vì unique index đã có prefix |
| D37 | Không blanket soft delete/cascade xóa lịch sử | Restrict FK mặc định; immutable/audit/delete guards; status theo nghiệp vụ | 1–3 | DDL; retention PII/Cloudinary cần policy vận hành |
| D38 | Dashboard đọc nguồn gốc, tách tiền và kỳ | `invoice_balances`, `prepaid_balances`, `branch_funds`, `real_cash_by_branch`, `invoiced_by_period` | 1 | DDL; backend scope Branch và định nghĩa chỉ tiêu theo loại khoản thu |
| D39 | ĐIỀU CHỈNH: P3 chỉ thêm Thread; tài liệu/OTP dữ liệu tối thiểu | `threads`, ALTER contracts/amendments/notifications; không bảng ký/tài liệu riêng | 3 | Comment/Suggestion/báo cáo vi phạm DEFERRED; workflow OTP nhiều signer NOT IMPLEMENTED |
| D40 | Seed hợp lệ; kiểm tra metadata, bất biến và ca lỗi | 05/06 seed, 07 verification, 08 negative, test runner | 1–3 | SQL cục bộ đã chạy; phần môi trường thật/API/concurrency ghi rõ NOT EXECUTED |

## 3. Danh sách Database Tables cuối cùng

[TABLE_CATALOG.md](TABLE_CATALOG.md) chứa **đủ 34 bảng** theo format: Table, Mục đích, PK, FK quan trọng, Phase, Thay đổi, Module ghi. Phần tiếp theo có toàn bộ column/type/null/default, constraints và index từ PostgreSQL đã thực thi.

- Phase 1: 32 bảng. Phase 2: chỉ thêm `contract_amendments`. Phase 3: chỉ thêm `threads`. Các thông tin gộp dùng ALTER TABLE.
- 22 khái niệm bảng từ ERD Legacy giữ nhưng sửa type/quan hệ/ràng buộc; Tenant đã gộp vào User.
- `MANAGERS` gộp vào User + role + `owner_managers`; không còn một kho đăng nhập Manager riêng.
- `APPOINTMENT` chuẩn hóa tên thành `appointments`; các tên nghiệp vụ còn lại giữ khi phù hợp.
- Các bảng bị loại khỏi phương án do trùng chức năng được liệt kê dưới bảng catalog; việc này **không phải DROP các bảng đang có trong public**.

Quyền ghi theo `modules` trong `coding_rules.md`, không tách thành nhiều database. `identity` sở hữu USERS và hồ sơ chung; `property` sở hữu Branch/Room; `leasing` sở hữu Contract/ContractTenant/Booking/occupancy; `utilities` sở hữu ContractService và charges; `billing` sở hữu Invoice/Items; `payment` sở hữu Payments/webhooks. Module gọi service interface/DTO, không gọi repository của nhau để ghi. Java mapping sau này bỏ Tenant entity, dùng User ID và snapshot trên ContractTenant; không dùng User role làm bằng chứng quan hệ thuê.

`reporting` không có bảng riêng. `common.audit` ghi audit hạ tầng; không thêm `features.platform` vào cấu trúc đã chốt. Khóa/version parent trong trigger là cơ chế toàn vẹn DB, không cho phép repository module tự sửa dữ liệu nghiệp vụ module khác.

## 4. Thay đổi ERD

File [All_Diagrams.drawio](../../All_Diagrams.drawio) có 3 trang **Legacy/Deprecated — không phải thiết kế đang sử dụng**, Conceptual ERD, Logical ERD_Ver2 và 10 trang Physical theo module. Các trang hiện hành có đúng 34 bảng, không 16 entity bị loại. Tên bảng/cột/FK sinh từ catalog đã thực thi.

- Users N–N Roles qua UserRoles; Owner–Manager hiện tại và Branch manager có scope.
- Room 1–N Contract theo lịch sử; không có hai Contract chính có thời hạn chồng nhau trong trạng thái ACTIVE/ENDED.
- Contract 1–N ContractTenant; Booking 0..1 → ContractTenant, không FK Booking ở Contract chính.
- ContractTenant 1–N Invoice theo tháng; Invoice 1–N dòng phí/áp dụng tiền và 1–N Payment trực tiếp.
- Payment có tối đa một đích Invoice/Booking; PREPAID_APPLICATION là quan hệ sử dụng trả trước nội bộ có nguồn payment và beneficiary.
- Allocation khóa Room/Slot; occupancy thể hiện ngày cư trú. Đường FK không thể tự diễn tả overlap, “đúng một đại diện”, tổng phân bổ JSON hay số dư tiền; các bất biến này nằm ở SQL trigger và transaction.

Ký hiệu trên trang Physical: `!` NOT NULL, `?` nullable; PK/FK/UQ trên cột, cạnh child → parent ghi cardinality/optionality. Partial UNIQUE và CHECK đầy đủ xem catalog; UQ trên một cột trong composite key không có nghĩa cột đó unique độc lập. Bảng màu xanh dương/vàng/xanh lá phân biệt Phase theo chú thích của trang/table.

## 5. Danh sách file đã tạo/sửa

Sửa tài liệu nguồn: `docs/All_Diagrams.drawio`, `docs/be_tasks.md`, `docs/all-feature.txt`. Giữ ID, người được giao và điểm ước lượng gốc; task bị hoãn ghi DEFERRED. Tổng 211 mã/709 điểm là baseline truy vết bao gồm phần hoãn, không phải cam kết làm hết. Không sửa `docs/coding_rules.md` hoặc Java.

Cập nhật các file hiện có trong `docs/database/rental/` (không tạo bộ file mới):

| File | Vai trò |
|---|---|
| `README.md` | Các bước chạy trên Supabase, số liệu cần kiểm tra, lưu ý chuyển backend |
| `REFACTOR_REPORT.md` | Báo cáo này; D01–D40 và impact |
| `TABLE_CATALOG.md`, `schema-catalog.json`, `module-table-map.json` | Catalog được sinh từ database thử nghiệm và module ownership |
| `01_phase1.sql`, `02_phase1_integrity.sql` | Schema core và bất biến liên bảng bắt buộc |
| `03_phase2.sql`, `04_phase3.sql` | Extension theo Phase |
| `06_seed_demo.sql` | USERS/Member trực tiếp, invited profile, ADMIN review, maintenance block; giữ tiền demo |
| `07_verify.sql`, `08_negative_tests.sql` | Verification theo Phase và ca lỗi cho dữ liệu gộp |
| `test-database.mjs`, `TEST_RESULTS.md` | Runner PostgreSQL thử nghiệm và kết quả/giới hạn |
| `generate-docs.mjs` | Sinh catalog và trang ERD; giữ trang Legacy |
| `feature-overrides.json`, `update-feature-docs.mjs` | Đồng bộ mô tả feature giữ nguyên mã/điểm |

Giữ nguyên `00_preflight.sql`, `05_seed_roles.sql`, `09_access.sql` vì vẫn phù hợp. Runner và generator chỉ sửa chỗ hardcode bảng/cases/Phase, mapping và nhãn Legacy bị ảnh hưởng.

## 6. DDL và Tests

Thứ tự chạy đề xuất: **00 → 01 → 02 → 03 → 04 → 09 → 05 → 06 → 07 → 08**. File 09 được chạy ngay sau khi tạo đủ đối tượng để chốt quyền truy cập trước seed/backend. Xem [README.md](README.md) để thao tác bằng SQL Editor.

SQL thử nghiệm PostgreSQL 18.6/PGlite 0.5.9: 32/33/34 bảng theo Phase, seed, verify và lần lượt 45/48/50 negative tests. Không có credentials Supabase. SQLSTATE lỗi integrity phân biệt với lỗi lập trình; dữ liệu test lỗi được ROLLBACK. Không coi CHECK định dạng SHA-256 là kiểm chứng backend đã hash hay xác thực token đúng.

Đây là baseline mới, không phải cơ chế tránh migration mãi mãi. Sau khi nhóm sử dụng `rental`, các thay đổi tiếp theo phải version hóa, giữ dữ liệu và có script nâng cấp. Không dùng Hibernate `update/create` để âm thầm sửa schema đã chốt.

Các kiểm tra **NOT EXECUTED**: SQL trên Supabase, API/JWT/IDOR, chữ ký provider, concurrency đa kết nối, load test và render giao diện draw.io. Kết quả chi tiết và hướng dẫn test tiếp theo: [TEST_RESULTS.md](TEST_RESULTS.md).

## 7. Các điểm nhóm cần xác nhận khi cấu hình nghiệp vụ thật

Các điểm này không cản trở tạo schema/fixture; chúng cản trở bật quy trình tương ứng cho dữ liệu vận hành. Không cần hỏi lại D01–D40.

| Chính sách còn mở | Nơi lưu / cách xử lý hiện tại |
|---|---|
| Mức Booking Hold và các thời hạn ở mỗi Branch | `branch_policies`, bắt buộc truyền giá trị; seed chỉ là ví dụ |
| Hủy Booking: hoàn bao nhiêu, điều kiện khấu trừ | Policy terms + quyết định Owner/audit; không tự động hoàn hay tịch thu |
| Rời sớm: phạt, tiền thuê cuối đã trả được hoàn/giữ/chuyển thế nào | `contract_tenants` có disposition/lý do/số tiền/người duyệt; chưa quyết định không gây chuyển tiền |
| QR thủ công, VNPay Sandbox hay MoMo Sandbox triển khai trước | DDL hỗ trợ ba lựa chọn; fixture VNPay không thay quyết định của nhóm |
| Tiền tháng lẻ, giá/thời hạn cụ thể chưa chốt | Thỏa thuận member, policy versions và snapshot; không dùng giá seed làm default |

Chưa có chính sách thì backend phải để chờ phê duyệt hoặc từ chối thực hiện use case cần chính sách đó. Việc có cột lưu quyết định không có nghĩa hệ thống đã được phép tự quyết định.
