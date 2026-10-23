# Student 3 — Booking / Contract / Zalo Implementation Plan (Backend)

> Phạm vi: BE-03.1 → BE-03.6 (Phase 1, P0) và BE-03.7 → BE-03.8 (Phase 2, P1). Chỉ backend.
> Nguồn: `docs/be_tasks.md`, `docs/business_rules/br-st3-st5.md`, `docs/backend/contract-policies.md`,
> `docs/frontend/fe-be-api-contract.md` (C07, C08, C12, C13, C14, C25, C26, C27), `docs/database/rental/*.sql`, `docs/coding_rules.md`.

**Goal:** Hoàn thiện nghiệp vụ Booking giữ chỗ, Contract + ContractTenant, cư trú và link Zalo trên schema `rental` hiện có, không đổi DDL.

**Architecture:** Modular monolith. Module sở hữu: `leasing` (toàn bộ Booking/Contract). Ghi dữ liệu `Branch` (Zalo) thuộc `property`. Gọi module khác chỉ qua Service Interface hoặc Port (pattern `RoomTenancyPort` / `Pending*` placeholder đã có).

**Tech Stack:** Java 21, Spring Boot, Spring Data JPA (Hibernate `ddl-auto: validate`, `default_schema: rental`, Flyway tắt), Lombok, MapStruct, PostgreSQL.

---

## 1. Module & Entity của Student 3

### 1.1 Module `leasing` (chủ sở hữu) — entity ĐÃ CÓ, không tạo mới

| Bảng (ERD) | Entity (đã có) | Dùng cho | Ghi chú quan trọng từ DDL/trigger |
|---|---|---|---|
| `bookings` | `leasing/entity/Booking` | BE-03.1→03.3, P2-13 | `status` terminal (`EXPIRED/CANCELLED/REJECTED/CONVERTED`) không mở lại được (trigger `guard_source_snapshot`). `CONFIRMED` bắt buộc `confirmed_hold_until` + đủ Payment `VERIFIED` (trigger finance). `policy_id` FK tới `branch_policies`. |
| `resource_allocations` | `leasing/entity/ResourceAllocation` | Khóa khả dụng P1-54/58/62/66 | Trigger **deferred** `Room/Slot allocation overlap` chặn trùng mọi allocation chưa `RELEASED` — **không xét `expires_at`** ⇒ hold hết hạn phải được chuyển `RELEASED` thực sự. Allocation của Booking phải `HELD` và Booking đang `AWAITING_PAYMENT/CONFIRMED`; allocation của Contract phải `LEASED` và Contract `ACTIVE`. |
| `contracts` | `leasing/entity/Contract` | BE-03.4/03.5, P2 | Status DB chỉ có `DRAFT/ACTIVE/ENDED/CANCELLED`; `document_status` (`DRAFT/AWAITING_CONFIRMATION/CONFIRMED`) dùng cho "hai bên xác nhận / chờ hiệu lực". `UNIQUE(branch_id, code)`. Trigger chặn 2 Contract ACTIVE/ENDED trùng ngày trên cùng Room. |
| `contract_tenants` | `leasing/entity/ContractTenant` | Người tham gia P1-63, giá thuê P1-61, trả phòng P2-17 | Unique index: đúng 1 `REPRESENTATIVE` ACTIVE / contract (trigger cũng yêu cầu Contract ACTIVE có **đúng 1**). `prepaid_first_required = first_month_rent`, `prepaid_last_required = last_month_rent` (CHECK). Có `source_booking_id` nhưng trigger yêu cầu Booking đó **đã `CONVERTED`** ngay khi ghi. Có sẵn cột move-out/handover/disposition cho P2. ACTIVE cần Payment prepaid VERIFIED (trigger finance). |
| `room_slot_occupancies` | `leasing/entity/RoomSlotOccupancy` | Cư trú P1-66 | Phải nằm trong một allocation `LEASED` của Contract; không trùng slot/member; không vượt `rooms.capacity`. |
| `contract_amendments` (P2) | `leasing/entity/ContractAmendment` | Gia hạn/điều chỉnh P2-16/20 | `amendment_number`, `previous_terms`, `new_terms` jsonb, `status DRAFT/APPROVED/REJECTED`. |

Enums đã có: `BookingStatus`, `ContractStatus`, `ContractDocumentStatus`, `ContractTenantRole`, `ContractTenantStatus`, `MoveOutStatus`, `RentalMode`, `ResourceAllocationStatus`, `ResourceAllocationAllocationKind`, `PaymentEnvironment`, `PrepaidLastDisposition`, `ContractAmendmentStatus`.
Enum mới duy nhất: `HoldRefundOutcome { FULL_REFUND, NO_REFUND, REFUND_PAID_PART, MANAGER_REVIEW }` (kết quả áp chính sách hoàn cọc mục B — lưu trong audit, không có cột).

### 1.2 Entity của module khác Student 3 cần ĐỌC (không ghi trực tiếp)

| Entity | Module | Field dùng | Cách truy cập |
|---|---|---|---|
| `Branch` | `property` | `ownerId`, `managerId`, `status`, `qrUrl`, **`zaloGroupUrl`, `zaloEnabled`** | `PropertyQueryService` (mới, property) |
| `BranchPolicy` | `property` | `bookingHoldAmount`, `paymentWindowMinutes`, `temporaryHoldMinutes`, `confirmedHoldMinutes`, `cancellationTerms`, `version` | `PropertyQueryService.currentPolicy(branchId)` |
| `Room`, `RoomSlot` | `property` | `branchId`, `rentalMode`, `operationalStatus`, `capacity`, `referencePrice`; slot `status` | `PropertyQueryService` |
| `Payment` | `payment` (Student 5) | tổng `allocated_amount` VERIFIED theo `booking_id` | Port `BookingFundsPort` do leasing định nghĩa, payment implement |
| `AuditLog` | `common.audit` | lịch sử trạng thái | `AuditLogRepository` (mới, common) |

> Lưu ý: `room_slots.status` và `rooms.operational_status` **không có** giá trị OCCUPIED/HELD. "Cập nhật trạng thái phòng/slot" (P1-66) = ghi `resource_allocations` + `room_slot_occupancies`; trạng thái trống/đang giữ/đang thuê được **suy ra** từ hai bảng này, không sửa Room.

---

## 2. Cấu trúc file sẽ thêm (theo `coding_rules.md`)

