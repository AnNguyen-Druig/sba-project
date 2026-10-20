# BE TASKS — WEBSITE QUẢN LÝ PHÒNG TRỌ (SBA301)

Điều chỉnh chính thức: 34 bảng (32 + 1 + 1), thay thế phần lưu trữ D04/D18/D33/D39 trước đây. USERS là hồ sơ duy nhất; CONTRACT_TENANTS tham chiếu tenant_user_id. Các task DEFERRED giữ ID và điểm ước lượng gốc để truy vết; tổng 211 mã/709 điểm không phải cam kết triển khai toàn bộ phạm vi hiện tại. Xem database/rental/REFACTOR_REPORT.md.


Cập nhật thiết kế D01–D40: 2026-10-12 — xem database/rental/REFACTOR_REPORT.md. Giữ nguyên 211 feature ID; DDL chưa đồng nghĩa task backend đã hoàn thành. “Cọc giữ chỗ” là Booking Hold; hợp đồng dùng PREPAID_RENT, không SECURITY_DEPOSIT. Quyết định mới thay thế các mô tả cũ mâu thuẫn.


> **File:** `be_tasks.md`  
> **Nguồn chính:** `all-feature.txt` (211 dòng tính năng có mã ID; Phase 1 = 138, Phase 2 = 39, Phase 3 = 34).  
> **Tham chiếu nghiệp vụ:** `features.txt`, các xác nhận mới nhất của nhóm và ERD hiện tại trong `All_Diagrams.drawio`.  
> **Nhân sự:** 6 Student. **Chỉ giao Backend**; Frontend, thiết kế UI, thay đổi ERD và các phần khác không nằm trong kế hoạch này.  
> **Công nghệ tham chiếu:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA/Hibernate, PostgreSQL.  
> **Trạng thái:** Đề xuất phân công, nhóm cần duyệt trước khi bắt đầu phát triển.

## 0. Cách đọc và nguyên tắc phân công

- **Phần 1 → Phần 6:** tương ứng **Student 1 → Student 6**; mỗi Student nhận việc ở cả ba Phase.
- **Phase 1** phải hoàn thành trước khi coi Backend MVP sẵn sàng demo; **Phase 2** hoàn thiện nghiệp vụ sau Phase 1; **Phase 3** chỉ làm nếu còn thời gian.
- Mã `[P1-xx]`, `[P2-xx]`, `[P3-xx]` trùng **chính xác** `all-feature.txt`. Mỗi mã chỉ giao **một Student owner**; các Student liên quan phối hợp qua interface chứ không code trùng nhiệm vụ.
- Một **BE-XX.Y** là **task lớn (work package/epic)** có thể giao và kiểm thử; các checkbox phía dưới là feature/backend acceptance items nằm trong epic. Không nên chỉ đếm số checkbox để đánh giá công bằng.
- **Độ khó ước lượng:** `0` = chỉ là ghi chú, không phải task code; `1` = nhỏ; `2` = thao tác đơn giản; `3` = nghiệp vụ bình thường; `4` = nghiệp vụ khó/đa quy tắc; `5` = tích hợp ngoài/cạnh tranh dữ liệu/tiền. Đây là điểm tương đối để chia việc, **không phải ngày công**.
- Student 1 thống nhất security/authorization; Student 4 thống nhất notification event; Student 5 thống nhất trạng thái thanh toán; Student 6 hỗ trợ conventions/CI và integration test. **Mọi người vẫn phải viết validation và test cho API mình làm.**
- **Đã chốt D01–D40:** N–N roles, Contract chính theo Room, nghĩa vụ/Invoice theo ContractTenant, chia phí theo ngày cư trú, PREPAID_RENT tháng đầu/cuối và QR thủ công hoặc Sandbox. Các mức tiền/thời hạn và xử lý rời sớm còn mở.
- **Ghi chú chỉ có trong nguồn:** `[P1-67]` (Phase 1 không yêu cầu ký OTP/PDF hợp đồng), `[P3-23]` (ký OTP mô phỏng không mặc định có giá trị pháp lý tương đương ký số). Hai mã này vẫn được giữ để đối chiếu, nhưng điểm phát triển = 0.

## 0.1. Bảng cân bằng công việc

| Sinh viên | Mảng chính | Phase 1: số mã / điểm | Phase 2: số mã / điểm | Phase 3: số mã / điểm | Tổng số mã / điểm | Task lớn |
|---|---|---:|---:|---:|---:|---:|
| Student 1 | Authentication / Security / Owner – Manager – Branch / nhận tiền của Owner | 24 / 68 | 7 / 22 | 5 / 23 | **36 / 113** | **9** |
| Student 2 | Room – Slot / tìm kiếm công khai / Appointment / API đọc & dashboard | 29 / 66 | 7 / 27 | 7 / 30 | **43 / 123** | **9** |
| Student 3 | Booking – cọc giữ chỗ / Contract / liên kết Zalo cho Tenant | 22 / 72 | 5 / 18 | 7 / 28 | **34 / 118** | **9** |
| Student 4 | Services – điện nước – phân bổ chi phí / Notification | 20 / 72 | 5 / 17 | 6 / 24 | **31 / 113** | **8** |
| Student 5 | Invoice – công nợ – Payment – QR thủ công/Sandbox | 17 / 73 | 7 / 28 | 4 / 20 | **28 / 121** | **8** |
| Student 6 | Tenant – CSVC – sửa chữa / kiểm thử tích hợp – deploy / Thread nền tảng | 26 / 71 | 8 / 33 | 5 / 17 | **39 / 121** | **9** |
| **Tổng** | **211 mã từ nguồn** | **138 / 422** | **39 / 145** | **34 / 142** | **211 / 709** | **52** |

> **Ý nghĩa công bằng:** Phần quan trọng nhất là Phase 1 được giữ gần nhau về **điểm độ khó**; một số Student có nhiều mã tra cứu/CRUD nhỏ, trong khi Student 5 có ít mã hơn nhưng phụ trách xác minh thanh toán, công nợ và tính nhất quán tài chính. Điểm là ước lượng chủ quan, cần điều chỉnh nếu dự án thực tế chênh lệch.

## 0.2. Definition of Done chung cho mỗi work package

- [ ] Backend API/service nghiệp vụ chính chạy được với request/response DTO phù hợp; chức năng đọc có tìm kiếm/lọc/phân trang khi nguồn yêu cầu.
- [ ] Validate dữ liệu, kiểm tra state transition và quyền truy cập đúng **Owner/Manager/Branch/Tenant/khách đã đăng nhập**; không tin role/ID từ client.
- [ ] Có unit test/service test và integration/API test cần thiết; test cả trường hợp lỗi/không có quyền. Các thao tác tiền, giữ chỗ hoặc trạng thái phải kiểm tra cạnh tranh và chống lặp.
- [ ] Tích hợp với module phụ thuộc qua hợp đồng API/sự kiện đã thống nhất, không thay đổi âm thầm nghĩa vụ/tiền đã chốt.
- [ ] Tài liệu API, biến cấu hình môi trường và ví dụ test phù hợp để Student khác có thể tích hợp.

---

# Phần 1: Student 1

**Phạm vi BE:** Authentication / Security / Owner – Manager – Branch / nhận tiền của Owner.

## PHASE 1 — BACKEND BẮT BUỘC (P0) — 24 mã / 68 điểm

### BE-01.1 — Đăng ký, đăng nhập và vòng đời tài khoản

**Quy tắc/điều kiện tích hợp:** Đăng ký Owner/User; bảo mật phiên; mã xác minh; quên/đổi mật khẩu; chặn thông tin liên hệ trùng.

- [ ] **[P1-01]** Đăng ký tài khoản Owner bằng email hoặc số điện thoại; kiểm tra trùng và tính hợp lệ.  _(độ khó 4/5)_
- [ ] **[P1-02]** Đăng ký tài khoản người dùng tìm phòng (chưa cần là Tenant).  _(độ khó 1/5)_
- [ ] **[P1-03]** Đăng nhập bằng email + mật khẩu HOẶC số điện thoại + mật khẩu.  _(độ khó 4/5)_
- [ ] **[P1-04]** Logout/refresh tối giản trên USERS: một refresh_token_hash và hạn dùng mỗi User; rotate bằng transaction/CAS, logout xóa token và tăng auth_version khi cần. Không đa phiên độc lập.  _(độ khó 2/5)_
- [ ] **[P1-05]** Một verification challenge hiện hành trên USERS: purpose, hash, expiry, số lần thử, consumed_at; backend xác minh và vô hiệu mã cũ, không audit secret/hash.  _(độ khó 4/5)_
- [ ] **[P1-06]** Quên/đặt lại mật khẩu dùng challenge hiện hành trên USERS; thay mục đích phải tạo hash mới, không tạo bảng token riêng.  _(độ khó 2/5)_
- [ ] **[P1-07]** Xem/cập nhật hồ sơ cá nhân; quản lý trạng thái hoạt động/khóa tài khoản.  _(độ khó 2/5)_

**Ước lượng task lớn:** 7 mã nguồn, 19 điểm.

### BE-01.2 — Quyền, onboarding Manager/Tenant và bảo vệ đăng nhập

**Quy tắc/điều kiện tích hợp:** Quyền phải kiểm tra trên server và trong phạm vi Branch/hợp đồng, kể cả khi người dùng có nhiều ngữ cảnh truy cập.

- [ ] **[P1-09]** Tạo/mời USERS trạng thái INVITED, có thể chưa email/phone/password; không được đăng nhập hay nhận quyền từ lời mời. Xác nhận danh tính trước liên kết, tránh tạo trùng người.  _(độ khó 4/5)_
- [ ] **[P1-10]** Phân quyền USERS N–N ROLES qua USER_ROLES: USER, OWNER, MANAGER, TENANT, ADMIN; GUEST là ngữ cảnh chưa đăng nhập. Kiểm tra phạm vi Owner/Branch/nghĩa vụ thuê.  _(độ khó 5/5)_
- [ ] **[P1-11]** Kiểm soát truy cập người tìm phòng đã đăng nhập đối với đặt lịch, giữ phòng; cho phép GUEST chỉ đọc dữ liệu công khai.  _(độ khó 2/5)_
- [ ] **[P1-12]** Băm mật khẩu/token; failed_login_count/last_failed_login_at/locked_until trên USERS và rate limiting backend/cache, không lưu lịch sử từng lần login.  _(độ khó 2/5)_

