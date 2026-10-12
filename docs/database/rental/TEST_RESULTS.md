# Kiểm chứng rental

Ngày thực hiện: 2026-10-12. Môi trường: PostgreSQL 18.6 trong PGlite 0.5.9, Node 24.13.0; database thử nghiệm trong bộ nhớ. Không truy cập Supabase. Docker daemon không khả dụng, chưa có PostgreSQL server nhiều session cho concurrency test.

## Đã chạy

```powershell
npm.cmd install --prefix be-sba-project/target/rental-ddl-check --no-audit --no-fund @electric-sql/pglite@0.5.9
node docs/database/rental/test-database.mjs --write-catalog
node docs/database/rental/test-database.mjs --phase=1
node docs/database/rental/test-database.mjs --phase=2
node docs/database/rental/generate-docs.mjs
```

Dependency thử nghiệm nằm trong `target`, không sửa `pom.xml` hay dependency backend. Runner mở database mới cho mỗi lần chạy; thiếu file yêu cầu sẽ báo lỗi.

| Kiểm tra | Kết quả |
|---|---|
| Thực thi 01, 02, 03, 04 | PASS — 32/33/34 bảng; thứ tự FK hợp lệ; không bảng ngoài danh sách chốt |
| Role catalog + toàn bộ demo | PASS |
| Quyền schema/default privileges trong PostgreSQL thử nghiệm | PASS về cú pháp; role Supabase được kiểm tra có tồn tại trước REVOKE |
| Metadata, unique index, FK, không DELETE CASCADE | PASS |
| Ngày thu vượt cuối tháng, tháng 2 và năm nhuận | PASS |
| Helper Owner/Manager/Tenant đúng và sai phạm vi | PASS — chỉ helper DB, không phải HTTP authorization |
| Điện 30/10 ngày, tổng 300.000đ, tiền trả trước, trả hộ, nợ, tiền dư, audit | PASS |
| Phân biệt tiền Sandbox với tiền thật | PASS |
| Negative SQL theo Phase, ép deferred checks trước kết luận | PASS — P1: 45, P2: 48, đầy đủ: 50; transaction cuối ROLLBACK |
| Dữ liệu gộp | PASS — INVITED thiếu contact/password hợp lệ nhưng không có quyền đọc Invoice; cùng User thuê hai Owner; snapshot member bất biến |
| Bảo mật identity gộp | PASS — ADMIN duyệt Owner có audit whitelist; hash không vào audit; định dạng plaintext thông thường bị từ chối; đổi purpose/khôi phục challenge đã consumed bị chặn |
| Chặn bảo trì | PASS — Block không được chồng lên lease; hold không được chồng lên BLOCKED; không cần Booking/Contract cho block |
| Bảng/FK/routine/view | PASS — không có 16 bảng bị bỏ hoặc tham chiếu relation tương ứng; không còn cột tenant_id cũ |
| Đối chiếu tài liệu với Git HEAD | PASS — 211 feature ID và điểm độ khó giữ nguyên ở cả hai tài liệu |
| Mapping quyết định và link tài liệu | PASS — đủ D01–D40, không trùng ID, link nội bộ tồn tại |
| XML draw.io | PASS — 15 trang, 34 entity trong Logical hiện hành, không entity bị bỏ trong trang hiện hành; ba trang Legacy/Deprecated giữ nguyên nội dung |

Danh sách và SQL thực tế: [08_negative_tests.sql](08_negative_tests.sql). Mỗi ca chỉ nhận SQLSTATE thuộc nhóm integrity violation; lỗi cú pháp hoặc lỗi code trigger không bị coi nhầm là PASS. Các test này chứng minh một thao tác bị chặn; khi nhiều ràng buộc cùng áp dụng, chúng không chứng minh mọi ràng buộc đã được kiểm tra độc lập.

## NOT EXECUTED — phải chạy khi tích hợp backend