```text
modules/leasing/
├── controller/
│   ├── BookingController.java          # /api/bookings (user + manager, scope ở service)
│   ├── ContractController.java         # /api/contracts (manager) + /api/contracts/my (tenant)
│   └── TenantZaloController.java       # /api/tenant/zalo
├── dto/request/
│   ├── BookingCreateRequest.java       # roomId, roomSlotId?, startDate, endDate, appointmentId?
│   ├── BookingSearchRequest.java       # branchId?, roomId?, status?, from?, to?
│   ├── BookingDecisionRequest.java     # reason (reject/cancel)
│   ├── ContractCreateRequest.java      # roomId, startDate, endDate, terms, paymentEnvironment, tenants[]
│   ├── ContractFromBookingRequest.java # bookingId(s), endDate, terms, giá từng member
│   ├── ContractUpdateRequest.java      # chỉ khi DRAFT
│   ├── ContractTenantRequest.java      # tenantUserId, roomSlotId?, role, start/endDate, monthlyRent, firstMonthRent, lastMonthRent, terms
│   ├── ContractTransitionRequest.java  # reason, effectiveDate
│   └── ContractSearchRequest.java      # P2-15
├── dto/response/
│   ├── BookingResponse.java            # field theo C07 + timeline + contractId
│   ├── BookingPaymentInstructionResponse.java # amount, reference, paymentDueAt, qrUrl, environment, instructions
│   ├── StatusHistoryResponse.java      # from, to, actorId, reason, at
│   ├── ContractResponse.java           # theo C13 + participants
│   ├── ContractTenantResponse.java
│   └── ZaloLinkResponse.java           # branchId, branchName, zaloGroupUrl
├── mapper/
│   ├── BookingMapper.java              # MapStruct
│   └── ContractMapper.java             # Contract + ContractTenant
├── repository/
│   ├── BookingRepository.java          # + findByIdForUpdate (PESSIMISTIC_WRITE), expired queries
│   ├── ContractRepository.java
│   ├── ContractTenantRepository.java
│   ├── ResourceAllocationRepository.java  # overlap query + advisory lock native query
│   ├── RoomSlotOccupancyRepository.java
│   └── ContractAmendmentRepository.java   # P2
├── specifications/
│   ├── BookingSpecifications.java
│   └── ContractSpecifications.java     # P2-15
├── schedule/
│   ├── BookingExpiryJob.java           # P1-55, BR-P10a/P11
│   ├── ContractActivationJob.java      # BR-P14/P14a: kích hoạt member đủ điều kiện
│   └── ContractExpiryReminderJob.java  # P2-16, BR-R17/R20a
└── service/
    ├── BookingService.java
    ├── ContractService.java
    ├── TenancyQueryService.java        # API công khai cho module khác (maintenance, billing, zalo)
    ├── BookingFundsPort.java           # Student 5 implement
    ├── MoveInPaymentPort.java          # Student 5 implement (hóa đơn nhận phòng đã đủ)
    ├── policy/HoldRefundPolicy.java    # bảng hoàn cọc mục B (thuần)
    ├── policy/LeasingDates.java        # quy ước ngày + giờ VN
    └── impl/
        ├── BookingServiceImpl.java
        ├── ContractServiceImpl.java
        ├── TenancyQueryServiceImpl.java
        ├── ResourceAvailabilityGuard.java   # dùng chung Booking + Contract (lock + overlap)
        ├── LeasingHistoryRecorder.java      # ghi audit_logs cho chuyển trạng thái
        └── PendingPaymentBookingFundsPort.java  # placeholder fail-closed

modules/property/   (phối hợp Student 1/2 — chủ sở hữu Branch)
├── repository/BranchRepository.java, BranchPolicyRepository.java   # chưa có
├── service/PropertyQueryService.java + impl/PropertyQueryServiceImpl.java
├── service/BranchZaloService.java + impl/BranchZaloServiceImpl.java
├── dto/request/BranchZaloRequest.java, dto/response/BranchZaloResponse.java
└── controller/BranchZaloController.java   # PUT/GET /api/branches/{branchId}/zalo

common/audit/AuditLogRepository.java     # chưa có repository cho AuditLog
common/exceptions/ErrorCode.java         # thêm mã 1307+
modules/maintenance/service/impl/PendingLeasingRoomTenancyPort.java
    → thay bằng adapter gọi TenancyQueryService (Student 3 là owner của contract này)
```

Mã lỗi mới (dải leasing 13xx, hiện dùng tới 1306):

| Code | Tên | HTTP |
|---|---|---|
| 1307 | `CONTRACT_REPRESENTATIVE_INVALID` — cần đúng 1 người đại diện | 409 |
| 1308 | `CONTRACT_NOT_EDITABLE` — hợp đồng không còn ở trạng thái cho sửa | 409 |
| 1309 | `BOOKING_HOLD_FUNDS_INSUFFICIENT` — chưa đủ tiền cọc đã xác minh | 409 |
| 1310 | `ZALO_URL_INVALID` | 400 |
| 1311 | `ZALO_LINK_UNAVAILABLE` — không có quan hệ thuê hợp lệ / branch tắt Zalo | 403/404 |

Dùng lại: `BOOKING_NOT_FOUND`, `BOOKING_EXPIRED`, `CONTRACT_NOT_FOUND`, `CONTRACT_TENANT_NOT_FOUND`, `INVALID_RENTAL_PERIOD`, `LEASING_STATE_CONFLICT`, `RESOURCE_UNAVAILABLE`, `BRANCH_POLICY_NOT_FOUND`, `CONCURRENT_MODIFICATION`, `UNAUTHORIZED`.

---

## 3. Quyết định thiết kế xuyên suốt