**Ước lượng task lớn:** 4 mã nguồn, 13 điểm.

### BE-01.3 — Quản trị Owner – Manager – Branch

**Quy tắc/điều kiện tích hợp:** Không có Brand; mỗi Branch đang hoạt động có một Manager chính; một Manager có thể phụ trách nhiều Branch; chuyển phân công phải đổi quyền ngay.

- [ ] **[P1-13]** Owner tạo, xem, cập nhật và ngừng hoạt động Branch; quản lý nhiều Branch trong cùng tài khoản Owner.  _(độ khó 2/5)_
- [ ] **[P1-14]** Quản lý thông tin Branch: mã, tên, địa chỉ, mô tả, trạng thái, thông tin liên hệ và nội quy cơ bản.  _(độ khó 1/5)_
- [ ] **[P1-15]** Owner tạo/mời, xem, cập nhật thông tin, khóa/mở khóa Manager thuộc phạm vi mình.  _(độ khó 2/5)_
- [ ] **[P1-16]** Gán Manager cho Branch; mỗi Branch đang vận hành có đúng 01 Manager phụ trách tại một thời điểm (khi mới khởi tạo có thể chờ phân công).  _(độ khó 4/5)_
- [ ] **[P1-17]** Cho phép 01 Manager phụ trách NHIỀU Branch; chuyển Manager giữa các Branch theo phân công của Owner.  _(độ khó 4/5)_
- [ ] **[P1-18]** Hủy/chuyển phân công phải cập nhật ngay quyền thao tác của Manager ở Branch cũ/mới.  _(độ khó 4/5)_
- [ ] **[P1-19]** Owner được vận hành trực tiếp các nghiệp vụ của Manager trên mọi Branch thuộc mình.  _(độ khó 1/5)_
- [ ] **[P1-20]** Xem danh sách Branch/Manager phụ trách và trạng thái vận hành theo phạm vi được phép.  _(độ khó 1/5)_

**Ước lượng task lớn:** 8 mã nguồn, 19 điểm.

### BE-01.4 — QR theo Branch và tham chiếu thanh toán

**Quy tắc/điều kiện tích hợp:** Tài khoản nhận tiền thuộc Owner; mã tham chiếu riêng cho hóa đơn/booking; Student 5 chịu trách nhiệm xác thực giao dịch thực nhận.

- [ ] **[P1-96]** Owner cấu hình ảnh QR thủ công trên Branch bằng Cloudinary; không tạo owner_bank_accounts hoặc bắt buộc kết nối ngân hàng thật.  _(độ khó 4/5)_
- [ ] **[P1-97]** Hiển thị QR đã tải lên, số tiền và tham chiếu; hoặc gọi VNPay/MoMo Sandbox theo Payment của nhà cung cấp được chọn; không tạo payment_requests.  _(độ khó 4/5)_
- [ ] **[P1-98]** QR thủ công chuyển tiền trực tiếp đến Owner; Sandbox có môi trường riêng và không được tính vào tiền thật.  _(độ khó 1/5)_

**Ước lượng task lớn:** 3 mã nguồn, 9 điểm.

### BE-01.5 — Security & audit cross-module

**Quy tắc/điều kiện tích hợp:** Thống nhất cách bắt buộc kiểm tra quyền và chuẩn ghi dấu vết nghiệp vụ nhạy cảm cho các module; mỗi Student vẫn phải áp dụng vào API của mình.

- [ ] **[P1-134]** Phân quyền ở tầng backend trên mọi nghiệp vụ, tránh truy cập chéo Owner/Branch/Tenant.  _(độ khó 4/5)_
- [ ] **[P1-136]** Ghi log nghiệp vụ quan trọng/khả năng tra soát, đặc biệt thay đổi quyền, tiền cọc và giao dịch thanh toán.  _(độ khó 4/5)_

**Ước lượng task lớn:** 2 mã nguồn, 8 điểm.

## PHASE 2 — HOÀN THIỆN BACKEND (P1) — 7 mã / 22 điểm

### BE-01.6 — Phiên nâng cao và lịch sử thay đổi quyền

**Quy tắc/điều kiện tích hợp:** Thu hồi phiên khi khóa tài khoản hoặc đổi mật khẩu; lịch sử thao tác/phân công có phạm vi truy cập.

- [ ] **[P2-01]** Quản lý phiên đăng nhập trên nhiều thiết bị; thu hồi toàn bộ phiên khi tài khoản bị khóa/đổi mật khẩu.  _(độ khó 4/5)_
- [ ] **[P2-02]** Tra cứu lịch sử đăng nhập, thao tác quan trọng và lịch sử phân công/chuyển Manager.  _(độ khó 4/5)_
- [ ] **[P2-03]** Nâng cao quy trình xác minh Owner: bổ sung hồ sơ, yêu cầu xem xét lại, lưu trạng thái từng bước.  _(độ khó 4/5)_
- [ ] **[P2-04]** Xuất dữ liệu tổng hợp Owner/Manager/Branch theo phạm vi truy cập.  _(độ khó 1/5)_

**Ước lượng task lớn:** 4 mã nguồn, 13 điểm.

### BE-01.7 — Xuất dữ liệu quản lý và báo cáo Owner

**Quy tắc/điều kiện tích hợp:** Chỉ xuất dữ liệu phù hợp quyền; dữ liệu Tenant cần bảo vệ thông tin định danh.

- [ ] **[P2-09]** Xuất danh sách Tenant/cư trú và các dữ liệu phục vụ quản lý hành chính phù hợp.  _(độ khó 1/5)_
- [ ] **[P2-34]** Báo cáo doanh thu/công nợ theo tháng, quý, năm; so sánh giữa các Branch của cùng Owner.  _(độ khó 4/5)_
- [ ] **[P2-37]** Xuất báo cáo CSV/Excel phù hợp quyền truy cập.  _(độ khó 4/5)_

**Ước lượng task lớn:** 3 mã nguồn, 9 điểm.

## PHASE 3 — MỞ RỘNG KHI CÒN THỜI GIAN (P2) — 5 mã / 23 điểm

### BE-01.8 — An toàn Thread: kiểm duyệt và chống spam

**Quy tắc/điều kiện tích hợp:** Chỉ thực hiện khi Phase 3 được duyệt; phối hợp Student 6 (Thread), Student 4 (thông báo kết quả).

- [ ] **[P3-12]** DEFERRED — Quy trình người dùng báo cáo vi phạm độc lập; không moderation_reports hoặc JSONB thay thế.  _(độ khó 4/5)_
- [ ] **[P3-13]** ADMIN ẩn/đóng Thread cơ bản bằng status/moderated_by/moderated_at/moderation_reason; hàng đợi báo cáo vi phạm DEFERRED.  _(độ khó 5/5)_
- [ ] **[P3-15]** Rate limiting tạo/chỉnh Thread ở backend; phần Comment/Suggestion và hệ thống lịch sử vi phạm chi tiết DEFERRED.  _(độ khó 4/5)_

**Ước lượng task lớn:** 3 mã nguồn, 13 điểm.

### BE-01.9 — Tích hợp xác minh Owner bằng eKYC

**Quy tắc/điều kiện tích hợp:** Tích hợp thực tế phụ thuộc nhà cung cấp và chính sách bảo vệ dữ liệu nhạy cảm.

- [ ] **[P3-31]** Tích hợp eKYC với nhà cung cấp xác minh danh tính thực tế cho Owner khi nhóm chọn được giải pháp phù hợp.  _(độ khó 5/5)_
- [ ] **[P3-32]** Theo dõi kết quả xác minh, chống giả mạo yêu cầu xác minh và bảo vệ dữ liệu giấy tờ.  _(độ khó 5/5)_

**Ước lượng task lớn:** 2 mã nguồn, 10 điểm.

## Phụ thuộc và điểm bàn giao BE

- Cung cấp chuẩn xác thực/phân quyền cho Student 2–6; hàm kiểm tra Owner/Manager với Branch cần dùng thống nhất.
- Cung cấp cấu hình tài khoản nhận tiền và mã tham chiếu cho Student 5; Student 5 quyết định trạng thái giao dịch thành công.
- Duyệt Owner liên quan trực tiếp Student 6; hai bên thống nhất luồng khóa/mở quyền Owner.

---

# Phần 2: Student 2

**Phạm vi BE:** Room – Slot / tìm kiếm công khai / Appointment / API đọc & dashboard.

## PHASE 1 — BACKEND BẮT BUỘC (P0) — 29 mã / 66 điểm

### BE-02.1 — Room Type và Room

**Quy tắc/điều kiện tích hợp:** Không trùng mã phòng trong cùng Branch; hỗ trợ cho thuê nguyên phòng hoặc theo slot.

- [ ] **[P1-21]** CRUD và quản lý trạng thái Room Type: tên loại, sức chứa mặc định, mô tả, đặc điểm.  _(độ khó 2/5)_
- [ ] **[P1-22]** CRUD Room trong Branch: mã phòng, loại phòng, giá tham chiếu, sức chứa, mô tả, trạng thái.  _(độ khó 2/5)_
- [ ] **[P1-23]** Không cho trùng mã phòng trong cùng một Branch; chỉ Owner/Manager có quyền mới sửa được phòng.  _(độ khó 1/5)_
- [ ] **[P1-24]** Quản lý hai hình thức cho thuê: THUÊ NGUYÊN PHÒNG và THUÊ THEO SLOT/CHỖ.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 9 điểm.

### BE-02.2 — Slot, sức chứa và trạng thái khả dụng

**Quy tắc/điều kiện tích hợp:** Khả dụng phản ánh hợp đồng và booking còn hiệu lực; kiểm tra sức chứa và chặn thao tác gây xung đột.

