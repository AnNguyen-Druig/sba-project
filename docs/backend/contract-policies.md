# CONTRACT POLICIES — Chính sách thuê phòng & hợp đồng dữ liệu

> **SBA301 | Student 3 (Leasing) × Student 5 (Billing/Payment)**  
> **Trạng thái:** Bản thảo để họp và chốt nghiệp vụ — **chưa giao Codex triển khai toàn bộ**.  
> **Tham chiếu:** `refactor/rental-schema` · commit `70a55cd` (13/10/2026).  
> **Ký hiệu:** ✅ Đã thống nhất · 💡 Đề xuất · ❓ Cần chốt.

## 1. Chính sách đã thống nhất ✅

1. **Ngày nhận phòng linh hoạt:** Tenant được bắt đầu thuê vào **bất kỳ ngày nào trong tháng**. Khoảng thuê `[startDate, endDate)`; ngày kết thúc không được tính vào thời gian thuê.
2. **Giá được thỏa thuận:** Owner/Manager được tăng/giảm/giữ nguyên giá thuê cho **mọi thời hạn**; không bắt buộc tăng giá khi thuê ngắn hạn. `Room.referencePrice` là giá tham khảo, giá được chốt nằm ở `ContractTenant`.
3. **Thuê đúng 1 tháng:** Trước khi bắt đầu thuê, Tenant trả trước **1 tháng tiền thuê**, không thu riêng thêm tiền tháng cuối.
4. **Thuê từ 2 tháng:** Trước khi bắt đầu thuê, Tenant trả trước **tiền kỳ thuê đầu + kỳ thuê cuối**; các kỳ giữa đóng theo hạn thu. Đây là **PREPAID_RENT**, không phải tiền đặt cọc bảo đảm (`SECURITY_DEPOSIT`).
5. **Không thu trùng:** Kỳ thuê đã trả trước vẫn có Invoice; tiền đã thu được **áp dụng để giảm dư nợ**, không tạo thêm một Payment thu tiền.
6. **Công nợ riêng:** Mỗi `ContractTenant` có Invoice theo tháng và nghĩa vụ riêng, kể cả khi chung Contract chính/phòng thuê SLOT.

**Ví dụ (nhận phòng đầu tháng, chỉ xét RENT):** Thuê 1 tháng giá chốt 5 triệu → trả trước 5 triệu. Thuê 6 tháng giá chốt 3 triệu/tháng → trả trước 6 triệu (tháng đầu + cuối), 4 tháng giữa trả 3 triệu/kỳ.

## 2. Vấn đề chính: Kỳ thuê khác kỳ hóa đơn ❓

- **Kỳ thuê** có thể là `15/11 → 15/12` (đúng 1 tháng) hoặc `15/11 → 15/01` (đúng 2 tháng).
- **Invoice hiện tại** cố định theo **tháng dương lịch** (`billing_month` luôn là ngày 01). Vì vậy, một kỳ thuê có thể nằm trên **nhiều Invoice**.
- Tiền thuê trả trước của một *kỳ thuê đầu/cuối* phải được phân bổ đúng sang các tháng dương lịch tương ứng, không được thu lại.

**Ví dụ:** Thuê `15/11/2026 → 15/01/2027`, giá 4 triệu/kỳ thuê, **trả trước 8 triệu**. Đây là **lịch phân bổ minh họa cần được thỏa thuận**, không phải công thức bắt buộc:

| Invoice | Tiền RENT | Dùng khoản trả trước đầu | Dùng khoản trả trước cuối |
|---|---:|---:|---:|
| 11/2026 | 2.000.000 | 2.000.000 | 0 |
| 12/2026 | 4.000.000 | 2.000.000 | 2.000.000 |
| 01/2027 | 2.000.000 | 0 | 2.000.000 |
| **Tổng** | **8.000.000** | **4.000.000** | **4.000.000** |

> Không bao gồm phí dịch vụ/sửa chữa. Trường hợp **thuê đúng 1 tháng** nhưng vắt qua 2 tháng dương lịch cũng gặp vấn đề tương tự với bucket trả trước kỳ đầu.

### So sánh cách tổ chức hóa đơn

