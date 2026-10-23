# Student 3 — Theo dõi tiến độ Backend (Booking / Contract / Zalo)

> Plan gốc: [2026-10-20-student3-booking-contract-zalo.md](../plans/2026-10-20-student3-booking-contract-zalo.md)
> Nhánh: `feature/booking` · Cập nhật: 24/10/2026

## 1. Tổng quan

| Task | Mã | Trạng thái code | Commit |
|---|---|---|---|
| Task 0 — Nền tảng & port liên module | — | ✅ Xong | `67143bf` |
| Task 1 — Tạo & duyệt Booking | P1-49, 50, 51 (+ P1-54) | ✅ Xong | `67143bf` |
| Task 2 — Thanh toán cọc & khóa khả dụng | P1-52, 53, 55 | ✅ Xong | `69fabb5` |
| Task 3 — Hủy booking, liên kết hợp đồng, lịch sử | P1-56, 57, 58 | ✅ Xong | chưa commit |
| Task 4 — Contract & người tham gia | P1-59 → 63 | ⏳ Chưa làm (khung `ContractService` đã có) | — |
| Task 5 — Hiệu lực hợp đồng & cư trú | P1-64 → 67 | ⏳ Chưa làm | — |
| Task 6 — Zalo Group | P1-120 → 122 | ⏳ Chưa làm | — |
| Task 7 — Cọc nâng cao (P2) | P2-13 | ⏳ Chưa làm | — |
| Task 8 — Tra cứu, gia hạn, trả phòng (P2) | P2-15, 16, 17, 20 | ⏳ Chưa làm | — |

"Xong" nghĩa là **đã code và build được** (`./mvnw compile`). Không có test tự động cho phần này (theo yêu cầu), và **chưa chạy thử trên database thật**.

## 2. Điều kiện chung đang chặn khi chạy thật

Áp dụng cho **mọi** endpoint ở mục 3, kể cả những dòng ghi "dùng được":

1. **Auth (Student 1).** `PendingSecurityTenantAccessPolicy` là bản tạm: `currentUserId()` luôn ném `UNAUTHENTICATED` và `canManageBranch()` luôn trả `false`. ⇒ Hiện **mọi request vào `/api/bookings/**` đều trả 401**, và mọi thao tác của Owner/Manager sẽ trả 403 cho tới khi có implementation thật của `TenantAccessPolicy`.
2. **Chưa kiểm trên DB thật.** Các câu JPQL/native query (khóa phòng `pg_advisory_xact_lock`, tìm trùng, tìm giữ chỗ quá hạn, dò mã tham chiếu) mới chỉ compile; chưa chạy trên Postgres có schema `rental`. Nên chạy trên Postgres local dựng từ `docs/database/rental/00 → 06` trước khi merge (không chạy vào Supabase dùng chung).
3. **Dữ liệu cấu hình bắt buộc.** Branch phải `ACTIVE`, Room `AVAILABLE` (Slot `AVAILABLE` nếu thuê slot) và Branch phải có ít nhất một `branch_policies` đã hiệu lực — không có thì trả `BRANCH_POLICY_NOT_FOUND`, không dùng số mặc định.
4. **Lỗi phát hiện lúc commit.** Trigger chống trùng của DB là loại *deferred* (chạy lúc commit). Nếu nó bắt được trùng, API trả `DATA_CONFLICT` (409) chứ không phải `RESOURCE_UNAVAILABLE`. Trường hợp thường gặp đã được khóa phòng + kiểm tra trước nên vẫn trả `RESOURCE_UNAVAILABLE`.

## 3. Endpoint — cái nào dùng được, cái nào chưa

Ký hiệu: ✅ dùng được ngay khi có auth (mục 2.1) · ⚠️ chạy được nhưng kết quả bị giới hạn · ⛔ chưa dùng được trong thực tế