- [ ] **[P1-25]** CRUD Room Slot: mã slot, tên, tình trạng khả dụng; slot luôn thuộc một phòng xác định.  _(độ khó 2/5)_
- [ ] **[P1-26]** Quản lý sức chứa, trạng thái còn chỗ, số chỗ đã có người ở và số chỗ có thể đặt.  _(độ khó 2/5)_
- [ ] **[P1-27]** Xác định khả dụng dựa trên hợp đồng/chiếm dụng và yêu cầu giữ chỗ còn hiệu lực, không chỉ dựa vào trạng thái nhập tay.  _(độ khó 5/5)_
- [ ] **[P1-28]** Quản lý trạng thái phòng/slot: khả dụng, đang được giữ, đang thuê, đang bảo trì hoặc ngừng khai thác (theo thực tế).  _(độ khó 4/5)_
- [ ] **[P1-29]** Lọc/tìm kiếm phòng theo Branch, loại phòng, giá, sức chứa và trạng thái; phân trang danh sách.  _(độ khó 1/5)_
- [ ] **[P1-30]** Chặn sửa/xóa/ngừng khai thác làm mất tính nhất quán khi phòng/slot đang có người thuê, booking hoặc nghĩa vụ còn hiệu lực.  _(độ khó 4/5)_

**Ước lượng task lớn:** 6 mã nguồn, 18 điểm.

### BE-02.3 — Tìm kiếm công khai từ dữ liệu Room/Branch

**Quy tắc/điều kiện tích hợp:** Guest xem thông tin công khai, không thấy thông tin Tenant/hợp đồng/công nợ; không xây chức năng đăng tin riêng.

- [ ] **[P1-37]** Cung cấp API công khai danh sách Branch/Room đang được phép hiển thị và có khả năng cho thuê.  _(độ khó 2/5)_
- [ ] **[P1-38]** Tìm kiếm/lọc theo khu vực/địa chỉ, mức giá, loại phòng, sức chứa, tình trạng chỗ ở; bổ sung tiêu chí khác nếu có dữ liệu thực tế.  _(độ khó 4/5)_
- [ ] **[P1-39]** Trả về chi tiết phòng/Branch: mô tả, giá tham chiếu, trạng thái khả dụng và thông tin liên hệ công khai.  _(độ khó 2/5)_
- [ ] **[P1-40]** Phân trang, sắp xếp và lọc nhất quán; không hiển thị thông tin cá nhân Tenant, hợp đồng hoặc công nợ.  _(độ khó 2/5)_
- [ ] **[P1-41]** Tìm kiếm/xem chi tiết không cần đăng nhập; các thao tác đặt lịch/giữ phòng phải đăng nhập.  _(độ khó 1/5)_
- [ ] **[P1-42]** Dữ liệu tìm kiếm lấy trực tiếp từ Branch, Room, Room Type, Room Slot; KHÔNG xây dựng nghiệp vụ đăng tin riêng.  _(độ khó 1/5)_

**Ước lượng task lớn:** 6 mã nguồn, 12 điểm.

### BE-02.4 — Đặt lịch xem phòng

**Quy tắc/điều kiện tích hợp:** Người đặt phải đăng nhập; kiểm tra lịch/trạng thái, không bắt buộc đặt lịch xem trước khi giữ phòng.

- [ ] **[P1-43]** Người dùng đã đăng nhập tạo yêu cầu xem phòng, chọn phòng và thời điểm dự kiến, gửi ghi chú.  _(độ khó 4/5)_
- [ ] **[P1-44]** Owner/Manager xem, xác nhận, đổi lịch hoặc từ chối yêu cầu xem trong Branch được quản lý.  _(độ khó 2/5)_
- [ ] **[P1-45]** Người đặt xem lịch hẹn và hủy yêu cầu của mình theo quy tắc trạng thái.  _(độ khó 2/5)_
- [ ] **[P1-46]** Kiểm tra phòng còn được phép xem, thời gian hợp lệ, tránh lịch hẹn trùng do quy định vận hành.  _(độ khó 4/5)_
- [ ] **[P1-47]** Theo dõi trạng thái: chờ xác nhận, đã xác nhận, đã hoàn thành, đã hủy, từ chối.  _(độ khó 2/5)_
- [ ] **[P1-48]** Đặt lịch xem KHÔNG bắt buộc phải xảy ra trước khi gửi yêu cầu giữ phòng.  _(độ khó 1/5)_

**Ước lượng task lớn:** 6 mã nguồn, 15 điểm.

### BE-02.5 — API đọc hóa đơn và số liệu vận hành

**Quy tắc/điều kiện tích hợp:** Đây là nhóm read-only/aggregation; lấy trạng thái tiền từ Student 5, không tự diễn giải VietQR là đã thu tiền.

- [ ] **[P1-94]** Owner/Manager xem lọc hóa đơn theo Branch, phòng, Tenant, kỳ thu, trạng thái và hạn thanh toán.  _(độ khó 2/5)_
- [ ] **[P1-95]** Tenant xem danh sách, chi tiết hóa đơn và số dư còn nợ được phép xem.  _(độ khó 2/5)_
- [ ] **[P1-127]** Owner xem tổng số Branch, phòng/slot, còn trống, đã thuê, đang giữ, đang bảo trì.  _(độ khó 1/5)_
- [ ] **[P1-128]** Manager xem số liệu giới hạn trong các Branch được phân công.  _(độ khó 1/5)_
- [ ] **[P1-129]** Thống kê tổng tiền phải thu, tiền ĐÃ THU THỰC TẾ, công nợ, hóa đơn quá hạn theo kỳ/Branch.  _(độ khó 4/5)_
- [ ] **[P1-130]** Thống kê số booking, hợp đồng hiệu lực, yêu cầu sửa chữa theo trạng thái cơ bản.  _(độ khó 1/5)_
- [ ] **[P1-131]** Tách tiền thật thực nhận, tiền lập Invoice, nợ, Booking Hold, trả trước chưa dùng, tiền hoàn và doanh thu theo kỳ; không đếm lại trả trước khi áp dụng, không gộp Sandbox.  _(độ khó 1/5)_

**Ước lượng task lớn:** 7 mã nguồn, 12 điểm.

## PHASE 2 — HOÀN THIỆN BACKEND (P1) — 7 mã / 27 điểm

### BE-02.6 — Nâng cấp khả dụng phòng/slot và chuyển chỗ

**Quy tắc/điều kiện tích hợp:** Lịch phòng và di chuyển phải tránh chồng lấn thời gian; báo cáo lấp đầy hỗ trợ hai loại thuê.

- [ ] **[P2-05]** Quản lý các đợt tạm ngừng khai thác phòng/slot theo khoảng ngày (sửa chữa, vệ sinh, chờ nhận phòng).  _(độ khó 4/5)_
- [ ] **[P2-06]** Tự động cảnh báo phòng sắp trống, sắp quá sức chứa và xung đột khả dụng.  _(độ khó 4/5)_
- [ ] **[P2-07]** Hỗ trợ chuyển phòng/chuyển slot và lưu đầy đủ lịch sử chuyển chỗ của Tenant.  _(độ khó 4/5)_
- [ ] **[P2-35]** Thống kê tỷ lệ lấp đầy theo nguyên phòng và theo slot; so sánh biến động từng kỳ.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 16 điểm.

### BE-02.7 — Nhắc lịch/đổi lịch và funnel đặt phòng

**Quy tắc/điều kiện tích hợp:** Chia sẻ sự kiện Booking → Contract của Student 3 cho báo cáo chuyển đổi.

- [ ] **[P2-10]** Tự động nhắc lịch hẹn xem phòng, nhắc Manager xác nhận và thống kê lịch hẹn đã/không diễn ra.  _(độ khó 4/5)_
- [ ] **[P2-11]** Cho phép đổi lịch theo khung thời gian và giới hạn số lượt đặt lịch theo chính sách Branch.  _(độ khó 4/5)_
- [ ] **[P2-14]** Báo cáo tỷ lệ từ đặt lịch → booking → hợp đồng, cùng nguyên nhân hủy/từ chối.  _(độ khó 3/5)_

**Ước lượng task lớn:** 3 mã nguồn, 11 điểm.

## PHASE 3 — MỞ RỘNG KHI CÒN THỜI GIAN (P2) — 7 mã / 30 điểm

### BE-02.8 — Đề xuất phòng vào Thread và tính độ phù hợp

**Quy tắc/điều kiện tích hợp:** Chỉ đề xuất phòng thuộc Branch được phép; gợi ý gần đúng phải chỉ ra điều kiện phù hợp/chưa phù hợp; phối hợp Student 6 về Thread.

- [ ] **[P3-05]** DEFERRED — Đề xuất Room/Slot vào Thread hoãn đến vòng mở rộng.  _(độ khó 4/5)_
- [ ] **[P3-06]** DEFERRED — Chấm mức độ phù hợp của đề xuất phòng trong Thread; chưa có dữ liệu đề xuất.  _(độ khó 5/5)_
- [ ] **[P3-07]** DEFERRED — Giải thích tiêu chí khớp/chưa khớp cho đề xuất Thread.  _(độ khó 4/5)_
- [ ] **[P3-08]** DEFERRED — Chống trùng đề xuất Room/Slot cho Thread.  _(độ khó 4/5)_
- [ ] **[P3-09]** DEFERRED — Cập nhật đề xuất Thread khi khả dụng thay đổi.  _(độ khó 5/5)_

**Ước lượng task lớn:** 5 mã nguồn, 22 điểm.

### BE-02.9 — Tìm phòng nâng cao

**Quy tắc/điều kiện tích hợp:** Chỉ dùng các dữ liệu thực có; không khẳng định tính năng gợi ý AI khi nguồn chưa hỗ trợ.