| Phương án | Ưu điểm | Nhược điểm | Đề xuất |
|---|---|---|---|
| **A. Giữ Invoice theo tháng dương lịch; thêm lịch phân bổ RENT theo ContractTenant** | Ít ảnh hưởng module khác, dễ thống kê theo tháng, linh hoạt ngày vào | Cần lưu lịch tiền thuê đã duyệt và sửa quy tắc áp dụng trả trước | **💡 Chọn A** |
| **B. Invoice theo chu kỳ thuê 15→15** | Dễ tính kỳ thuê đầu/cuối | Phải thay đổi `billing_month`, unique, due date, cách ghép phí dịch vụ/báo cáo | Không khuyến nghị Phase 1 |
| **C. Giữ nguyên SQL, chỉ áp dụng FIRST/LAST trong tháng dương lịch đầu/cuối** | Không migration | Có thể không bù được toàn bộ khoản đã trả trước trong tình huống ở trên | Không chọn |

## 3. Các câu hỏi cần chốt tại cuộc họp

| Mã | Vấn đề | Giải pháp & ưu/nhược điểm | 💡 Khuyến nghị |
|---|---|---|---|
| **Q1** | Có cho thuê **1 tháng 10 ngày**, hay chỉ số tháng nguyên? | **A.** Thời hạn 1, 2, 3… tháng, nhưng được bắt đầu bất kỳ ngày nào: đơn giản, ít trường hợp ngoại lệ. **B.** Mọi thời hạn: linh hoạt hơn nhưng cần cách định giá tháng lẻ. | **A trong MVP**; mở rộng B sau khi có chính sách rõ. |
| **Q2** | Chia RENT vào các tháng dương lịch theo cách nào? | **A.** Owner/Manager xác nhận trước **số tiền từng kỳ**: đúng thỏa thuận nhưng cần thao tác duyệt. **B.** Hệ thống tự chia theo số ngày: nhanh hơn nhưng phải chốt cách chia và làm tròn. | **A**; tổng RENT các tháng phải bằng tổng tiền thuê đã chốt. |
| **Q3** | Khoản trả trước FIRST/LAST được áp dụng cho tháng nào? | **A.** Cho phép áp dụng vào **mọi Invoice giao với kỳ thuê đầu/cuối**: đúng dòng tiền nhưng phải sửa integrity SQL. **B.** Chỉ tháng dương lịch đầu/cuối: giữ nguyên SQL nhưng không đáp ứng đủ ví dụ trên. | **A**; migration có version. |
| **Q4** | Tiền trả trước phải đủ **trước ngày nhận phòng**, trong khi `Invoice.dueDate` theo Branch có thể muộn hơn? | **A.** Tách thời hạn trả trước để kích hoạt thuê khỏi dueDate của Invoice: đúng nghiệp vụ, cần hai kiểm tra. **B.** Dùng duy nhất dueDate: ít logic nhưng có thể cho nợ đến sau ngày nhận phòng. | **A**. |
| **Q5** | Đổi giá sau khi ContractTenant đã `ACTIVE`? | **A.** Yêu cầu thỏa thuận/phiên bản điều chỉnh, không đổi Invoice đã phát hành: có audit. **B.** Sửa trực tiếp giá: dễ nhưng sai lịch sử. | **A**. |

## 4. Database: Giữ gì, có thể phải sửa gì?

**Đang có:**

- `contract_tenants`: `monthly_rent`, `first_month_rent`, `last_month_rent`, `prepaid_first_required`, `prepaid_last_required`, `start_date`, `end_date`.
- `invoices`: duy nhất `(contract_tenant_id, billing_month)` khi chưa `CANCELLED`; `billing_month` là ngày 01.
- `invoice_items`: `RENT`, `PREPAID_APPLICATION` cùng `source_payment_id` và `prepaid_bucket` (`FIRST_MONTH` / `LAST_MONTH`).
- CHECK: `prepaid_first_required = first_month_rent`, `prepaid_last_required = last_month_rent`. Trigger yêu cầu ContractTenant `ACTIVE` có đủ Payment `VERIFIED`.

**Vướng mắc đã xác thực:** `guard_invoice_item()` hiện chỉ cho FIRST áp dụng vào **tháng dương lịch chứa `start_date`**, LAST vào **tháng chứa `end_date - 1`**. Do đó hợp đồng nhận phòng giữa tháng có thể **không áp dụng hết tiền trả trước**, dù đã nhận đủ tiền. Cấu hình hợp đồng một tháng `first_month_rent = giá cả kỳ`, `last_month_rent = 0` **không tự giải quyết được** vướng mắc này.

**💡 Phạm vi migration đề xuất (chưa phê duyệt):**