1. **Chống trùng tài nguyên (P1-54/58/62, BR-P10b)** — `ResourceAvailabilityGuard.lockAndAssertFree(roomId, slotId, start, end, excludeBookingIds)`:
   - `select pg_advisory_xact_lock(hashtextextended(:roomId, 0))` → tuần tự hóa mọi thao tác giữ chỗ/hợp đồng trên cùng Room trong transaction.
   - Giải phóng ngay (lazy) các allocation `HELD` của Booking đã quá `expires_at` trên Room đó (đổi Booking → `EXPIRED`), để không phụ thuộc thời điểm job chạy.
   - Query overlap: allocation `status <> RELEASED`, cùng room, `(slot NULL OR other.slot NULL OR slot = other.slot)`, `start < :end AND end > :start` (khoảng `[start, end)`). Điều kiện slot này chính là BR-P10: ROOM chặn bởi mọi slot, SLOT chặn bởi giữ nguyên phòng.
   - **BR-R20b — hết hạn nhưng còn ở**: allocation `LEASED` kết thúc ở `end_date` nên trigger không chặn sau ngày đó. Guard phải coi member `ACTIVE` chưa có `actual_leave_date` của Contract `ACTIVE` đã quá `end_date` là **vẫn chiếm tài nguyên vô thời hạn** (ROOM: cả phòng; SLOT: slot của member) cho tới khi bàn giao trả phòng.
   - Trigger deferred là chốt chặn cuối: lỗi SQLSTATE `23514` xuất hiện **lúc commit** ⇒ gọi `entityManager.flush()` trong service và map `DataIntegrityViolationException`/`JpaSystemException` thông điệp overlap → `RESOURCE_UNAVAILABLE` (bổ sung case trong `GlobalExceptionHandler` nếu chưa có).
2. **Chuyển trạng thái an toàn đồng thời**: `version` là scalar, không có `@Version` ⇒ đọc Booking/Contract bằng `@Lock(PESSIMISTIC_WRITE)` (`findByIdForUpdate`), kiểm tra state machine, `version++`, save. Không dựa vào nút FE.
3. **Lịch sử trạng thái (P1-58, P2-20)**: không có bảng history trong ERD ⇒ ghi `audit_logs` (`table_name='bookings'|'contracts'|'contract_tenants'`, `action='STATUS_CHANGE'`, `detail={from,to,refund?}`, `reason`, `actor_user_id`). Gọi `AuditActorContext.bind(actorId)` đầu mỗi command. Timeline trong `BookingResponse` đọc từ audit này.
4. **Actor & scope**: dùng lại port `TenantAccessPolicy` (đã có trong leasing: `currentUserId`, `canManageBranch`, `canManageRoom`). Không nhận `userId/branchId/ownerId` của actor từ body. Hiện là placeholder fail-closed → cần Student 1 merge implementation thật trước khi test end-to-end.
5. **Snapshot chính sách**: Booking lưu `policy_id` + `policy_snapshot` (jsonb của BranchPolicy hiệu lực lúc tạo) → tính tiền/hạn/hoàn cọc theo snapshot, không theo policy mới.
6. **Quy ước ngày (BR-G01/G04 vs DB)**: nghiệp vụ tính ngày **bao gồm cả hai đầu** (ngày trả phòng vẫn là ngày ở), còn DB dùng `daterange [start, end)` và `CHECK(end_date > start_date)`. ⇒ Lưu `end_date = ngày ở cuối cùng + 1`. API nhận/trả `lastDay` (inclusive) và mapper tự cộng/trừ 1 ngày; `actual_leave_date` = ngày bàn giao + 1 trong DB (CHECK `actual_leave_date > start_date`). Viết một helper duy nhất (`LeasingDates`) để không lệch giữa Booking/Contract/Occupancy.
7. **Múi giờ (BR-P03a)**: hạn theo ngày tính đến 23:59 giờ Việt Nam (`Asia/Ho_Chi_Minh`); hạn phiếu cọc giữ chỗ là **24 giờ kể từ thời điểm tạo** (Instant, không theo ngày lịch). "Hôm nay" khi validate `startDate`, hết hạn hợp đồng… dùng `LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))`, không dùng `ZoneId.systemDefault()`.

---

## 3b. Business rule (`docs/business_rules/br-st3-st5.md`) áp dụng cho Student 3

| Mã BR | Nội dung rút gọn | Nơi áp dụng |
|---|---|---|
| BR-P08 | Cọc giữ chỗ mặc định 1.000.000đ **hoặc % giá thuê** do Owner cấu hình | Task 1. `branch_policies` chỉ có `booking_hold_amount` cố định ⇒ P1 chỉ hỗ trợ số tiền cố định (D6) |
| BR-P09, P03a | Tự kiểm tra khả dụng, **không cần duyệt**; khóa tạm + tạo phiếu cọc, hạn **24h từ lúc tạo** | Task 1, 2 |
| BR-P10 | ROOM: kiểm tra phòng **và mọi slot**; SLOT: kiểm tra slot đó **và** yêu cầu thuê nguyên phòng | `ResourceAvailabilityGuard` |
| BR-P10a | Đủ tiền đã xác minh ⇒ `CONFIRMED`; hết hạn chưa đủ ⇒ `EXPIRED`, mở lại tài nguyên, hoàn phần đã đóng | Task 2 |
| BR-P10b | Kiểm tra + khóa nguyên tử, có khóa/ràng buộc DB; chỉ 1 request xung đột thắng | Mục 3.1 |
| BR-P10c | Tiền về sau hạn ⇒ xử lý ngoại lệ, không tự xác nhận | Task 2, Task 7 |
| BR-P11 | `CONFIRMED` giữ tối đa **14 ngày** từ lúc xác nhận đủ cọc; quá hạn chưa chuyển hợp đồng ⇒ hết hạn, xử lý cọc theo mục B | Task 2 job |
| BR-P12 | Ký hợp đồng ⇒ cọc giữ chỗ thành một phần cọc hợp đồng (tối đa phần còn thiếu của từng ContractTenant, phần vượt là tiền dư BR-P27); **chuyển quyền giữ tài nguyên sang hợp đồng không có khoảng trống**; đã chuyển thì hết chịu hạn 14 ngày | Task 5, D2, D7 |
| Mục B | Bảng hoàn cọc giữ chỗ (5 trường hợp) | `HoldRefundPolicy` — Task 3 |
| BR-P13, P13a, P13b | Cọc hợp đồng mặc định 1 tháng (0–3 tháng), **ghi cố định cho từng ContractTenant**, tổng khớp tổng cọc phòng; phân bổ cọc giữ chỗ cho từng người; người trả ≠ người được ghi cọc | Task 4, D3 |
| BR-P14, P14a, P14b | Bắt đầu thuê xét **từng ContractTenant**: hợp đồng đã hai bên xác nhận + đến ngày bắt đầu + đã thanh toán đủ hóa đơn nhận phòng. Contract `ACTIVE` khi **≥ 1** member đủ điều kiện. Member chưa đủ không làm mất chỗ của họ | Task 5 |
| BR-R02 | Ngày nhận phòng ≥ ngày ký (ngày xác nhận) | Task 4 `/confirm`, Task 5 |
| BR-R04 | Chủ trọ bàn giao muộn ⇒ ngày nhận phòng điều chỉnh theo bàn giao thực tế | Task 5 (sửa `start_date` member còn chờ nhận phòng) |
| BR-R05, R07 | Nhận phòng ngày nào cũng được; không trùng hợp đồng/giữ chỗ khác trên cùng phòng/slot | Task 1, 4 |
| BR-R11, R12 | Trả phòng hợp lệ (đúng hạn / báo trước ≥ 30 ngày / chủ trọ đồng ý) vs vi phạm | Task 8 (P2-17) |
| BR-R17 | Nhắc Owner/Manager **và người đại diện** trước hạn 30 ngày | Task 8 (P2-16) |
| BR-R18, R19 | Gia hạn giữ điều khoản / chỉ đổi giá ⇒ **phụ lục**; đổi phòng, mode, người đại diện ⇒ kết thúc + **hợp đồng mới**, chuyển cọc | Task 8 |
| BR-R20, R20a, R20b, R20c | Không tự gia hạn. Hết hạn + đã trả phòng ⇒ "Hết hạn"; hết hạn còn ở ⇒ "Hết hạn nhưng còn ở", vẫn tính tiền, nhắc hằng tháng, **phòng/slot vẫn bị chiếm**, không tính là vi phạm | Task 5, Task 8, `ResourceAvailabilityGuard` |
| BR-R21 → R25 | Chuyển phòng = kết thúc cũ (D−1) + hợp đồng mới (D); chuyển slot cùng phòng cùng giá chỉ đổi vị trí | Ngoài P0/P1 của Student 3; chỉ cần không chặn luồng này (ghi chú Task 8) |
| BR-P28 → P31 | Giá mới áp ngay cho booking/hợp đồng mới; hợp đồng đang chạy giữ giá, tăng giá qua phụ lục, báo trước 30 ngày, hiệu lực từ đầu kỳ thu kế tiếp | Task 4 (không tự lấy giá mới), Task 8 |
| BR-P17a → c | Hoàn/khấu trừ cọc phải được Owner/Manager duyệt, ghi người nhận, số tiền, lý do, người duyệt; duyệt ≠ đã chi | Student 5 thực hiện; Student 3 chỉ đưa ra quyết định + căn cứ (Task 3, 7) |
| BR-I01 | Hóa đơn nhận phòng lập sau khi hợp đồng được **hai bên xác nhận**, cho member còn chờ nhận phòng | Student 5; Student 3 cung cấp query "member chờ nhận phòng" trong `TenancyQueryService` |