- [ ] **[P3-33]** Gợi ý phòng tương tự cho người tìm trọ dựa trên tiêu chí và dữ liệu phòng công khai; có thể giải thích nguyên nhân phù hợp.  _(độ khó 4/5)_
- [ ] **[P3-34]** Tăng cường tìm kiếm theo tiện ích, vị trí và ưu tiên gợi ý phù hợp, chỉ khi có dữ liệu đáng tin cậy.  _(độ khó 4/5)_

**Ước lượng task lớn:** 2 mã nguồn, 8 điểm.

## Phụ thuộc và điểm bàn giao BE

- API đọc trạng thái Room/Slot phải phản ánh Booking của Student 3 và dữ liệu cư trú Student 6, không chỉ status cập nhật tay.
- Read API Invoice/Payment và Dashboard phải sử dụng số tiền đã thu hợp lệ từ Student 5; quyền dựa trên Student 1.
- Ở Phase 3, hệ thống gợi ý phòng dùng Thread do Student 6 quản lý; notification do Student 4 gửi.

---

# Phần 3: Student 3

**Phạm vi BE:** Booking – cọc giữ chỗ / Contract / liên kết Zalo cho Tenant.

## PHASE 1 — BACKEND BẮT BUỘC (P0) — 22 mã / 72 điểm

### BE-03.1 — Tạo và duyệt yêu cầu Booking

**Quy tắc/điều kiện tích hợp:** Booking có thể phát sinh khi người dùng chưa là Tenant; phân biệt phòng nguyên căn và slot.

- [ ] **[P1-49]** Người dùng đã đăng nhập gửi yêu cầu giữ nguyên phòng hoặc giữ một slot còn khả dụng; khai báo ngày dự kiến nhận phòng.  _(độ khó 4/5)_
- [ ] **[P1-50]** Tính Booking Hold theo phiên bản chính sách Branch và lưu snapshot; không tự đặt mức tiền mặc định.  _(độ khó 4/5)_
- [ ] **[P1-51]** Owner/Manager xem, chấp thuận/từ chối yêu cầu; Tenant/khách xem trạng thái yêu cầu của mình.  _(độ khó 2/5)_

**Ước lượng task lớn:** 3 mã nguồn, 10 điểm.

### BE-03.2 — Thanh toán cọc và khóa khả dụng

**Quy tắc/điều kiện tích hợp:** Chỉ giữ chỗ sau khi cọc được đối soát; bảo vệ đồng thời hai khách đặt cùng tài nguyên.

- [ ] **[P1-52]** Hướng dẫn QR thủ công hoặc sandbox đã chọn, mã tham chiếu riêng; chỉ giữ chỗ khi Payment đã được xác minh đủ tiền.  _(độ khó 4/5)_
- [ ] **[P1-53]** Chỉ xác nhận giữ phòng thành công sau khi khoản cọc yêu cầu đã được đối soát hợp lệ.  _(độ khó 5/5)_
- [ ] **[P1-54]** Ngăn hai yêu cầu/hợp đồng chiếm giữ trùng một phòng nguyên căn hoặc cùng một slot trong khoảng thời gian xung đột.  _(độ khó 5/5)_
- [ ] **[P1-55]** Hạn thanh toán và thời gian giữ chỗ cấu hình theo Branch; hết hạn giải phóng ResourceAllocation trong transaction, tiền đến muộn để chờ xử lý.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 18 điểm.

### BE-03.3 — Hủy Booking, hợp đồng hóa và lịch sử trạng thái

**Quy tắc/điều kiện tích hợp:** Hoàn/khấu trừ cọc theo chính sách nhóm chốt, không tự ý đặt mức phí hoặc hạn giữ phòng.

- [ ] **[P1-56]** Cho phép hủy booking, lưu lý do và tình trạng xử lý tiền cọc/hoàn cọc theo chính sách đã công bố.  _(độ khó 4/5)_
- [ ] **[P1-57]** Booking tạo Contract chính của Room và ContractTenant đầu tiên, hoặc thêm ContractTenant vào Contract hiện có; source_booking_id nằm ở ContractTenant và là UNIQUE.  _(độ khó 4/5)_
- [ ] **[P1-58]** Ghi nhận lịch sử chuyển trạng thái booking; chống xử lý đồng thời làm giữ chỗ trùng.  _(độ khó 5/5)_

**Ước lượng task lớn:** 3 mã nguồn, 13 điểm.

### BE-03.4 — Tạo hợp đồng và người tham gia

**Quy tắc/điều kiện tích hợp:** Một hợp đồng có đúng một người đại diện khi hiệu lực; ngăn trùng đối tượng thuê cùng thời gian.

- [ ] **[P1-59]** Contract chính thuộc Room, hỗ trợ ROOM/SLOT; thành viên thuê slot chia sẻ Contract chính nhưng có thỏa thuận và nghĩa vụ riêng.  _(độ khó 2/5)_
- [ ] **[P1-60]** Tạo Contract chính hoặc thêm thành viên từ Booking phù hợp; hỗ trợ tạo trực tiếp không cần Booking.  _(độ khó 2/5)_
- [ ] **[P1-61]** Contract lưu điều khoản chung; ContractTenant lưu tiền thuê, tháng lẻ thỏa thuận, PREPAID_RENT tháng đầu/cuối và điều khoản riêng. Phase 1 không có SECURITY_DEPOSIT.  _(độ khó 4/5)_
- [ ] **[P1-62]** Hợp đồng hỗ trợ đối tượng thuê nguyên phòng hoặc thuê theo slot; ngăn xung đột thuê cùng tài nguyên.  _(độ khó 5/5)_
- [ ] **[P1-63]** Thành viên vào/rời có lịch sử; Contract ACTIVE có đúng một đại diện ACTIVE. Đổi đại diện không ghi đè nghĩa vụ cá nhân.  _(độ khó 4/5)_

**Ước lượng task lớn:** 5 mã nguồn, 17 điểm.

### BE-03.5 — Quản lý hiệu lực hợp đồng và cư trú

**Quy tắc/điều kiện tích hợp:** P1-67 là ghi chú loại trừ ký OTP/PDF ở Phase 1, không phải một API riêng.

- [ ] **[P1-64]** Chuyển trạng thái bản nháp, hiệu lực, hết hạn, đã kết thúc/hủy theo điều kiện nghiệp vụ hợp lệ.  _(độ khó 4/5)_
- [ ] **[P1-65]** Tenant được xem hợp đồng của mình; Owner/Manager chỉ xem/sửa hợp đồng thuộc phạm vi quản lý.  _(độ khó 1/5)_
- [ ] **[P1-66]** Chuyển giữ chỗ sang thuê trong một transaction; ResourceAllocation khóa tài nguyên, RoomSlotOccupancy lưu khoảng cư trú thực tế [start,end).  _(độ khó 5/5)_
- [ ] **[P1-67]** Ở Phase 1 KHÔNG bắt buộc ký bằng OTP, chữ ký điện tử hoặc tạo file PDF hợp đồng.  _(độ khó 0/5)_

**Ước lượng task lớn:** 4 mã nguồn, 10 điểm.

### BE-03.6 — Liên kết Zalo Group của Branch

**Quy tắc/điều kiện tích hợp:** Chỉ người thuê hợp lệ mới được lấy link; chỉ xử lý URL, không tích hợp Zalo API.

- [ ] **[P1-120]** Owner/Manager cấu hình, cập nhật, vô hiệu hóa đường dẫn Zalo Group của Branch.  _(độ khó 2/5)_
- [ ] **[P1-121]** Kiểm tra định dạng/đường dẫn hợp lệ; chỉ trả link cho người thuê có quan hệ thuê hợp lệ tại Branch.  _(độ khó 1/5)_
- [ ] **[P1-122]** Tenant lấy link nhóm Zalo của Branch đang ở; không tích hợp nhắn tin bằng Zalo API ở Phase 1.  _(độ khó 1/5)_

**Ước lượng task lớn:** 3 mã nguồn, 4 điểm.

## PHASE 2 — HOÀN THIỆN BACKEND (P1) — 5 mã / 18 điểm

### BE-03.7 — Tiền cọc, điều chỉnh và hoàn trả nâng cao

**Quy tắc/điều kiện tích hợp:** Liên kết Student 5 để thực hiện các bút toán hoàn/khấu trừ có xác nhận.

- [ ] **[P2-13]** Hoàn thiện hủy/hoàn Booking Hold và chuyển sang PREPAID_RENT có audit trên cùng Payment; không thêm lần thu tiền. Chính sách cụ thể cần phê duyệt.  _(độ khó 5/5)_

**Ước lượng task lớn:** 1 mã nguồn, 5 điểm.

### BE-03.8 — Tra cứu, gia hạn và trả phòng theo hợp đồng

**Quy tắc/điều kiện tích hợp:** Không cho sửa hợp đồng kết thúc trái trạng thái, giữ lịch sử thay đổi.

- [ ] **[P2-15]** Tìm kiếm/lọc hợp đồng theo Tenant, phòng, Branch, ngày bắt đầu/hết hạn, trạng thái.  _(độ khó 1/5)_
- [ ] **[P2-16]** Nhắc hết hạn, gia hạn và thay điều khoản qua CONTRACT_AMENDMENTS có version/effective_from; giữ nguyên thỏa thuận gốc đã duyệt.  _(độ khó 4/5)_
- [ ] **[P2-17]** Yêu cầu → duyệt → chuyển đi lưu trên CONTRACT_TENANTS và AUDIT; chỉ giữ một yêu cầu hiện hành, không kết thúc nghĩa vụ người ở ghép khác.  _(độ khó 4/5)_
- [ ] **[P2-20]** Khóa các trạng thái không hợp lệ, hạn chế sửa nội dung hợp đồng đã kết thúc; lưu lịch sử thay đổi.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 13 điểm.

## PHASE 3 — MỞ RỘNG KHI CÒN THỜI GIAN (P2) — 7 mã / 28 điểm

### BE-03.9 — PDF và ký OTP mô phỏng

**Quy tắc/điều kiện tích hợp:** Bản ký OTP chỉ là mô phỏng, không mặc định tương đương chữ ký số được chứng nhận; P3-23 là ghi chú tuân thủ.