1. Có **lịch RENT theo tháng dương lịch được chốt** gồm `contractTenantId`, `billingMonth`, khoảng sử dụng, `rentAmount`, phần tiền thuộc kỳ thuê đầu/cuối, version/phê duyệt. Chọn nơi lưu có cấu trúc và truy vết; **chưa quyết định thêm bảng hay cột**, không parse giá từ `terms` dạng text.
2. Cập nhật trigger/prepaid validation để áp dụng đúng phần RENT của kỳ thuê đầu/cuối **qua nhiều Invoice nếu cần**, vẫn giới hạn theo nguồn Payment, beneficiary, bucket và số dư.
3. Không thay đổi âm thầm Invoice `ISSUED`, không ghi thêm Payment khi áp dụng prepaid, không tắt CHECK/trigger để vượt lỗi. Mọi thay đổi DB phải có migration và test.

## 5. Hợp đồng dữ liệu bàn giao Student 3 → Student 5 💡

| Chủ sở hữu | Dữ liệu/nghiệp vụ cần bàn giao |
|---|---|
| **Student 3 — `leasing`** | `contractTenantId`, `contractId`, `branchId`, `tenantUserId`, `status`, `startDate`, `endDate`, `paymentEnvironment`; giá đã thỏa thuận, tổng tiền trả trước; **lịch RENT theo `billingMonth` + phần FIRST/LAST có thể áp dụng**; snapshot/phiên bản thỏa thuận. |
| **Student 5 — `billing`** | Đọc dữ liệu chốt từ `leasing` → tạo Invoice DRAFT → RENT theo lịch được duyệt → bổ sung phí nguồn hợp lệ → tính tổng/dueDate → ISSUED; không tự sửa ContractTenant hay tính lại giá theo `Room.referencePrice`. |
| **Student 5 — `payment` (task sau)** | Kiểm tra Payment VERIFIED, phân bổ FIRST/LAST đúng kỳ, tạo PREPAID_APPLICATION có truy vết; **không** ghi Payment nhận tiền lần 2. |

**Ranh giới:** Student 3 công khai *Service Interface + read DTO* cho Student 5. Student 5 **không gọi Repository nội bộ hoặc ghi Entity của module `leasing`**; có thể dùng mock để làm Invoice Core trước. `Invoice.dueDate` lấy `BranchPolicy` hiệu lực theo kỳ hóa đơn và lưu snapshot. Student 4 cung cấp SERVICE đã chốt; Student 6 cung cấp REPAIR đã duyệt — Student 5 **chỉ tiêu thụ nguồn**, không triển khai thay.

## 6. Biên bản chốt sau cuộc họp

| Quyết định | Kết quả thống nhất |
|---|---|
| Q1 — Quy tắc thời hạn thuê | **OPEN** — A / B / phương án khác: … |
| Q2 — Cách chia RENT theo tháng | **OPEN** — A / B / phương án khác: … |
| Q3 — FIRST/LAST qua nhiều Invoice và migration | **OPEN** — A / B / phương án khác: … |
| Q4 — Hạn trả trước vs Invoice dueDate | **OPEN** — A / B / phương án khác: … |
| Q5 — Điều chỉnh giá sau ACTIVE | **OPEN** — A / B / phương án khác: … |
| DTO/interface, người làm migration, người review | **OPEN** — … |

**Sau khi chốt:** Sửa đặc tả → duyệt migration (nếu cần) → test hợp đồng 1 tháng qua 2 tháng dương lịch, 2 tháng qua 3 tháng dương lịch, thuê SLOT, trả trước không trùng → triển khai BE-03 và BE-05.1 song song.

---

**Tài liệu gốc:** [BE tasks](https://github.com/AnNguyen-Druig/sba-project/blob/refactor/rental-schema/docs/be_tasks.md) · [SQL tables](https://github.com/AnNguyen-Druig/sba-project/blob/refactor/rental-schema/docs/database/rental/01_phase1.sql) · [SQL integrity/triggers](https://github.com/AnNguyen-Druig/sba-project/blob/refactor/rental-schema/docs/database/rental/02_phase1_integrity.sql) · [Refactor report](https://github.com/AnNguyen-Druig/sba-project/blob/refactor/rental-schema/docs/database/rental/REFACTOR_REPORT.md) · [Coding rules](https://github.com/AnNguyen-Druig/sba-project/blob/refactor/rental-schema/docs/coding_rules.md).