---

## 4. Tasks

### Task 0 — Nền tảng & port liên module
**Files:** `common/audit/AuditLogRepository`, `ErrorCode` (1307–1311), các Repository leasing, `property/repository/BranchRepository|BranchPolicyRepository`, `PropertyQueryService(+Impl)`, `BookingFundsPort` + `PendingPaymentBookingFundsPort`, `TenancyQueryService(+Impl)`, `ResourceAvailabilityGuard`, `LeasingHistoryRecorder`.
- [ ] `PropertyQueryService`: `RoomLeasingView getRoom(roomId)`, `RoomSlotView getSlot(slotId)`, `BranchView getBranch(branchId)`, `BranchPolicyView currentPolicy(branchId)` (version lớn nhất có `effective_from <= now`), `ZaloView getZalo(branchId)`. Read-only record DTO.
- [ ] `BookingFundsPort.verifiedHold(bookingId)` → `{ BigDecimal verifiedAmount, Instant fullyPaidAt }` (D8). Placeholder trả `0` / null (fail-closed: không ai được CONFIRMED khi chưa có S5).
- [ ] `MoveInPaymentPort.isMoveInInvoiceSettled(contractTenantId)` (BR-P14, Student 5 implement). Placeholder trả `false`.
- [ ] `LeasingDates` (quy ước inclusive ↔ `[start, end)`, `today()` theo `Asia/Ho_Chi_Minh`) và `HoldRefundPolicy` (bảng mục B) — class thuần, không phụ thuộc Spring.
- [ ] `TenancyQueryService`: `findActiveContractTenantId(userId, roomId)`, `hasActiveTenancyInBranch(userId, branchId)`, `contractTenantView(id)` (cho Student 5 theo contract-policies §5).
- [ ] Thay `PendingLeasingRoomTenancyPort` bằng adapter gọi `TenancyQueryService`.
- [ ] Repository native: advisory lock, overlap query, expired holds query.

### Task 1 — BE-03.1 Tạo & duyệt Booking (P1-49, P1-50, P1-51)
- [ ] `POST /api/bookings` (User đã đăng nhập, **không cần là Tenant**):
  - Validate: `startDate >= today (giờ VN)` — ngày nào trong tháng cũng được (BR-R05); `lastDay >= startDate` (`INVALID_RENTAL_PERIOD`); Room `AVAILABLE`, Branch `ACTIVE`; `rentalMode` lấy từ Room (không tin client); ROOM ⇒ `roomSlotId` null; SLOT ⇒ slot thuộc room và `AVAILABLE`; `appointmentId` (tùy chọn) phải của chính requester — không bắt buộc có lịch hẹn.
  - Lấy policy hiện hành → `holdAmount = policy.bookingHoldAmount` (P1-50, BR-P08; không hardcode 1.000.000), `paymentDueAt = createdAt + paymentWindowMinutes` và `temporaryHoldUntil = createdAt + temporaryHoldMinutes` (seed/policy mặc định 1440 phút = **24h từ lúc tạo**, BR-P09/P03a). Không cho requester có 2 booking đang mở trùng tài nguyên/thời gian.
  - `ResourceAvailabilityGuard` → tạo Booking `AWAITING_PAYMENT` + `ResourceAllocation(HELD, booking_id, expires_at=temporaryHoldUntil)` trong **một transaction** (BR-P09/P10).
  - Response kèm `holdAmount`, `paymentDueAt`, `policySnapshot` (thông báo mức cọc phải trả).
- [ ] `GET /api/bookings/my`, `GET /api/bookings/{id}` (requester hoặc Owner/Manager của branch).
- [ ] `GET /api/bookings?branchId&status&roomId...` (Owner/Manager, `BookingSpecifications` + Pageable, `PagedResponse`).
- [ ] `POST /api/bookings/{id}/reject` (Owner/Manager; từ `AWAITING_PAYMENT|CONFIRMED`) → `REJECTED`, release allocation, outcome `FULL_REFUND` nếu đã có tiền (mục B: chủ trọ từ chối ⇒ 100%).
- [ ] "Duyệt" (P1-51): theo BR-P09 hệ thống tự duyệt khả dụng; Manager chỉ **từ chối** hoặc xem. Không có endpoint để Manager tự set `CONFIRMED` (C12). `REQUESTED` không dùng ở P1 (xem Quyết định mở D1).