| Test | Cách chạy / kết quả yêu cầu |
|---|---|
| SQL trên project Supabase thật | Theo README; đối chiếu version, 32/33/34 tables theo Phase; chưa kết nối |
| Hai kết nối cùng giữ một Slot | Hai request khác transaction vào cùng room/slot và khoảng ngày; đúng một thành công, request còn lại conflict/retry; rollback/cleanup ở DB test |
| Whole-room hold đối đầu slot hold | Cùng room, hai giao dịch đang chạy; không được commit cả hai |
| Hai giao dịch dùng cùng tiền trả trước | Hai idempotency key khác nhau tranh số dư hữu hạn; tổng áp dụng không vượt số dư |
| Isolation READ COMMITTED / REPEATABLE READ | Lặp các race trên; serialization/deadlock được rollback và retry có giới hạn |
| HTTP 401/403/IDOR | JWT nhiều role, manager khác owner, tenant khác invoice, revoked/locked user; API phải từ chối |
| Chữ ký VNPay/MoMo | Sandbox được chọn, chữ ký đúng/sai, replay, out-of-order, timeout; database không tự xác minh chữ ký bằng cryptography |
| Xác nhận QR thủ công | Owner/Manager được phân công mới xác minh; ghi evidence và audit, người trả không tự xác minh |
| Refund idempotency và side effect provider | Provider refund request idempotent, chỉ ghi kết quả xác nhận một lần; retry không hoàn hai lần |
| Đổi đại diện / rời phòng / ký OTP / bàn giao | Service state machine, DTO scope, quyền tài liệu/đa signer; cột gộp không tự triển khai workflow |
| Refresh/OTP đồng thời và account claiming | Một token/challenge mỗi User: refresh dùng CAS hash cũ + expiry và auth_version; chứng minh quyền sở hữu trước activate/link; chưa chạy integration backend |
| Load/performance | EXPLAIN ANALYZE trên volume thực; deferred trigger hiện kiểm tra phạm vi rộng, chưa được benchmark |
| Hiển thị draw.io | Đã kiểm tra XML/cấu trúc và metadata, chưa kiểm tra render bằng ứng dụng draw.io |

## Giao ước transaction khi viết backend

1. Một use case liên module chạy trong một transaction của Modular Monolith. Khóa Branch trước, rồi Room theo thứ tự UUID nhất quán; multi-branch cũng theo UUID. Dùng version optimistic locking cho cập nhật từ UI.
2. Trigger ghi tăng `version` vào parent để serialize các thao tác liên quan; không coi `SELECT FOR UPDATE` đơn lẻ là đủ dưới mọi isolation level. Xử lý SQLSTATE `40001`/`40P01` bằng retry có giới hạn và giữ request idempotency.
3. Actor đặt từ principal đã xác thực: `SELECT set_config('rental.actor_id', :userId, true)` trong transaction. Thêm `rental.reason` và `rental.request_key` cho thao tác nhạy cảm; không copy từ trường actor do client gửi. Audit request unique trên mỗi Payment UPDATE; gộp thay đổi một payment vào một UPDATE/use case.
4. Booking conversion: tạo/tham gia Contract, tạo ContractTenant, chuyển Payment giữ chỗ sang trả trước, RELEASE allocation cũ, thêm LEASED allocation mới, cập nhật booking CONVERTED, ghi occupancy, notification; cùng COMMIT. Contract ACTIVE cần đại diện, member ACTIVE cần đủ trả trước tháng đầu/cuối; constraint deferred cho phép chuẩn bị các dòng trong cùng transaction.
5. Booking hết hạn: scheduler cập nhật status và RELEASE allocation trong một transaction. Cột timestamp không tự thực thi tác vụ. Allocation quá giờ nhưng chưa được release vẫn chặn đặt mới để tránh cấp trùng.
6. Invoice: tạo DRAFT + dòng phí/snapshot; validate tổng và quyền, phát hành. Sau phát hành không sửa dòng phí. Muốn thay thế: Owner duyệt, đảo tiền đã áp dụng/đưa khoản direct về chưa phân bổ, CANCELLED bản cũ, tạo bản thay thế cùng member/tháng có `replaces_invoice_id`, áp dụng tiền lại có truy vết.
7. Dòng PREPAID_APPLICATION không làm tăng subtotal, không được coi là doanh thu âm, không tạo receipt. Không dùng `SUM(invoice_items.amount)` vô điều kiện.
8. Optional service/rate precedence, thời điểm có hiệu lực, state machine, rule quyền và tính hợp lệ Cloudinary do service kiểm tra. FK/trigger bảo vệ dữ liệu không thay thế các bước này.

## Giới hạn cần giữ rõ

DDL của Phase 2/3 là cấu trúc chuẩn bị. Không tuyên bố ký OTP, nhà cung cấp thanh toán, gửi email/SMS, eKYC hoặc API đã được triển khai. `invoiced_by_period` thể hiện nghĩa vụ theo kỳ và từng loại khoản thu; `branch_funds` thể hiện nguồn tiền. Backend báo cáo không được gọi toàn bộ số thu trước là doanh thu tháng nhận tiền, hoặc dùng báo cáo này thay cho một hệ thống kế toán đã được chuẩn hóa.

Không có phiên đa thiết bị độc lập, lịch sử login chi tiết, hồ sơ Tenant riêng từng Owner hoặc bảng nhiều lần nộp Owner Verification. Move-out/handover chỉ giữ hiện hành, lịch sử trạng thái qua audit. PDF chính không được ghi đè sau phát hành; thay đổi qua amendment mới. Signer JSONB do backend kiểm tra ID/quyền/toàn bộ chữ ký; CHECK hình dạng không chứng minh OTP đúng. Worker gửi notification có thể retry trùng nếu provider không có idempotency; không hứa exactly-once. Comment/Suggestion/report moderation hoãn hoàn toàn, không được nhét vào JSONB.
