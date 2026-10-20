# Tạo schema rental trên Supabase

Bộ này tạo mới trong **schema `rental` rỗng** theo D01–D40. Không chuyển dữ liệu từ `public`, không DROP, không đổi cấu hình backend. Link `https://fmzupdyoxtlkxrfdtuly.supabase.co` là API URL; chạy SQL trong **Dashboard của đúng project → SQL Editor**.

**Bản điều chỉnh chính thức: 34 bảng (32 + 1 + 1).** Nếu bạn đã chạy bộ 50 bảng trước đó, **không chạy bộ này đè lên và không DROP để giảm số bảng**. Khi đó cần kế hoạch migration dữ liệu riêng và xác nhận trước khi thực hiện. Các bước dưới đây chỉ dành cho `rental` rỗng.

Đã kiểm thử cục bộ bằng PostgreSQL 18.6 chạy trong PGlite 0.5.9. **Chưa thực thi trên Supabase của bạn.** Các kiểm tra API/JWT, chữ ký nhà cung cấp và đồng thời nhiều kết nối chưa chạy; xem [TEST_RESULTS.md](TEST_RESULTS.md).

## 1. Kiểm tra trước khi chạy

Mở SQL Editor, tạo query mới, dán **toàn bộ nội dung** [00_preflight.sql](00_preflight.sql), chạy bằng role sở hữu schema (ảnh bạn gửi là `postgres`). Kết quả cần có schema `rental`, danh sách bảng trong `rental` rỗng, `has_uuid_generator = true`. Việc `public` còn dữ liệu cũ là bình thường.

Nếu `rental` đã có bảng, dừng tại đây để đối chiếu trạng thái; không chạy DROP để “sửa” lỗi. `01_phase1.sql` cũng chủ động từ chối schema không rỗng. Không cần extension bổ sung cho bộ script này.

## 2. Tạo bảng, constraint, trigger và index

Mỗi hàng dưới đây: tạo query riêng → dán toàn bộ file → **Run** → chỉ sang bước kế khi file trước thành công. Không chọn một đoạn SQL rồi bấm Run. Mỗi file có transaction riêng; không bọc thêm một transaction ở ngoài.

| Thứ tự chạy | File | Kết quả |
|---|---|---|
| 1 | [01_phase1.sql](01_phase1.sql) | 32 bảng Phase 1; PK/FK/CHECK/UNIQUE và index |
| 2 | [02_phase1_integrity.sql](02_phase1_integrity.sql) | Bắt buộc ngay sau 01: ràng buộc liên bảng, bảo vệ lịch sử, audit, 5 view |
| 3 | [03_phase2.sql](03_phase2.sql) | Thêm `contract_amendments`, ALTER các cột gộp; tổng 33 |
| 4 | [04_phase3.sql](04_phase3.sql) | Chỉ thêm `threads`, ALTER tài liệu/thông báo; tổng 34 |
| 5 | [09_access.sql](09_access.sql) | Thu hồi quyền Data API trên tất cả đối tượng vừa tạo và default privileges của role thực thi |
| 6 | [05_seed_roles.sql](05_seed_roles.sql) | 5 application role; có thể chạy lại an toàn |
| 7 | [06_seed_demo.sql](06_seed_demo.sql) | Dữ liệu demo hợp lệ; chỉ chạy một lần trên môi trường thử nghiệm |
| 8 | [07_verify.sql](07_verify.sql) | Kiểm tra cấu trúc và số liệu demo |
| 9 | [08_negative_tests.sql](08_negative_tests.sql) | 50 ca kiểm tra ở đủ Phase; toàn bộ ca thử được ROLLBACK |

Tạo sẵn bảng Phase 2/3 không có nghĩa phải triển khai tính năng đó trong MVP. Nếu chỉ dùng Phase 1, bỏ 03/04; nếu đến Phase 2, bỏ 04. **07/08 tự nhận diện Phase đã cài**: kiểm tra 32/33/34 bảng và chạy lần lượt 45/48/50 negative cases. Mặc định hướng dẫn trên tạo đủ ba Phase.

Nếu một file lỗi, đọc lỗi và chạy `ROLLBACK;` nếu session còn ở trạng thái lỗi. Các file đã COMMIT trước đó vẫn tồn tại. Không chạy lại 01, không DROP toàn bộ schema. Các file DDL là bản cài mới, không phải migration lặp; chỉ tiếp tục đúng bước đã thất bại sau khi xác định nguyên nhân.