### Task 2 — BE-03.2 Thanh toán cọc & khóa khả dụng (P1-52 → P1-55)
- [ ] `GET /api/bookings/{id}/payment-instruction` (requester): `amount = holdAmount − verifiedHoldAmount`, `reference = "BK" + 10 ký tự từ bookingId (uppercase, không dấu)`, `paymentDueAt`, `qrUrl` của Branch, `environment`. Reference là deterministic nên idempotent; Student 5 dùng reference này để khớp Payment (`payments.booking_id`, `reference_text`).
- [ ] `BookingService.confirmHold(bookingId)` — API công khai cho Student 5 gọi **sau khi** Payment VERIFIED:
  - Lock Booking; chỉ khi `AWAITING_PAYMENT` và `now <= paymentDueAt`; `BookingFundsPort.verifiedHoldAmount >= holdAmount` (P1-53) — nếu không: `BOOKING_HOLD_FUNDS_INSUFFICIENT`.
  - → `CONFIRMED`, `confirmedHoldUntil = now + confirmedHoldMinutes` (14 ngày, BR-P11), cập nhật `allocation.expires_at = confirmedHoldUntil`. Idempotent nếu đã `CONFIRMED`.
  - Tiền đến sau hạn: không xác nhận (BR-P10c; trigger DB cũng buộc payment để unallocated).
- [ ] `BookingExpiryJob` (`@Scheduled`, mỗi 1 phút, pattern như `ThreadAutoCloseJob`): `AWAITING_PAYMENT` quá `temporaryHoldUntil` và `CONFIRMED` quá `confirmedHoldUntil` → `EXPIRED`, allocation `RELEASED` (`released_at`), audit với outcome (`REFUND_PAID_PART` nếu đã đóng một phần; `NO_REFUND` khi hết 14 ngày do khách). Xử lý từng booking trong transaction riêng + lock để không đụng `confirmHold` chạy song song.

### Task 3 — BE-03.3 Hủy, liên kết hợp đồng, lịch sử (P1-56 → P1-58)
- [ ] `POST /api/bookings/{id}/cancel` (requester, hoặc Manager kèm lý do bắt buộc): từ `AWAITING_PAYMENT|CONFIRMED` → `CANCELLED`, `cancellation_reason`, release allocation, tính `HoldRefundOutcome` bằng một class `HoldRefundPolicy` (thuần, dễ unit test) theo **bảng mục B**:

  | Tình huống | Trạng thái kết thúc | Outcome |
  |---|---|---|
  | Khách hủy ≤ 24h kể từ khi đóng đủ cọc (mốc = thời điểm nhận khoản tiền hoàn tất cọc đã xác minh; tạm dùng thời điểm `CONFIRMED`, xem D8) | `CANCELLED` | `FULL_REFUND` |
  | Khách hủy > 24h, hoặc không đến nhận phòng (hết 14 ngày do phía khách) | `CANCELLED` / `EXPIRED` | `NO_REFUND` |
  | Chủ trọ từ chối / không bàn giao được phòng | `REJECTED` | `FULL_REFUND` |
  | Hết hạn đóng cọc khi mới đóng một phần | `EXPIRED` | `REFUND_PAID_PART` (100% phần đã đóng) |
  | Khách hủy khi chưa đóng đồng nào | `CANCELLED` | không phát sinh |
  | Tiền về sau khi đã hết hạn | (Booking giữ nguyên terminal) | `MANAGER_REVIEW` → Task 7 |

  Outcome trả về trong response + lưu audit. Đây chỉ là **quyết định theo chính sách**; việc duyệt và chi hoàn/khấu trừ do Student 5 thực hiện theo BR-P17a–c (phải có người duyệt, người nhận, chứng từ).
- [ ] `GET /api/bookings/{id}/history` → `StatusHistoryResponse[]` từ `audit_logs`.
- [ ] Liên kết hợp đồng (P1-57): `POST /api/bookings/{id}/convert` = tạo Contract **DRAFT** điền sẵn từ Booking `CONFIRMED` (room, slot, mode, dates, requester làm member). Khách không cần có hợp đồng trước mới được đặt. Cơ chế chuyển Booking → `CONVERTED` xảy ra ở Task 5 (xem D2).

### Task 4 — BE-03.4 Contract & người tham gia (P1-59 → P1-63)
- [ ] `POST /api/contracts` (tạo trực tiếp) và `POST /api/bookings/{id}/convert` (từ booking) → `ContractServiceImpl.createDraft(...)`:
  - Owner/Manager có quyền branch; `ownerId`, `branchId` lấy từ Room/Branch; `code` sinh `HD-{yyMMdd}-{4 ký tự}` + retry khi vi phạm `UNIQUE(branch_id, code)`; `createdBy` = actor.
  - Kiểm tra trước khả dụng bằng `ResourceAvailabilityGuard` (loại trừ allocation của chính các booking đang được chuyển) để báo sớm xung đột (P1-62). Contract DRAFT **chưa** tạo allocation (DB chỉ cho `LEASED` khi `ACTIVE`).