- [ ] **[P3-17]** PDF Cloudinary chính tham chiếu tại CONTRACTS, phụ lục/phiên bản bổ sung tại CONTRACT_AMENDMENTS; không bảng documents riêng.  _(độ khó 5/5)_
- [ ] **[P3-18]** EXTENSION NOT IMPLEMENTED — OTP ký mô phỏng dùng challenge hash hiện hành USERS, ràng buộc đúng tài liệu/người ký tại backend; chưa có luồng gửi/kiểm tra OTP hoàn chỉnh.  _(độ khó 5/5)_
- [ ] **[P3-19]** EXTENSION NOT IMPLEMENTED — Bằng chứng xác nhận tối thiểu trong confirmation_evidence của CONTRACTS/CONTRACT_AMENDMENTS: signer, thời điểm, content hash; không OTP plaintext/hash trong evidence/audit.  _(độ khó 5/5)_
- [ ] **[P3-20]** EXTENSION NOT IMPLEMENTED — Backend kiểm tra danh sách signer và trạng thái ký mô phỏng trong transaction; JSONB không thay thế FK/authorization.  _(độ khó 5/5)_
- [ ] **[P3-21]** Khóa document snapshot đã phát hành xác nhận; sau xác nhận muốn đổi tạo amendment mới, không ghi đè nội dung đã duyệt.  _(độ khó 4/5)_
- [ ] **[P3-22]** EXTENSION NOT IMPLEMENTED — Tài liệu phụ lục/gia hạn/thanh lý dùng CONTRACT_AMENDMENTS và Cloudinary, bằng chứng ký tối giản.  _(độ khó 4/5)_
- [ ] **[P3-23]** LƯU Ý: Đây là ký OTP MÔ PHỎNG trong dự án môn học, không mặc nhiên là chữ ký số được chứng nhận có giá trị pháp lý tương đương.  _(độ khó 0/5)_

**Ước lượng task lớn:** 7 mã nguồn, 28 điểm.

## Phụ thuộc và điểm bàn giao BE

- Booking yêu cầu kiểm tra khả dụng Room/Slot cùng Student 2, đặt cọc đã thu qua Student 5, cư trú/slot do Student 6 kiểm soát.
- Khi ký/tạo hiệu lực hợp đồng, Student 6 nhận sự kiện cư trú; Student 4 nhận dữ liệu dịch vụ theo hợp đồng.
- Liên kết Zalo chỉ trả theo Branch mà Tenant thực sự đang cư trú; Owner/Manager cập nhật phải có quyền từ Student 1.

---

# Phần 4: Student 4

**Phạm vi BE:** Services – điện nước – phân bổ chi phí / Notification.

## PHASE 1 — BACKEND BẮT BUỘC (P0) — 20 mã / 72 điểm

### BE-04.1 — Danh mục và cấu hình dịch vụ

**Quy tắc/điều kiện tích hợp:** Điện/nước mặc định không cho hủy; dịch vụ tùy chọn được đăng ký/hủy đúng kỳ và quyền Tenant.

- [ ] **[P1-68]** Owner/Manager quản lý danh mục Services: tên, đơn vị, cách tính phí, đơn giá tham chiếu, trạng thái.  _(độ khó 4/5)_
- [ ] **[P1-69]** Cấu hình dịch vụ và đơn giá áp dụng theo phòng/Branch trong phạm vi phụ trách.  _(độ khó 4/5)_
- [ ] **[P1-70]** Điện, nước là dịch vụ mặc định; không cho Tenant tự hủy. Dịch vụ tiêu thụ 0 thì phí theo mức tiêu thụ bằng 0.  _(độ khó 2/5)_
- [ ] **[P1-71]** Các dịch vụ bổ sung là tùy chọn; Tenant xem dịch vụ và gửi đăng ký/hủy dịch vụ đã đăng ký.  _(độ khó 4/5)_
- [ ] **[P1-72]** Owner/Manager xem và xử lý việc đăng ký/hủy dịch vụ tùy chọn; kiểm tra quyền thuê hợp lệ và kỳ hiệu lực.  _(độ khó 4/5)_
- [ ] **[P1-73]** Dịch vụ theo Branch → Room → Contract → ContractTenant; thời hạn và giá ghi đè có snapshot. Điện/nước bắt buộc, dịch vụ tùy chọn phải duyệt.  _(độ khó 2/5)_

**Ước lượng task lớn:** 6 mã nguồn, 20 điểm.

### BE-04.2 — Chỉ số điện/nước và tính phí

**Quy tắc/điều kiện tích hợp:** Lượng dùng không âm; tính phí đúng kỳ; giá mới không sửa hóa đơn đã phát hành.

- [ ] **[P1-74]** Nhập chỉ số điện/nước trực tiếp trong RoomServiceCharge theo segment; hỗ trợ thay/reset đồng hồ và đổi giá, không tạo bảng Meter riêng.  _(độ khó 4/5)_
- [ ] **[P1-75]** Tính lượng dùng, đơn giá, tổng tiền cho từng phòng và kỳ thanh toán; hạn chế tính trùng một khoản.  _(độ khó 5/5)_
- [ ] **[P1-76]** Tính dịch vụ kiểu theo chỉ số, theo người, theo phòng, cố định hoặc theo lượt theo cấu hình được phép.  _(độ khó 4/5)_
- [ ] **[P1-77]** Khi thay đổi đơn giá, chỉ áp dụng cho các kỳ tính phí phù hợp; không tự sửa hóa đơn đã phát hành.  _(độ khó 4/5)_
- [ ] **[P1-78]** Cho phép xem lại nguồn gốc số tiền dịch vụ (kỳ tính, chỉ số, số lượng, đơn giá) để đối chiếu.  _(độ khó 1/5)_

**Ước lượng task lớn:** 5 mã nguồn, 18 điểm.

### BE-04.3 — Chia phí giữa các Tenant

**Quy tắc/điều kiện tích hợp:** Mặc định theo ngày cư trú; Tenant đề xuất thay đổi, Manager/Owner duyệt; tổng phân bổ phải khớp phí gốc.

- [ ] **[P1-79]** Mặc định chia phí theo ngày cư trú thực tế giao với kỳ/segment; làm tròn VND bằng largest remainder, hòa thì theo UUID; tổng trọng số 0 phải báo lỗi.  _(độ khó 4/5)_
- [ ] **[P1-80]** Tenant gửi CostAllocationProposal với allocation_details JSONB và lý do; không có bảng cost_allocation_items.  _(độ khó 4/5)_
- [ ] **[P1-81]** Manager/Owner xem, chấp thuận hoặc từ chối yêu cầu; ghi nhận phương án phân bổ cuối cùng.  _(độ khó 4/5)_
- [ ] **[P1-82]** Tính toán tổng số phân bổ không vượt/thiếu số tiền dịch vụ của phòng; xử lý phần làm tròn VND nhất quán.  _(độ khó 5/5)_
- [ ] **[P1-83]** Chỉ phương án được duyệt mới dùng để chốt khoản phải trả; lịch sử đề xuất và duyệt được tra cứu.  _(độ khó 4/5)_
- [ ] **[P1-84]** Không tự thay đổi khoản phải trả trong hóa đơn đã chốt khi Tenant yêu cầu chia lại; thực hiện điều chỉnh có kiểm soát nếu được cho phép.  _(độ khó 4/5)_

**Ước lượng task lớn:** 6 mã nguồn, 25 điểm.

### BE-04.4 — Sự kiện và thông báo trong ứng dụng

**Quy tắc/điều kiện tích hợp:** Gửi đến người liên quan, không lộ dữ liệu riêng tư; Student 4 chịu trách nhiệm cơ chế chung; các Student khác phát sự kiện nghiệp vụ.

- [ ] **[P1-123]** Tạo thông báo khi có lịch xem phòng, yêu cầu giữ chỗ, tiền cọc, hợp đồng, hóa đơn, thanh toán, yêu cầu sửa chữa và thay đổi quan trọng.  _(độ khó 4/5)_
- [ ] **[P1-124]** Gửi thông báo đúng người nhận theo vai trò và phạm vi Branch; không gửi dữ liệu riêng tư cho người không liên quan.  _(độ khó 4/5)_
- [ ] **[P1-125]** Người dùng lấy danh sách thông báo, lọc đã đọc/chưa đọc và đánh dấu đã đọc.  _(độ khó 1/5)_

**Ước lượng task lớn:** 3 mã nguồn, 9 điểm.

## PHASE 2 — HOÀN THIỆN BACKEND (P1) — 5 mã / 17 điểm

### BE-04.5 — Nhập số điện nước và kiểm soát phân bổ nâng cao

**Quy tắc/điều kiện tích hợp:** Cảnh báo số liệu bất thường, hỗ trợ tra cứu và đối chiếu nhiều kỳ.

- [ ] **[P2-21]** Nhập chỉ số điện/nước hàng loạt theo Branch; phát hiện chỉ số tăng đột biến hoặc thiếu kỳ.  _(độ khó 4/5)_
- [ ] **[P2-22]** Cảnh báo sai lệch khi phân bổ chi phí, tự kiểm tra tổng chia tiền điện/nước.  _(độ khó 4/5)_
- [ ] **[P2-23]** Tra cứu và so sánh chi phí dịch vụ qua nhiều kỳ, theo phòng và người thuê.  _(độ khó 1/5)_

**Ước lượng task lớn:** 3 mã nguồn, 9 điểm.

### BE-04.6 — Tùy chọn thông báo và thống kê phí dịch vụ

**Quy tắc/điều kiện tích hợp:** Báo cáo dịch vụ dùng kết quả phí đã khóa; không làm biến động nghĩa vụ thanh toán cũ.

- [ ] **[P2-33]** Preferences thông báo trong USERS.notification_preferences JSONB; backend kiểm tra dữ liệu cấu hình. Không tạo bảng preferences.  _(độ khó 4/5)_
- [ ] **[P2-36]** Thống kê dịch vụ, giá trị tài sản, chi phí sửa chữa và khả năng thu hồi công nợ.  _(độ khó 4/5)_