## 3. Kết quả cần nhìn thấy

```sql
SELECT count(*) AS tables
FROM pg_tables WHERE schemaname='rental'; -- 34 (hoặc 32/33 theo Phase)

SELECT code FROM rental.roles ORDER BY code;
-- ADMIN, MANAGER, OWNER, TENANT, USER

SELECT invoice_id,billing_month,total_amount,settled_amount,outstanding_amount,payment_status
FROM rental.invoice_balances ORDER BY billing_month,invoice_id;

SELECT * FROM rental.branch_funds;
SELECT * FROM rental.real_cash_by_branch; -- Không có dòng tiền thật từ seed
```

Sau file 07, statement kiểm tra trả `PASS: metadata and applicable demo assertions`. File 08 trả bảng `negative_results` có **50 dòng PASS** ở đầy đủ Phase (45 ở P1, 48 ở P2) nếu xem SELECT trước ROLLBACK; SQL Editor có thể chỉ hiển thị statement cuối. Script tự phát lỗi nếu một ca sai lại được chấp nhận. Không có bản ghi thử sai tồn tại sau file 08.

Demo có 10 users gồm một hồ sơ INVITED chưa có liên hệ/mật khẩu; 3 branches của 2 owners; một manager quản lý 2 branches; Room và Slot; 2 bookings vào cùng Contract chính; 4 ContractTenants (một bản DRAFT cho cùng User thuê ở Owner khác), 4 invoices, 8 payments. Có allocation BLOCKED bảo trì và Owner Verification do ADMIN duyệt. Tất cả payment là SANDBOX, tổng khoản nhận mô phỏng **30.200.000đ**. Đây là tập tình huống qua nhiều kỳ, không mô phỏng trạng thái “hôm nay”.

| Tình huống demo | Kết quả đúng |
|---|---:|
| Điện tháng 11/2026, 300.000đ, A ở 30 ngày, B ở 10 ngày | A: 225.000đ; B: 75.000đ |
| Invoice A: rent 5.000.000 + điện 225.000 + sửa chữa 60.000 | Tổng 5.285.000đ |
| A áp dụng trả trước 5.000.000, B trả hộ 100.000 | A còn nợ 185.000đ |
| B: tiền thuê tháng đầu thỏa thuận 2.000.000 + điện 75.000 | Tổng 2.075.000đ, đã thanh toán đủ |
| B nộp 100.000 để trả 75.000 còn thiếu | Dư 25.000đ tại Payment |
| Invoice tháng cuối của A | Vẫn có tiền thuê 5.000.000đ, được bù hết bằng trả trước |
| Tiền trả trước tháng cuối chưa dùng của B / C | 3.000.000đ / 7.000.000đ |
| Tiền 1.000.000đ đến sau hạn booking | Để chưa phân bổ; booking vẫn EXPIRED |

Mọi khoản tiền/thời hạn trong seed là **fixture**, không phải chính sách mặc định được nhóm chốt. Email dùng `example.invalid`, password là chuỗi vô hiệu `!DEMO_LOGIN_DISABLED`; chưa có tài khoản đăng nhập sử dụng được. Callback mang nhãn fixture, không phải bằng chứng provider thật. Không dùng các giá trị này làm cấu hình vận hành.

## 4. Cách đọc mô hình tiền trả trước

`Payments` chỉ ghi một lần thu tiền. Ví dụ A giữ chỗ 1 triệu: chuyển `allocated_amount` từ 1 triệu về 0, tăng `prepaid_first_amount` lên 1 triệu và gắn `prepaid_contract_tenant_id`. Audit giữ trước/sau. Không INSERT một receipt mới để ghi lại số tiền đã thu.

Khi thanh toán Invoice, thêm `InvoiceItems.item_type = PREPAID_APPLICATION` trỏ `source_payment_id`, bucket `FIRST_MONTH` hoặc `LAST_MONTH`. Đây là **dòng áp dụng tiền**, không phải dòng phí, không phải giảm giá. Tổng phí Invoice phải loại loại dòng này; view `invoice_balances` tính nó vào số đã thanh toán. Dòng phí sau phát hành bị khóa; dòng áp dụng tiền được thêm sau phát hành, chỉ được đảo có Owner duyệt.