- [ ] `PATCH /api/contracts/{id}` — chỉ khi `DRAFT` và `document_status <> CONFIRMED`, ngược lại `CONTRACT_NOT_EDITABLE`.
- [ ] Điều khoản (P1-61) trên từng `ContractTenant`: `monthlyRent`, `firstMonthRent`, `lastMonthRent`, `prepaidFirstRequired = firstMonthRent`, `prepaidLastRequired = lastMonthRent` (CHECK DB), `terms`; giá chốt KHÔNG mặc định bằng `Room.referencePrice` (chỉ gợi ý, BR-P28: giá hiện hành của phòng lúc tạo). Điều khoản chung ở `Contract.terms`. Ngày member nằm trong khoảng Contract.
- [ ] Cọc hợp đồng (BR-P13/P13a/P14b): mỗi member có **mức cọc cố định riêng**; mặc định = 1 tháng tiền thuê đã chốt của member (Owner chọn 0–3 tháng); ROOM nhiều người: tổng các phần phải khớp tổng cọc phòng đã thỏa thuận, không chia đều mặc định. Phần cọc giữ chỗ chuyển sang: `min(holdVerified, cọc còn thiếu của member)`, phần vượt là tiền dư (BR-P27, Student 5). Nghĩa vụ tiền thuê của từng member phải cộng lại khớp tổng tiền phòng. ⚠ Schema chưa có cột cọc hợp đồng — **chặn bởi D3**.
- [ ] Ngày ký = ngày `/confirm`; validate `startDate` mỗi member ≥ ngày ký (BR-R02).
- [ ] Mode (P1-62): ROOM ⇒ member không có slot; SLOT ⇒ mỗi member có slot thuộc room, các member không trùng slot+ngày; số member không vượt `capacity`.
- [ ] Người tham gia (P1-63): `POST /api/contracts/{id}/tenants`, `DELETE /api/contracts/{id}/tenants/{ctId}` (DRAFT ⇒ xóa mềm `CANCELLED`), `POST /api/contracts/{id}/tenants/{ctId}/representative`. Kiểm tra đúng 1 `REPRESENTATIVE` khi activate; `tenant_snapshot.full_name` bắt buộc khi khác DRAFT (lấy từ User qua service identity).
- [ ] `POST /api/contracts/{id}/confirm` → `document_status = CONFIRMED`, ghi `confirmation_evidence` (không OTP/PDF — P1-67) ⇒ "chờ hiệu lực".

### Task 5 — BE-03.5 Hiệu lực hợp đồng & cư trú (P1-64 → P1-66)
- [ ] State machine (P1-64), DB status + trạng thái suy ra:

  | Từ | Lệnh | Tới | Điều kiện |
  |---|---|---|---|
  | DRAFT | `PATCH` | DRAFT | doc chưa CONFIRMED |
  | DRAFT | `/confirm` | DRAFT + doc CONFIRMED | đủ member, đúng 1 đại diện |
  | DRAFT(doc CONFIRMED) | `/activate` | ACTIVE | ≥ 1 member đủ điều kiện (BR-P14a), không trùng |
  | DRAFT | `/cancel` | CANCELLED | lý do bắt buộc |
  | ACTIVE | `/end` | ENDED | mọi member ACTIVE → LEFT với `actual_leave_date` |
  | ENDED/CANCELLED | bất kỳ sửa | — | `CONTRACT_NOT_EDITABLE` |

  Trạng thái hiển thị (field suy ra `lifecycleStatus` trong response, **không** đổi CHECK DB):
  - `DRAFT` + doc chưa CONFIRMED ⇒ **Nháp**; `DRAFT` + doc CONFIRMED ⇒ **Chờ hiệu lực** (thuật ngữ BR).
  - `ACTIVE` và hôm nay ≤ ngày cuối ⇒ **Đang hoạt động**.
  - `ACTIVE`, quá ngày cuối, còn member chưa bàn giao ⇒ **Hết hạn nhưng còn ở** (BR-R20a): không tự đổi `end_date`, không tự gia hạn, tài nguyên vẫn bị chiếm (BR-R20b), Student 5 vẫn lập hóa đơn tháng theo điều khoản cuối (BR-I02).
  - Mọi member đã bàn giao trả phòng ⇒ `ENDED` ⇒ **Hết hạn / Đã kết thúc** (BR-R20). Không coi ở quá hạn là vi phạm hay mất cọc (BR-R20c).
- [ ] `/activate` trong **một transaction** (P1-66, BR-P12):
  1. Lock Contract + `ResourceAvailabilityGuard` theo room.
  2. Với mỗi member có booking nguồn `CONFIRMED` (D2): Booking → `CONVERTED`, allocation HELD → `RELEASED`, set `ct.source_booking_id`.
  3. Tạo allocation `LEASED`: ROOM ⇒ 1 allocation (`contract_id`, slot null); SLOT ⇒ mỗi member 1 allocation (`contract_tenant_id`, slot).
  4. Member đủ điều kiện → `ACTIVE` + `RoomSlotOccupancy(start, end)`; member chưa đủ ở lại `DRAFT` (chờ nhận phòng).
  5. Contract → `ACTIVE`; audit. Trigger deferred kiểm tra toàn bộ lúc commit — flush & map lỗi.
  - Điều kiện bắt đầu thuê xét **riêng từng member** (BR-P14): (a) hợp đồng đã hai bên xác nhận, (b) đã đến `startDate` của member (giờ VN), (c) Student 5 xác minh đã thanh toán đủ **hóa đơn nhận phòng** của member (tiền thuê tháng đầu theo ngày, dịch vụ cố định tháng đầu, phần cọc còn thiếu). Thêm port `MoveInPaymentPort.isMoveInInvoiceSettled(contractTenantId)` (Student 5 implement, placeholder trả false). DB cũng có trigger yêu cầu Payment prepaid VERIFIED.
  - Contract `ACTIVE` ngay khi **≥ 1** member đủ điều kiện (BR-P14a); member khác vẫn `DRAFT` (chờ nhận phòng) và được kích hoạt sau qua `POST /api/contracts/{id}/tenants/{ctId}/activate`. Member chưa trả **không** làm giảm tiền phòng hay cho người ngoài giữ chỗ của họ (BR-P14b) — xem D7.
  - Tự động: `ContractActivationJob` (hằng ngày 00:05 giờ VN) + `ContractService.onMoveInInvoiceSettled(ctId)` để Student 5 gọi khi xác minh xong ⇒ kích hoạt member đủ điều kiện mà không cần Manager bấm. `/activate` thủ công vẫn giữ nhưng chỉ chạy được khi đủ điều kiện.
  - BR-R04: member còn chờ nhận phòng mà chủ trọ bàn giao muộn ⇒ `PATCH .../tenants/{ctId}/move-in-date` (Owner/Manager, có lý do) dời `startDate` theo ngày bàn giao thực tế; ghi audit; Student 5 tính lại hóa đơn nhận phòng nếu chưa phát hành.
  - Chỉ test end-to-end được khi Student 5 xong hoặc với seed `06_seed_demo.sql`.
- [ ] Quyền xem (P1-65): `GET /api/contracts/my`, `GET /api/contracts/my/{id}` (Tenant: chỉ contract có mình là member; chỉ trả điều khoản riêng của chính mình, không lộ giá của bạn cùng phòng — C27). `GET /api/contracts`, `GET /api/contracts/{id}` cho Owner/Manager theo branch scope.