**Ước lượng task lớn:** 2 mã nguồn, 8 điểm.

## PHASE 3 — MỞ RỘNG KHI CÒN THỜI GIAN (P2) — 6 mã / 24 điểm

### BE-04.7 — Notification cho Thread và job tự đóng

**Quy tắc/điều kiện tích hợp:** Phối hợp Student 6 xử lý vòng đời Thread và Student 1 về quyết định kiểm duyệt.

- [ ] **[P3-10]** DEFERRED — Thông báo Comment/Suggestion và gợi ý Thread theo phòng trống.  _(độ khó 4/5)_
- [ ] **[P3-11]** Cho phép tự đóng Thread theo thời hạn không hoạt động do hệ thống cấu hình.  _(độ khó 4/5)_
- [ ] **[P3-16]** Thông báo kết quả kiểm duyệt cho người liên quan theo chính sách hệ thống.  _(độ khó 4/5)_

**Ước lượng task lớn:** 3 mã nguồn, 12 điểm.

### BE-04.8 — Thông báo qua email/SMS

**Quy tắc/điều kiện tích hợp:** Chỉ làm khi Phase 3 được triển khai; có mẫu thông báo, retry, trạng thái gửi lỗi.

- [ ] **[P3-28]** Email là dòng NOTIFICATIONS channel EMAIL, chống trùng recipient/event/type/channel; worker gửi/retry sau commit, không outbox riêng.  _(độ khó 4/5)_
- [ ] **[P3-29]** SMS là dòng NOTIFICATIONS channel SMS; OTP chỉ hash trên USERS, dịch vụ gửi và danh tính người nhận kiểm tra tại backend.  _(độ khó 4/5)_
- [ ] **[P3-30]** Template cố định trong source/config có version theo deployment; delivery_status/attempt_count/next_attempt_at trên NOTIFICATIONS. Không tạo bảng template/job.  _(độ khó 4/5)_

**Ước lượng task lớn:** 3 mã nguồn, 12 điểm.

## Phụ thuộc và điểm bàn giao BE

- Phí dịch vụ sau khi chốt là dữ liệu đầu vào của hóa đơn Student 5; Student 5 không tính lại tiền điện nước.
- Tất cả Student khác phát event cho Student 4 về lịch xem, booking, hợp đồng, thanh toán và sửa chữa.
- Điều chỉnh tỷ lệ phí Tenant phải thống nhất người ở thực tế với Student 6 và kỳ hóa đơn Student 5.

---

# Phần 5: Student 5

**Phạm vi BE:** Invoice – công nợ – Payment – QR thủ công/Sandbox.

## PHASE 1 — BACKEND BẮT BUỘC (P0) — 17 mã / 73 điểm

### BE-05.1 — Lập hóa đơn, dòng hóa đơn và chốt số liệu

**Quy tắc/điều kiện tích hợp:** Một kỳ không phát hành trùng hóa đơn; số tiền và đơn giá đã chốt không đổi theo cấu hình giá mới.

- [ ] **[P1-85]** Owner/Manager lập một Invoice riêng cho mỗi ContractTenant/tháng; unique khi chưa CANCELLED, hóa đơn thay thế phải có truy vết.  _(độ khó 4/5)_
- [ ] **[P1-86]** InvoiceItems gồm các dòng phí/giảm trừ và sửa chữa được duyệt; PREPAID_APPLICATION là dòng áp dụng trả trước, không cộng vào tổng phí hay tiền thực nhận.  _(độ khó 2/5)_
- [ ] **[P1-87]** Tổng tiền khớp dòng phí; due_date theo collection_day + grace_days snapshot, ngày vượt cuối tháng chặn về cuối tháng; tiền thuê tháng lẻ phải thỏa thuận.  _(độ khó 4/5)_
- [ ] **[P1-88]** Ghi nhận đơn giá/số lượng/số tiền tại thời điểm phát hành để các hóa đơn cũ không bị đổi khi cập nhật giá.  _(độ khó 4/5)_
- [ ] **[P1-89]** Cho phép xem hóa đơn của phòng có nhiều người thuê; chỉ người có quyền mới thấy chi tiết và nghĩa vụ của mình.  _(độ khó 2/5)_

**Ước lượng task lớn:** 5 mã nguồn, 16 điểm.

### BE-05.2 — Công nợ từng Tenant và thanh toán từng phần

**Quy tắc/điều kiện tích hợp:** Phân biệt người thực trả và người được trừ nợ; hỗ trợ trả hộ/trả từng phần; đối chiếu tổng công nợ.

- [ ] **[P1-90]** Theo dõi nghĩa vụ và công nợ riêng ContractTenant qua Invoice; đại diện không gộp công nợ thành viên.  _(độ khó 5/5)_
- [ ] **[P1-91]** Cho phép một người thanh toán hộ thành viên khác nhưng phải phân biệt người chuyển tiền với người được ghi giảm công nợ.  _(độ khó 5/5)_
- [ ] **[P1-92]** Hỗ trợ thanh toán TỪNG PHẦN, cộng dồn chính xác; không vượt công nợ nếu không có xử lý khoản dư được phê duyệt.  _(độ khó 5/5)_
- [ ] **[P1-93]** Tách document_status DRAFT/ISSUED/CANCELLED khỏi payment_status; quá hạn suy ra từ due_date và dư nợ. Owner/Manager phát hành; Owner duyệt điều chỉnh sau phát hành.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 19 điểm.

### BE-05.3 — Tích hợp nhận giao dịch và xác thực webhook

**Quy tắc/điều kiện tích hợp:** QR thủ công hoặc sandbox đã chọn; chống callback giả/lặp khi dùng sandbox; ghi nhận đúng Invoice/Booking.

- [ ] **[P1-99]** Phase 1 chọn QR thủ công hoặc VNPay Sandbox hoặc MoMo Sandbox; không bắt buộc API ngân hàng thật hoặc triển khai cả hai sandbox.  _(độ khó 5/5)_
- [ ] **[P1-100]** QR thủ công: Owner/Manager đúng Branch xác minh và lưu chứng cứ/audit. Sandbox: backend xác minh chữ ký callback; không tin trạng thái client tự khai.  _(độ khó 5/5)_
- [ ] **[P1-101]** Đối chiếu tham chiếu, tài khoản nhà cung cấp, môi trường, tiền và trạng thái; Payment có tối đa một đích trực tiếp Invoice hoặc Booking, hoặc chưa khớp.  _(độ khó 5/5)_
- [ ] **[P1-102]** Chống trùng provider transaction, request idempotency và webhook event; PaymentWebhookEvent lưu chứng cứ xử lý, không có BankTransaction.  _(độ khó 5/5)_

**Ước lượng task lớn:** 4 mã nguồn, 20 điểm.

### BE-05.4 — Ghi Payment, cập nhật nợ và xử lý ngoại lệ

**Quy tắc/điều kiện tích hợp:** Hỗ trợ nhiều lần chuyển khoản và nhiều người trả; xác nhận thủ công là luồng hợp lệ của QR thủ công và luôn có audit.

- [ ] **[P1-103]** Ghi nhận Payment thành công, thất bại, đang xử lý; cập nhật công nợ Invoice/điều kiện giữ chỗ Booking sau đối soát.  _(độ khó 5/5)_
- [ ] **[P1-104]** Hỗ trợ một hóa đơn có nhiều lượt chuyển khoản hoặc nhiều người đóng; tổng số tiền đã thu phải được tính chính xác.  _(độ khó 5/5)_
- [ ] **[P1-105]** Cung cấp lịch sử giao dịch, thời điểm nhận tiền và trạng thái đối soát cho người có quyền.  _(độ khó 4/5)_
- [ ] **[P1-106]** Owner/Manager rà soát giao dịch; xác nhận thủ công là luồng chính hợp lệ của QR thủ công. Tiền dư/chưa khớp nằm ở Payment; hoàn có phê duyệt/audit, không có ví Tenant.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 18 điểm.

## PHASE 2 — HOÀN THIỆN BACKEND (P1) — 7 mã / 28 điểm

### BE-05.5 — Nhắc cọc và tất toán kỳ cuối

**Quy tắc/điều kiện tích hợp:** Phối hợp Student 3: Booking Hold/PREPAID_RENT; không tự khấu trừ khi chính sách chưa chốt.

- [ ] **[P2-12]** Tự động nhắc thanh toán cọc, sắp hết hạn giữ chỗ và kiểm tra booking tồn đọng.  _(độ khó 4/5)_
- [ ] **[P2-18]** Lập Invoice kỳ cuối và áp dụng PREPAID_RENT tháng cuối; rời sớm cần quyết định hoàn/giữ/phân bổ lại được duyệt, không tự suy diễn hoặc gọi là SECURITY_DEPOSIT.  _(độ khó 5/5)_

**Ước lượng task lớn:** 2 mã nguồn, 9 điểm.

### BE-05.6 — Hóa đơn định kỳ và quản lý quá hạn

**Quy tắc/điều kiện tích hợp:** Có cơ chế tránh phát hành trùng kỳ và kiểm soát trước phát hành.

- [ ] **[P2-24]** Tạo hóa đơn định kỳ theo lịch; kiểm tra tránh lập trùng, hỗ trợ duyệt trước khi phát hành.  _(độ khó 5/5)_
- [ ] **[P2-25]** Nhắc hóa đơn đến hạn/quá hạn và thống kê tuổi nợ theo khoảng ngày.  _(độ khó 3/5)_

**Ước lượng task lớn:** 2 mã nguồn, 8 điểm.

### BE-05.7 — Điều chỉnh, đối soát ngoại lệ và biên nhận

**Quy tắc/điều kiện tích hợp:** Không xóa âm thầm lịch sử tiền; xử lý khoản dư/thiếu/hoàn với trạng thái rõ ràng.