Một Payment vẫn có tối đa một đích trực tiếp là Invoice/Booking. Việc sử dụng quỹ trả trước của cùng ContractTenant vào Invoice tháng đầu/cuối được truy vết qua các dòng nội bộ nêu trên, đúng phạm vi D30; không có `payment_allocations` hay ví Tenant.

Môi trường `REAL`/`SANDBOX` phải thống nhất xuyên suốt Contract, Booking nguồn, Invoice và Payment. Không đổi nhãn một Contract sandbox thành nghĩa vụ tiền thật khi đã có dữ liệu liên quan; tạo dữ liệu vận hành riêng sau khi kết thúc kiểm thử.

Hoàn tiền cập nhật số lũy kế đã hoàn tại Payment, có Owner actor, lý do và `request_key` chống lặp, audit trước/sau. Backend chỉ tăng bộ đếm sau khi việc hoàn đã được xác nhận; schema không tự thực hiện chuyển tiền. Tiền đã áp dụng phải đảo trước khi hoàn. Quyết định rời sớm, disposition và bàn giao nằm trên `contract_tenants`, không tự hoàn/tịch thu.

USERS là hồ sơ cá nhân chung. Owner/Manager chỉ được đọc dữ liệu qua quan hệ thuê thuộc phạm vi mình; không được tự sửa profile toàn cục, thay password hay xác nhận danh tính hộ người được mời. Snapshot lúc ký ở `contract_tenants.tenant_snapshot` không thay khi User sửa profile. Mỗi User có một challenge và một refresh token hiệu lực; backend phải hash, kiểm tra expiry/attempts và rotate bằng transaction/CAS. CHECK định dạng hash không tự xác minh OTP hoặc token.

Phase 3 chỉ có Thread cơ bản. Comment/Suggestion/báo cáo vi phạm hoãn; template thông báo ở source/config, gửi/retry ngay trên notifications theo channel. PDF/bằng chứng ký tối giản ở Contract/Amendment; workflow OTP nhiều signer chưa được triển khai. Chi tiết giới hạn xem [REFACTOR_REPORT.md](REFACTOR_REPORT.md).

## 5. Quyền truy cập và bước refactor sau đó

Thiết kế dùng **Spring Security/JWT + kết nối PostgreSQL từ backend**, không dùng Supabase Auth. Giữ `rental` ngoài danh sách **Exposed schemas** của Data API. `09_access.sql` không cấp quyền cho `anon`, `authenticated`, `service_role`; không tạo policy dựa trên `auth.uid()`.

Đây là schema backend-only: các helper `can_manage_branch` / `can_read_invoice` hỗ trợ kiểm tra nhưng **không phải RLS và không tự bảo vệ mọi SELECT**. Backend phải kiểm tra principal và scope trước truy vấn, không nhận actor/role từ client. Khi tạo branch refactor sẽ cấp quyền cho DB login riêng của backend và kiểm thử JDBC. Không đưa password DB vào frontend hoặc git.

Sau khi xác nhận SQL trên Supabase chạy thành công:

1. Tạo branch refactor theo `docs/coding_rules.md`.
2. Map entity mới sang `rental`, UUID và `OffsetDateTime`/`Instant`; không kế thừa soft-delete cho mọi bảng.
3. Chuyển Invoice từ Contract/kỳ sang ContractTenant/tháng; tách trạng thái chứng từ và trả tiền.
4. Thực thi luồng thuê/thu tiền bằng transaction; SQL này **không tự cung cấp API, scheduler hết hạn, chữ ký callback hoặc xác thực đăng nhập**.
5. Dùng Hibernate `ddl-auto=validate`; lưu các SQL này làm baseline của schema mới. Các thay đổi tiếp theo cần script có version, không DROP/tạo lại mỗi lần đổi code.
6. Chỉ sau khi kiểm thử backend với `rental` mới chuyển môi trường dùng chung; việc xóa các đối tượng ứng dụng cũ trong `public` là bước riêng sau này.

Nguồn chính thức: [Supabase — tạo bảng bằng SQL Editor](https://supabase.com/docs/guides/database/tables), [Supabase — custom schema và Data API](https://supabase.com/docs/guides/api/using-custom-schemas), [PostgreSQL — constraints](https://www.postgresql.org/docs/current/ddl-constraints.html). CHECK không thay thế ràng buộc liên bảng; do đó có deferred constraint trigger cho các tổng tiền và quan hệ liên bảng.