### Task 6 — BE-03.6 Zalo Group của Branch (P1-120 → P1-122)
- [ ] (property) `PUT /api/branches/{branchId}/zalo` body `{zaloGroupUrl, zaloEnabled}`; `GET` cho Owner/Manager. Validate (P1-121): `https`, host `zalo.me` hoặc `chat.zalo.me`, path bắt đầu `/g/`, độ dài ≤ 500 ⇒ sai: `ZALO_URL_INVALID`. Bật mà URL rỗng ⇒ lỗi. Vô hiệu hóa = `zaloEnabled=false` (giữ URL). Ghi audit.
- [ ] (leasing) `GET /api/tenant/zalo` → danh sách `ZaloLinkResponse` của các Branch mà caller có ContractTenant `ACTIVE` trong Contract `ACTIVE`; `GET /api/tenant/zalo/{branchId}` → 403 `ZALO_LINK_UNAVAILABLE` nếu không có quan hệ hoặc branch tắt. Không đưa link vào API Branch public. Không gọi Zalo API.

### Task 7 — Phase 2: BE-03.7 Tiền cọc nâng cao (P2-13)
- [ ] Chuyển cọc giữ chỗ sang hợp đồng ở bước activate: trả cho Student 5 dữ liệu `bookingId → contractTenantId` (DB đã cho Payment prepaid trace qua `source_booking_id`).
- [ ] Luồng ngoại lệ "tiền về sau khi hết hạn" (BR-P10c, mục B dòng cuối): vì Booking terminal không mở lại được, Manager "khôi phục giữ phòng" = tạo **Booking mới** cho cùng requester qua `ResourceAvailabilityGuard` (chỉ khi tài nguyên còn trống) và Student 5 phân bổ khoản tiền REVIEW sang booking mới; nếu đã có người khác giữ/thuê ⇒ outcome `FULL_REFUND`.
- [ ] BR-P13a/P13b: một khoản cọc giữ chỗ dùng cho nhiều member ⇒ ghi rõ số phân bổ cho từng người (mỗi phần ≤ cọc còn thiếu của người đó, tổng ≤ tiền thực thu đã xác minh); lưu riêng người thực trả và người được ghi cọc.
- [ ] `GET /api/bookings/{id}/deposit-ledger`: đối chiếu hold amount, verified, outcome, refund (đọc từ port S5 + audit).

### Task 8 — Phase 2: BE-03.8 Tra cứu, gia hạn, trả phòng (P2-15/16/17/20)
- [ ] P2-15 `ContractSpecifications`: tenantUserId (join contract_tenants), roomId, branchId, start/end range, status; Pageable.
- [ ] P2-16 `ContractExpiryReminderJob` (hằng ngày, giờ VN): contract ACTIVE còn đúng 30 ngày tới ngày cuối → publish `NotificationEvent` (module notification đã có listener) cho Owner/Manager **và người đại diện** (BR-R17); ngày hết hạn mà còn người ở ⇒ cảnh báo Owner/Manager và nhắc lại **mỗi tháng** tới khi gia hạn hoặc trả phòng (BR-R20a). Dùng `eventKey` theo `contractId + mốc` để chạy lại job không gửi trùng.
- [ ] Gia hạn / điều chỉnh (BR-R18, BR-P29/P30): `POST /api/contracts/{id}/amendments` (DRAFT, `amendment_number` tăng dần, `previous_terms`/`new_terms` jsonb) → `/approve` | `/reject`. Chỉ cho: (a) kéo dài ngày cuối (giữ điều khoản), (b) đổi giá thuê. Đổi giá: phải báo trước ≥ 30 ngày, `effective_from` = **ngày 1 của kỳ thu đầu tiên sau thời hạn báo trước** (VD báo 10/11 ⇒ hiệu lực 01/01), không đổi giữa kỳ, không sửa hóa đơn đã phát hành (BR-P31). Approve gia hạn ⇒ cập nhật `end_date` contract + member, nối allocation/occupancy (kiểm tra overlap); với hợp đồng đang "hết hạn nhưng còn ở" không tính trùng phần đã ghi nhận (BR-R20c).
- [ ] Đổi phòng / mode / người đại diện ⇒ từ chối amendment, trả lỗi hướng dẫn kết thúc hợp đồng cũ + tạo hợp đồng mới, cọc chuyển sang (BR-R19). Chuyển phòng giữa tháng (BR-R21–R24) và chuyển slot cùng phòng cùng giá (BR-R25: chỉ cập nhật occupancy) ghi nhận là mở rộng sau, không nằm trong mã P2 được giao.
- [ ] P2-17 Trả phòng: dùng cột move-out trên `ContractTenant`: `POST .../tenants/{ctId}/move-out` (Tenant báo `requested_leave_date`, lý do) → hệ thống phân loại sơ bộ **hợp lệ** (trả đúng ngày cuối, hoặc báo trước ≥ 30 ngày) hay **vi phạm** (BR-R11/R12) → Manager `approve` (có thể đánh dấu "chủ trọ đồng ý" ⇒ hợp lệ) | `reject` → hoàn tất sau handover (Student 6 `HandoverService` đã có) ⇒ member `LEFT`, `actual_leave_date` = ngày bàn giao (ngày bàn giao vẫn là ngày ở, BR-G04), cắt occupancy/allocation; hết member ACTIVE ⇒ Contract `ENDED`. Gửi cho Student 5: loại trả phòng + ngày bàn giao để lập hóa đơn quyết toán (BR-R13–R16, BR-P15/P16); Student 3 không tự tính hoàn tiền.
- [ ] P2-20: chặn mọi command trên `ENDED/CANCELLED`; mọi thay đổi điều khoản sau ACTIVE chỉ qua amendment; lịch sử ở `audit_logs` + `contract_amendments`.