- [ ] **[P2-26]** Quy trình điều chỉnh hóa đơn đã phát hành (ghi nhận lý do, người duyệt, khoản bù trừ) mà không sửa âm thầm lịch sử tiền.  _(độ khó 5/5)_
- [ ] **[P2-27]** Xử lý chuyển khoản sai nội dung, chuyển khoản dư/thiếu, hoàn tiền và đối soát ngoại lệ nâng cao.  _(độ khó 5/5)_
- [ ] **[P2-28]** Xuất hóa đơn/biên nhận dạng file phục vụ tra soát và lưu trữ (khác PDF hợp đồng ký OTP ở Phase 3).  _(độ khó 1/5)_

**Ước lượng task lớn:** 3 mã nguồn, 11 điểm.

## PHASE 3 — MỞ RỘNG KHI CÒN THỜI GIAN (P2) — 4 mã / 20 điểm

### BE-05.8 — Tích hợp MoMo và hợp nhất đối soát

**Quy tắc/điều kiện tích hợp:** Chỉ triển khai khi Phase 3 được duyệt; bảo vệ chữ ký callback và tính idempotent.

- [ ] **[P3-24]** Bổ sung MoMo Sandbox nếu chưa chọn ở Phase 1; giữ tính tùy chọn Phase 3, không bắt buộc tích hợp tiền thật.  _(độ khó 5/5)_
- [ ] **[P3-25]** Mở rộng callback MoMo Sandbox với chữ ký, idempotency và retry; tái sử dụng Payments/PaymentWebhookEvents.  _(độ khó 5/5)_
- [ ] **[P3-26]** Hợp nhất lịch sử QR thủ công/VNPay Sandbox/MoMo Sandbox, luôn tách REAL và SANDBOX theo Invoice/Booking/người nộp.  _(độ khó 5/5)_
- [ ] **[P3-27]** Đối soát giao dịch thất bại/hủy/hoàn tiền và xử lý webhook lặp, đến trễ hoặc khác thứ tự.  _(độ khó 5/5)_

**Ước lượng task lớn:** 4 mã nguồn, 20 điểm.

## Phụ thuộc và điểm bàn giao BE

- Nhận đơn giá/chi phí phân bổ được chốt từ Student 4; không sửa tự động hóa đơn lịch sử.
- Hỗ trợ API xác nhận tiền cọc cho Student 3, nguồn tài khoản/QR từ Student 1.
- Công khai hợp đồng đọc/trạng thái đã trả cho Student 2 mà không lộ thông tin giao dịch hoặc tenant ngoài phạm vi.

---

# Phần 6: Student 6

**Phạm vi BE:** Tenant – CSVC – sửa chữa / kiểm thử tích hợp – deploy / Thread nền tảng.

## PHASE 1 — BACKEND BẮT BUỘC (P0) — 26 mã / 71 điểm

### BE-06.1 — Kiểm duyệt xác minh Owner

**Quy tắc/điều kiện tích hợp:** Admin xử lý hồ sơ Owner; thông tin giấy tờ cá nhân phải giới hạn người truy cập.

- [ ] **[P1-08]** Thông tin xác minh Owner hiện hành nằm trên USERS, giấy tờ Cloudinary; chỉ ADMIN được phê duyệt/từ chối, audit theo whitelist không ghi secrets.  _(độ khó 4/5)_
- [ ] **[P1-132]** ADMIN xử lý owner_verification_status và thông tin review trên USERS; audit chỉ field được phép, không audit toàn bộ bản ghi User.  _(độ khó 4/5)_

**Ước lượng task lớn:** 2 mã nguồn, 8 điểm.

### BE-06.2 — Quản lý Tenant, hợp đồng tham gia và chỗ ở

**Quy tắc/điều kiện tích hợp:** Tenant xem dữ liệu đúng quan hệ thuê; tránh quá sức chứa/chồng lấn slot; phối hợp Student 3.

- [ ] **[P1-31]** Hồ sơ cá nhân hiện tại dùng USERS; CONTRACT_TENANTS.tenant_user_id lưu quan hệ thuê và tenant_snapshot lúc ký. Owner/Manager chỉ thấy dữ liệu trong phạm vi thuê, không tự sửa hồ sơ chung hoặc chiếm tài khoản.  _(độ khó 2/5)_
- [ ] **[P1-32]** Tra cứu người thuê theo Branch/Room/Contract qua CONTRACT_TENANTS → USERS; một User được thuê nhiều Owner, giữ snapshot và lịch sử từng lần tham gia.  _(độ khó 2/5)_
- [ ] **[P1-33]** Quản lý quan hệ Tenant tham gia Contract: người đại diện, thành viên, ngày vào/ra dự kiến, ngày ra thực tế.  _(độ khó 2/5)_
- [ ] **[P1-34]** Ghi nhận việc bố trí Tenant vào Room Slot và lịch sử chiếm dụng chỗ ở.  _(độ khó 2/5)_
- [ ] **[P1-35]** Kiểm tra không vượt quá sức chứa, không cho một slot bị chiếm dụng trùng thời gian trái quy tắc.  _(độ khó 4/5)_
- [ ] **[P1-36]** Chỉ cho Tenant xem dữ liệu cá nhân, hợp đồng, phòng và chi phí mà họ có quyền truy cập.  _(độ khó 1/5)_

**Ước lượng task lớn:** 6 mã nguồn, 13 điểm.

### BE-06.3 — Quản lý Furniture và bảo hành

**Quy tắc/điều kiện tích hợp:** Không xóa CSVC gây đứt liên kết yêu cầu sửa chữa đang mở.

- [ ] **[P1-107]** Owner/Manager CRUD Furniture/CSVC theo Room: tên, loại, mã tài sản, serial, ngày mua, giá trị, tình trạng, trạng thái.  _(độ khó 2/5)_
- [ ] **[P1-108]** Quản lý thông tin bảo hành của từng CSVC: đơn vị bảo hành, thời gian, điều kiện, mã bảo hành.  _(độ khó 1/5)_
- [ ] **[P1-109]** Tra cứu CSVC theo phòng, trạng thái, loại tài sản; quản lý lịch sử tình trạng khi có sửa chữa.  _(độ khó 2/5)_
- [ ] **[P1-110]** Chặn thao tác xóa hoặc thay đổi tài sản làm mất liên kết với yêu cầu sửa chữa đang xử lý.  _(độ khó 1/5)_

**Ước lượng task lớn:** 4 mã nguồn, 6 điểm.

### BE-06.4 — Báo hỏng và lịch sử xử lý

**Quy tắc/điều kiện tích hợp:** Có thể báo Furniture hoặc Service; nhiều lần xử lý và ảnh; chỉ tính Tenant khoản phí sửa chữa đã duyệt.

- [ ] **[P1-111]** Tenant có quyền gửi báo hỏng Furniture HOẶC Service của phòng đang thuê (điện, nước, internet...).  _(độ khó 2/5)_
- [ ] **[P1-112]** Tạo Report Request: tiêu đề, nội dung, loại yêu cầu, mức độ ưu tiên, phòng liên quan và thông tin minh chứng.  _(độ khó 2/5)_
- [ ] **[P1-113]** Cho phép đính kèm và truy xuất hình ảnh trước/sau sửa chữa theo đúng quyền.  _(độ khó 4/5)_
- [ ] **[P1-114]** Owner/Manager xem, tiếp nhận, từ chối, bắt đầu xử lý và hoàn thành yêu cầu trong Branch phụ trách.  _(độ khó 2/5)_
- [ ] **[P1-115]** Mỗi yêu cầu có một người chịu trách nhiệm xử lý chính tại một thời điểm; tránh cập nhật trạng thái xung đột.  _(độ khó 4/5)_
- [ ] **[P1-116]** Lưu NHIỀU lần xử lý Report Records: thời gian, người xử lý, công việc, kết quả, chi phí, trạng thái sau xử lý.  _(độ khó 4/5)_
- [ ] **[P1-117]** Phân định trách nhiệm chi phí: Owner chịu hao mòn/hư do CSVC cũ; Tenant chịu khoản hỏng do lỗi Tenant khi được xác nhận.  _(độ khó 4/5)_
- [ ] **[P1-118]** Chỉ cộng chi phí Tenant phải trả vào hóa đơn sau khi Owner/Manager duyệt trách nhiệm và số tiền.  _(độ khó 4/5)_
- [ ] **[P1-119]** Tenant xem lịch sử/trạng thái yêu cầu của mình; thông báo khi tiếp nhận, cập nhật hoặc hoàn thành.  _(độ khó 1/5)_

**Ước lượng task lớn:** 9 mã nguồn, 27 điểm.

### BE-06.5 — Nền tảng API, chất lượng và deploy

**Quy tắc/điều kiện tích hợp:** Là đầu mối conventions, transaction test và deploy PostgreSQL; từng Student vẫn chịu trách nhiệm test/validation module riêng.

- [ ] **[P1-126]** Đảm bảo sự kiện nghiệp vụ không gây gửi thông báo trùng bất hợp lý.  _(độ khó 2/5)_
- [ ] **[P1-133]** API REST nhất quán, DTO/validation, xử lý lỗi và mã trạng thái phù hợp.  _(độ khó 2/5)_
- [ ] **[P1-135]** Transaction và kiểm soát cạnh tranh với Booking, Contract, Invoice, Payment và công nợ.  _(độ khó 5/5)_
- [ ] **[P1-137]** Kiểm thử tự động cho service và API quan trọng; kiểm thử âm tính về quyền, dữ liệu sai, booking trùng và webhook lặp.  _(độ khó 4/5)_
- [ ] **[P1-138]** Cấu hình PostgreSQL, môi trường chạy/deploy, tài liệu API và kịch bản dữ liệu demo để giảng viên có thể dùng thử.  _(độ khó 4/5)_

**Ước lượng task lớn:** 5 mã nguồn, 17 điểm.

## PHASE 2 — HOÀN THIỆN BACKEND (P1) — 8 mã / 33 điểm

### BE-06.6 — Nâng cao quản lý cư trú và bàn giao

**Quy tắc/điều kiện tích hợp:** Đổi người đại diện/thành viên có duyệt; bàn giao và đối chiếu CSVC cuối kỳ.