| Endpoint | Mã | Trạng thái | Ghi chú |
|---|---|---|---|
| `POST /api/bookings` | P1-49, 50, 54 | ✅ | Tạo booking `AWAITING_PAYMENT` + giữ chỗ `HELD` trong một transaction. Tiền cọc và hạn lấy từ policy của Branch. Body dùng `lastDay` (ngày ở cuối, tính cả ngày đó) — **khác FE đang dự kiến `endDate`**, cần báo FE. |
| `GET /api/bookings/my` | P1-51 | ✅ | Booking của chính người gọi. |
| `GET /api/bookings/{id}` | P1-51 | ✅ | Người đặt hoặc Owner/Manager của Branch. |
| `GET /api/bookings?branchId=…` | P1-51 | ✅ | Cần quyền trên Branch (phụ thuộc `canManageBranch` thật). |
| `POST /api/bookings/{id}/reject` | P1-51 | ⚠️ | Chạy đúng, nhưng `holdRefundOutcome` luôn là `NOTHING_PAID` vì chưa có tiền nào được xác minh (xem mục 4). |
| `GET /api/bookings/{id}/payment-instruction` | P1-52 | ⚠️ | Trả mã tham chiếu `BK…`, hạn, QR của Branch. `verifiedAmount` luôn 0 cho tới khi Student 5 xong; `qrUrl` null nếu Branch chưa cấu hình QR. |
| `POST /api/bookings/{id}/cancel` | P1-56 | ⚠️ | Hủy + lưu lý do + quyết định hoàn cọc theo bảng mục B. Hiện luôn ra `NOTHING_PAID` vì chưa có tiền xác minh. Việc **chi** hoàn tiền là của Student 5. |
| `GET /api/bookings/{id}/history` | P1-58 | ✅ | Đọc từ `audit_logs` do trigger `audit_change` của DB ghi. Phụ thuộc trigger này có trên DB đang dùng (có trong `02_phase1_integrity.sql`). |
| `POST /api/bookings/{id}/convert` | P1-57 | ⛔ | Chỉ chạy với booking `CONFIRMED`, mà booking **chưa thể lên `CONFIRMED`** cho tới khi Student 5 xong (mục 4). Chỉ thử được bằng dữ liệu seed hoặc sửa tay DB. |

## 4. API nội bộ cho module khác

| API | Dành cho | Trạng thái | Ghi chú |
|---|---|---|---|
| `BookingService.confirmHold(bookingId)` | Student 5 | ⛔ | Gọi sau khi Payment `VERIFIED`. Chỉ xác nhận khi `BookingFundsPort` báo đủ tiền và tiền về trước `paymentDueAt`. Hiện port trả 0 ⇒ luôn báo `BOOKING_HOLD_FUNDS_INSUFFICIENT`. |
| `BookingService.findBookingIdByPaymentReference(ref)` | Student 5 | ✅ | Dò booking từ nội dung chuyển khoản `BK` + 12 hex. Sai định dạng / không thấy / trùng ⇒ rỗng (đưa vào duyệt tay). |
| `BookingFundsPort` | Student 5 **implement** | ⛔ | Bản tạm `PendingPaymentBookingFundsPort` luôn trả 0 đồng, `fullyPaidAt = null`. Student 5 cần trả tổng `allocated_amount` VERIFIED và `received_at` của khoản làm đủ cọc. |
| `MoveInPaymentPort` | Student 5 **implement** | ⛔ (chưa dùng tới) | Dành cho Task 5 (BR-P14). Bản tạm luôn `false`. |
| `TenancyQueryService` | maintenance, billing, Zalo | ⚠️ | Đã dùng thật trong maintenance (`LeasingRoomTenancyAdapter` thay `PendingLeasingRoomTenancyPort`). Nhưng chỉ tìm thấy người thuê khi có Contract `ACTIVE` — chưa có luồng nào tạo ra (Task 5), nên hiện chỉ đúng với dữ liệu seed. |
| `PropertyQueryService` (module property) | leasing | ✅ | Đọc Room, Slot, policy hiện hành / theo id, QR thanh toán của Branch. |

## 5. Job chạy nền