### Task 9 — Kiểm thử
- [ ] Unit (Mockito) cho `BookingServiceImpl`, `ContractServiceImpl`: state machine, tính hold/deadline, đúng 1 đại diện, quyền tenant/manager.
- [ ] Unit `HoldRefundPolicy`: đủ 6 dòng bảng mục B, biên đúng 24h (23:59:59 vs 24:00:01).
- [ ] Unit `LeasingDates`: ở 18/10 → 31/10 = 14 ngày (BR-G01), ngày bàn giao được tính là ngày ở (BR-G04), mốc "hôm nay" lúc 23:30 UTC = ngày hôm sau giờ VN.
- [ ] Ví dụ trong BR làm test case: báo tăng giá 10/11 ⇒ hiệu lực 01/01 (BR-P30); hợp đồng hết hạn còn ở vẫn chặn booking mới (BR-R20b); member A đủ điều kiện, B chưa trả ⇒ Contract ACTIVE, B giữ chỗ (BR-P14a/b).
- [ ] Integration trên PostgreSQL local đã chạy `docs/database/rental/00→06` (H2 không có trigger/jsonb/advisory lock): 2 request đặt cùng slot song song ⇒ đúng 1 thành công; ROOM vs SLOT xung đột chéo; hold hết hạn được release và đặt lại được; confirm khi thiếu tiền bị chặn; activate tạo allocation/occupancy hợp lệ qua trigger.
- [ ] `./mvnw -q compile` + test trước mỗi PR.

---

## 5. Thứ tự làm & phụ thuộc

| Mốc | Task | Mã | Phụ thuộc |
|---|---|---|---|
| M0 | Task 0 | — | Student 1: auth thật cho `TenantAccessPolicy`; property repository Branch/Policy |
| M1 | Task 1 | P1-49, 50, 51, 54 | M0 |
| M2 | Task 2 | P1-52, 53, 55 | M1; Student 5 implement `BookingFundsPort` + gọi `confirmHold` |
| M3 | Task 3 | P1-56, 57, 58 | M1 |
| M4 | Task 4 | P1-59 → 63 | M0 |
| M5 | Task 5 | P1-64 → 67 | M3, M4; Student 5 cho điều kiện prepaid |
| M6 | Task 6 | P1-120 → 122 | M5 (`TenancyQueryService`) |
| M7 | Task 7–8 | P2-13, 15, 16, 17, 20 | M5; Student 5/6 |

Gợi ý nhánh: mỗi mốc một PR nhỏ từ `feature/booking` → `dev`.

---

## 6. Quyết định cần chốt với nhóm

| # | Vấn đề | Khuyến nghị |
|---|---|---|
| D1 | P1-51 "Owner/Manager chấp thuận" vs BR-P09 "không cần duyệt". DB không cho Booking `REQUESTED` giữ allocation ⇒ nếu bắt duyệt thì không chống được đặt trùng lúc chờ duyệt. | Theo BR-P09: tạo thẳng `AWAITING_PAYMENT`; Manager chỉ từ chối. |
| D2 | `contract_tenants.source_booking_id` chỉ được ghi khi Booking đã `CONVERTED`, và allocation của Contract chỉ được `LEASED` khi Contract `ACTIVE` ⇒ không lưu được liên kết booking trong lúc Contract còn DRAFT. | Booking giữ `CONFIRMED` + allocation HELD trong lúc DRAFT; lúc `/activate` tìm Booking `CONFIRMED` khớp (requester = member, room, slot, environment) hoặc nhận `bookingId` trong request activate, rồi chuyển `CONVERTED` cùng transaction. Hạn 14 ngày áp dụng tới khi hợp đồng được hai bên xác nhận; sau đó Booking được gia hạn giữ chỗ tới `startDate` (BR-P12, xem D7). |
| D3 | **Mâu thuẫn tài liệu**: `br-st3-st5.md` (Câu 9C, BR-R09, BR-P12–P17) quyết định có **cọc hợp đồng** riêng từng ContractTenant, cọc giữ chỗ chuyển thành một phần cọc hợp đồng. Nhưng schema chỉ có PREPAID_RENT `first/last_month_rent`, không có cột cọc hợp đồng; `contract-policies.md` lại ghi "PREPAID_RENT không phải tiền đặt cọc" và còn Q1–Q5 **OPEN**. | Business rule là quyết định đã chốt ⇒ cần **migration có version** thêm cột cọc hợp đồng trên `contract_tenants` (vd `security_deposit numeric(19,0) NOT NULL DEFAULT 0 CHECK >= 0`) + nguồn cọc ở `payments`/`invoice_items` của Student 5. Nhóm (Student 3 + 5) chốt trước Task 4/5. Trong lúc chờ, code Task 4 để field cọc trong DTO nhưng chưa persist. |
| D6 | BR-P08 cho phép cọc giữ chỗ **theo % giá thuê**, nhưng `branch_policies.booking_hold_amount` chỉ là số tiền cố định (`> 0`). | P1 chỉ hỗ trợ số cố định; % là mở rộng cần migration (policy version mới) — báo Student 1 (owner BranchPolicy). |
| D7 | BR-P12 + BR-P14b: giữ chỗ phải chuyển sang hợp đồng **không có khoảng trống**, và member chưa nhận phòng vẫn giữ chỗ của mình. Nhưng DB chỉ cho allocation hợp đồng `LEASED` khi Contract `ACTIVE` (SLOT: member cũng phải `ACTIVE`); Booking `CONFIRMED` lại hết hạn sau 14 ngày. ⇒ Contract chờ hiệu lực quá 14 ngày, hoặc member SLOT chưa trả tiền khi Contract đã ACTIVE, sẽ mất chỗ. | Trong lúc DRAFT: giữ allocation HELD của Booking và **gia hạn `confirmed_hold_until`/`expires_at` tới `startDate`** khi hợp đồng đã được hai bên xác nhận (Booking đã gắn hợp đồng thì không còn hạn 14 ngày theo BR-P12). Hợp đồng tạo trực tiếp không qua Booking: cần cách giữ chỗ cho bản chờ hiệu lực — đề xuất nới trigger cho phép allocation `HELD` thuộc `contract_id` khi Contract `DRAFT` + doc CONFIRMED (migration, chốt cùng D3). |
| D8 | Mục B "hủy trong 24h kể từ khi **đóng cọc**": mốc là lúc tiền về (`payments.received_at`) hay lúc hệ thống xác minh (`CONFIRMED`)? | Dùng `received_at` của khoản tiền làm đủ cọc (BR-P20b: ngày ghi nhận căn cứ thời điểm nhận tiền được xác minh, không phải lúc bấm xác nhận) ⇒ `BookingFundsPort` trả thêm `fullyPaidAt`. |
| D4 | Trạng thái xử lý hoàn cọc khi hủy không có cột trên `bookings`. | Lưu `HoldRefundOutcome` trong `audit_logs.detail`; thực hiện hoàn ở Payment (Student 5). |
| D5 | Zalo ghi vào `Branch` (module property, Student 1). | Student 3 viết `BranchZaloService` trong property, báo Student 1 review; leasing chỉ đọc qua `PropertyQueryService`. |