- [ ] **[P2-08]** Quản lý thay đổi người đại diện hoặc thành viên cùng thuê theo quy trình xét duyệt.  _(độ khó 4/5)_
- [ ] **[P2-19]** Bàn giao/thu hồi CSVC dùng handover_snapshot/evidence trên CONTRACT_TENANTS, lịch sử trạng thái qua audit; không bảng bàn giao riêng.  _(độ khó 4/5)_

**Ước lượng task lớn:** 2 mã nguồn, 8 điểm.

### BE-06.7 — Bảo hành, SLA và chi phí sửa chữa

**Quy tắc/điều kiện tích hợp:** SLA, đánh giá hoàn tất và thống kê chi phí theo bên chịu.

- [ ] **[P2-29]** Theo dõi tình trạng bảo hành sắp hết hạn và đề xuất xử lý CSVC có nhiều lần hỏng.  _(độ khó 3/5)_
- [ ] **[P2-30]** Cấu hình thời hạn xử lý theo mức độ ưu tiên; cảnh báo yêu cầu sửa chữa tồn đọng.  _(độ khó 4/5)_
- [ ] **[P2-31]** Tenant đánh giá chất lượng xử lý sau khi yêu cầu sửa chữa hoàn thành.  _(độ khó 4/5)_
- [ ] **[P2-32]** Thống kê chi phí sửa chữa theo phòng, Branch, nguyên nhân và bên chịu chi phí.  _(độ khó 4/5)_

**Ước lượng task lớn:** 4 mã nguồn, 15 điểm.

### BE-06.8 — Kiểm thử tích hợp và tối ưu vận hành

**Quy tắc/điều kiện tích hợp:** Hỗ trợ test E2E quy trình BE và tối ưu truy vấn/sao lưu; không chuyển trách nhiệm test cá nhân sang một người.

- [ ] **[P2-38]** Tăng độ bao phủ kiểm thử tích hợp: trọn luồng thuê phòng, tính tiền, thanh toán một phần, trả phòng.  _(độ khó 5/5)_
- [ ] **[P2-39]** Tối ưu truy vấn/phân trang, xử lý tác vụ định kỳ và sao lưu/khôi phục vận hành.  _(độ khó 5/5)_

**Ước lượng task lớn:** 2 mã nguồn, 10 điểm.

## PHASE 3 — MỞ RỘNG KHI CÒN THỜI GIAN (P2) — 5 mã / 17 điểm

### BE-06.9 — Thread tìm trọ: CRUD và trạng thái

**Quy tắc/điều kiện tích hợp:** Chỉ tài khoản đăng nhập xem/thao tác; tự đóng khi hết hạn hoặc theo quyết định ADMIN; phụ thuộc Student 1/2/4.

- [ ] **[P3-01]** Người dùng đã đăng nhập tạo, xem, chỉnh sửa, đóng và tra cứu Thread tìm trọ của mình.  _(độ khó 4/5)_
- [ ] **[P3-02]** Thread có tiêu đề, mô tả và tiêu chí: khu vực, mức giá, diện tích, sức chứa, tiện ích, thời gian dự kiến chuyển vào (nếu áp dụng).  _(độ khó 4/5)_
- [ ] **[P3-03]** Chỉ người đã đăng nhập được XEM hoặc tương tác Thread; Guest không được xem Thread.  _(độ khó 1/5)_
- [ ] **[P3-04]** DEFERRED — Comment Thread hoãn đến vòng mở rộng; không bảng hoặc JSONB giả lập comment.  _(độ khó 4/5)_
- [ ] **[P3-14]** Tự động đóng Thread theo quy tắc hết hạn, vi phạm hoặc yêu cầu chủ Thread.  _(độ khó 4/5)_

**Ước lượng task lớn:** 5 mã nguồn, 17 điểm.

## Phụ thuộc và điểm bàn giao BE

- Tenant/cư trú là dữ liệu để Student 3 kiểm tra hợp đồng và Student 4 phân bổ điện nước; giữ lịch sử ở ghép.
- Phí sửa chữa Tenant phải chịu được duyệt trước khi Student 5 đưa vào hóa đơn.
- Giữ conventions, test integration và triển khai môi trường dùng chung; không thay các Student khác viết test cho module họ phụ trách.

---

## 7. Checklist kiểm tra khi merge Backend toàn nhóm

- [ ] Tất cả mã `[P1-01]` đến `[P1-138]` có owner, tests và được tích hợp vào luồng chạy thử.
- [ ] Phase 2 và Phase 3 không âm thầm được đưa vào phạm vi bắt buộc; nhóm phê duyệt trước khi bắt đầu.
- [ ] Phân quyền ở mọi API dựa cả role và quyền thực tế trên Branch/hợp đồng; không có truy cập chéo Tenant/Owner.
- [ ] Luồng mẫu: tìm phòng → đặt lịch (có thể bỏ qua) → booking → xác minh Booking Hold đúng môi trường → xác nhận giữ chỗ → hợp đồng → cư trú → dịch vụ → hóa đơn → thanh toán từng phần → lịch sử chi phí và sửa chữa.
- [ ] QR thủ công/Sandbox chỉ dùng Payment đã xác minh; test webhook giả, webhook lặp, giao dịch sai nội dung và tranh chấp slot.
- [ ] Hóa đơn đã phát hành và khoản tiền thực thu không tự thay đổi khi đổi đơn giá hay người quản lý Branch.
- [ ] PostgreSQL, môi trường demo, OpenAPI/tài liệu API và dữ liệu mẫu có thể chạy cho giảng viên kiểm tra.

## 8. Phụ lục: Danh sách business rules chung phải giữ

- [BR-01] KHÔNG sử dụng Brand. OWNER quản lý trực tiếp các Branch, Manager của mình.
- [BR-02] Branch ACTIVE có một Manager; Manager chỉ thuộc một Owner tại một thời điểm, có thể phụ trách nhiều Branch của Owner đó. Branch DRAFT được chưa có Manager.
- [BR-03] Phân quyền dựa cả vào vai trò VÀ quan hệ quản lý/thuê thực tế; không dựa duy nhất vào tên role.
- [BR-04] Guest được tìm/xem phòng công khai; đặt lịch, booking, Thread (Phase 3) cần đăng nhập.
- [BR-05] Không có chức năng tạo bài đăng nhà trọ riêng; danh sách tìm phòng lấy từ dữ liệu quản lý phòng/cơ sở.
- [BR-06] Hỗ trợ cả thuê nguyên phòng và thuê từng chỗ; không cho thuê/giữ trùng tài nguyên ở cùng thời điểm.
- [BR-07] Đặt lịch xem và booking là hai nghiệp vụ độc lập; có thể đặt phòng mà không cần xem trước.
- [BR-08] Booking CONFIRMED chỉ sau khi Payment đã xác minh đủ Booking Hold đúng môi trường; tiền đến muộn không tự hồi sinh Booking.
- [BR-09] Điện/nước luôn sẵn như dịch vụ mặc định; không tiêu thụ thì không tính tiền theo lượng dùng.
- [BR-10] Tenant được yêu cầu đăng ký/hủy dịch vụ TÙY CHỌN, không được hủy dịch vụ điện/nước mặc định.
- [BR-11] Điện/nước mặc định theo ngày cư trú thực tế, tổng tiền đúng phí gốc sau làm tròn; thay đổi cần đề xuất và Owner/Manager duyệt.
- [BR-12] Giá dịch vụ thay đổi không sửa số tiền hóa đơn đã phát hành.
- [BR-13] Hóa đơn có thể thanh toán từng phần; thành viên tự trả hoặc đại diện trả thay và phải theo dõi chính xác công nợ từng người.
- [BR-14] Phase 1: QR thủ công HOẶC VNPay/MoMo Sandbox; không bắt buộc đối soát ngân hàng thật. Payment chưa xác minh không giảm công nợ.
- [BR-15] Ảnh QR nằm trên Branch; không có owner_bank_accounts. Sandbox tách tiền thật; không tạo ví Tenant.
- [BR-16] Tenant được báo hỏng Furniture/Service; mỗi yêu cầu có lịch sử nhiều lần xử lý và ảnh minh chứng.
- [BR-17] CSVC hao mòn/cũ do Owner chịu; lỗi Tenant gây ra chỉ tính vào hóa đơn khi Manager/Owner duyệt.
- [BR-18] Phase 1 hợp đồng CRUD/quản lý trạng thái; Phase 3 mới làm ký OTP mô phỏng và PDF hợp đồng.
- [BR-19] Thread chỉ có ở Phase 3; người chưa đăng nhập không được xem, gợi ý phòng có thể khớp gần đúng.
- [BR-20] Phase 3 ADMIN chỉ ẩn/đóng Thread cơ bản; quy trình báo cáo vi phạm, Comment và Room Suggestion DEFERRED, không giả lập bằng JSONB.

## 9. Các quyết định chưa chốt trong nguồn (không suy diễn)

- Đã chốt D03: một User có nhiều role đồng thời; không hỏi lại.
- Thời hạn thanh toán tiền cọc, thời hạn giữ phòng, mức cọc, điều kiện hủy/hoàn/khấu trừ.
- Đã chốt D22: số ngày cư trú giao với kỳ/segment và làm tròn VND xác định.
- Đã chốt D26: Owner/Manager phát hành; Owner duyệt điều chỉnh sau phát hành, bảo toàn snapshot và audit.
- Đã chốt D25/D29: phân biệt payer/người hưởng; tiền dư/chưa khớp nằm tại Payment. Thời hạn xử lý ngoại lệ cụ thể còn cấu hình.
- Phạm vi phân quyền xác minh Owner và mức độ xác thực yêu cầu trước khi mở quyền vận hành.
- Giới hạn đăng Thread, thời hạn tự đóng, quy tắc kiểm duyệt khi triển khai Phase 3.

**Kết thúc.** File này chỉ dùng để giao nhiệm vụ Backend. Đơn vị phân công là `BE-xx.y`, các mã P1/P2/P3 dùng đối soát 1–1 với `all-feature.txt`.