| Job | Trạng thái | Ghi chú |
|---|---|---|
| `BookingExpiryJob` (P1-55) | ✅ chạy ngay khi app khởi động | Mỗi 60s (`app.leasing.booking-expiry-interval-ms`), tối đa 200 booking/lần, mỗi booking một transaction. Booking quá hạn giữ tạm mà tiền đã về đủ đúng hạn sẽ được **xác nhận**, còn lại chuyển `EXPIRED` và giải phóng chỗ. ⚠️ Vì `BookingFundsPort` đang trả 0, **mọi** booking `AWAITING_PAYMENT` quá hạn sẽ thành `EXPIRED`. Lưu ý job chạy trên DB mà app đang kết nối — kể cả dữ liệu seed/dùng chung. |

Ngoài job, giữ chỗ quá hạn của một phòng cũng được xử lý ngay khi có người đặt phòng đó.

## 6. Cấu hình mới

| Key | Mặc định | Ý nghĩa |
|---|---|---|
| `app.leasing.payment-environment` (`LEASING_PAYMENT_ENVIRONMENT`) | `SANDBOX` | Môi trường tiền gắn vào Booking/Contract mới. Client không được chọn. |
| `app.leasing.booking-expiry-interval-ms` | `60000` | Chu kỳ job hết hạn giữ chỗ. |
| `app.leasing.booking-expiry-initial-delay-ms` | `60000` | Độ trễ lần chạy đầu. |

## 7. Vấn đề mới phát hiện khi code (cần chốt trước Task 4/5)

| # | Vấn đề | Ảnh hưởng | Đề xuất |
|---|---|---|---|
| D9 | Cột `document_status` của `contracts` là của **phase 3** và có CHECK `ck_document_complete`: khác `DRAFT` thì bắt buộc có file tài liệu (public_id, sha256, version, snapshot). Phase 1 không làm PDF (P1-67). | Plan định dùng `document_status = CONFIRMED` để biểu diễn "hai bên đã xác nhận / chờ hiệu lực" ⇒ **không làm được** ở Phase 1. | Lưu xác nhận hai bên vào `confirmation_evidence` (mảng jsonb, không bị CHECK này ràng buộc), giữ `document_status = null`. Chốt với nhóm. |
| D10 | Hủy một booking đã có hợp đồng nháp từ `/convert` thì hợp đồng nháp vẫn còn. | Hợp đồng nháp "mồ côi". | Task 4: khi hủy booking thì hủy luôn hợp đồng nháp tương ứng, hoặc chặn hủy booking khi đã có nháp. |
| D11 | Hợp đồng nháp chưa lưu được `source_booking_id` (trigger yêu cầu booking đã `CONVERTED`). | Liên kết booking ↔ nháp hiện được tìm lại theo (khách, phòng, slot, ngày bắt đầu). | Giữ cách này (D2); ghi `source_booking_id` lúc kích hoạt ở Task 5. |
| D3, D7 | Cọc hợp đồng chưa có cột; giữ chỗ khi hợp đồng chờ hiệu lực. | Chặn một phần Task 4, 5, 7. | Xem plan mục 6. |

## 8. Ghi chú kỹ thuật cho người tiếp nhận

- Lịch sử trạng thái **không tự ghi** — trigger `audit_change` của DB làm việc đó. Command chỉ gắn người thao tác (`AuditActorContext.bind`) và lý do (`bindReason`); lý do có dạng `CAUSE; refund=OUTCOME; lý do người dùng`.
- Mọi thay đổi trạng thái giữ chỗ đi qua `BookingHoldLifecycle` (xác nhận, giải phóng, xử lý quá hạn). Mọi kiểm tra phòng trống đi qua `ResourceAvailabilityGuard` (khóa theo phòng, tính cả người hết hạn hợp đồng chưa trả phòng — BR-R20b).
- Thứ tự khóa: **khóa phòng trước, khóa dòng booking sau** — ai thêm luồng mới cần giữ đúng thứ tự để tránh deadlock.
- Ngày: DB lưu `end_date` dạng loại trừ (= ngày ở cuối + 1); API dùng `lastDay`. Chỉ đổi qua `LeasingDates`. "Hôm nay" theo giờ Việt Nam.
- Không có test tự động cho phần Student 3. Các test đang fail sẵn trên nhánh (không liên quan): `RentalBootstrapTests` (do `DataInit` truy vấn DB lúc khởi động), `PublicRoomSecurityTests`, `SecurityConfigCorsTests`.
