# Demo Manager TroPro

## Chạy frontend

Trong `fe-sba-project`, dùng Node/npm sẵn có:

```powershell
npm.cmd install
npm.cmd run dev
```

Mở `http://localhost:5173`. Nếu server đã chạy thì dùng server hiện tại. `.env.example` mô tả provider; mặc định không cấu hình là mock. Không cần chạy BE để trải nghiệm toàn bộ Manager demo.

```dotenv
VITE_API_MODE=mock
```

Tất cả tài khoản dưới đây là hư cấu, mật khẩu demo **TroPro@123**:

| Email | Quyền và dữ liệu |
| --- | --- |
| manager@tropro.demo | Manager được phân công TroPro Thủ Đức và TroPro Làng Đại học; không được vào Bình Thạnh. Có nút điền nhanh tại trang đăng nhập. |
| owner@tropro.demo | Chủ sở hữu Thủ Đức/Làng Đại học, có thể dùng cùng thao tác vận hành tại `/manager`; không xây Owner workspace riêng. |
| manager-other@tropro.demo | Manager Bình Thạnh; không được xem hai Branch của Manager chính. |
| an@tropro.demo | USER có lịch xem và yêu cầu giữ phòng. |
| tenant@tropro.demo | Tenant nguyên phòng Thủ Đức, SLOT Làng Đại học và quan hệ thuê đã kết thúc. |
| roommate@tropro.demo | Người ở cùng hợp đồng SLOT; nghĩa vụ/hóa đơn riêng. |

Sau đăng nhập Manager, chọn **Quản lý chi nhánh** ở header/menu. Branch selector chỉ có chi nhánh được phân công. Trên điện thoại, dùng danh sách điều hướng ngay dưới Branch selector.

Mock dùng sessionStorage. Để xem cùng dữ liệu liên vai trò, **đăng xuất rồi đăng nhập tài khoản khác trong cùng tab**. Tab/context trình duyệt riêng không bảo đảm chia sẻ kho giả lập. Refresh cùng tab vẫn giữ dữ liệu; đóng phiên/tab mới có thể tạo kho mới. Không dùng dữ liệu cá nhân thật trong demo.

## Luồng nên kiểm tra

1. **Quyền Branch:** đăng nhập Manager chính, đổi Thủ Đức ↔ Làng Đại học; tên, phòng, người thuê, số liệu thay đổi. Mở link Branch Bình Thạnh trực tiếp: bị từ chối. Mở Công cụ kịch bản Manager demo → mô phỏng chủ nhà thu hồi phân công: nội dung Branch bị ẩn sau lần đồng bộ quyền; API từ chối ngay. Khôi phục dữ liệu liên kết để lấy lại phân công, không phải đăng nhập lại.
2. **Phòng/chỗ:** tạo phòng nội bộ với RoomType cùng Branch, xem trước rồi lưu; phòng không xuất hiện trên public. Sửa tên/công khai, thử trùng mã. Với phòng SLOT, xem chỗ và khoảng ngày, thử thêm vượt sức chứa hoặc ngừng chỗ có người ở. Giá tham khảo không thay giá ContractTenant.
3. **Lịch xem:** USER tạo lịch tại phòng trống; chuyển Manager vào Lịch xem, xác nhận/từ chối có lý do; chuyển USER xem kết quả và thông báo. Lịch chồng lấn bị chặn.
4. **Giữ phòng → hợp đồng:** chọn Làng Đại học, mở giữ phòng CONFIRMED của USER tại phòng SLOT chưa có tenancy (fixture Booking 305). Tạo hợp đồng với mã/điều khoản/giá kỳ đầu/cuối đã thỏa thuận. Hợp đồng mới DRAFT, hold vẫn theo dõi riêng. Chuyển `an@tropro.demo` → Không gian thuê → Hợp đồng → Điều khoản cá nhân; thanh toán sandbox kỳ đầu/kỳ cuối. Đợi VERIFIED; chuyển Manager → hợp đồng → kích hoạt. Không tự xác minh tiền bằng nút Manager, không chuyển hold thành prepaid.
5. **Dịch vụ:** sau một lần đăng nhập Manager, chuyển Tenant đăng ký Internet riêng hoặc dọn phòng. Yêu cầu chờ Manager xử lý, không tự duyệt. Chuyển Manager → Yêu cầu dịch vụ → chấp thuận/từ chối; Tenant thấy kết quả. Điện/nước không thể yêu cầu ngừng.
6. **Điện nước:** chọn Làng Đại học, phòng ở ghép có Tenant. Tạo khoản phí trong kỳ chưa có khoản đã chốt; chọn dịch vụ và công tơ đúng phòng. Nhập từng đoạn ngày/chỉ số/đơn giá, xem trước số tiền, lưu nháp; sửa nháp nếu cần rồi chốt. Tách đoạn reset đầu 0 hoặc chọn công tơ thay thế. Phân bổ Tenant cộng đúng tổng phòng, đọc từng đoạn và ảnh minh chứng. Khoản đã chốt không sửa trực tiếp.
7. **Phân bổ:** Tenant vào Đề xuất phân bổ, gửi tổng hợp lệ; Manager so sánh mặc định/đề xuất rồi quyết định. Hóa đơn đã phát hành giữ nguyên; đề xuất sai tổng/người tham gia bị chặn.
8. **Hóa đơn:** Manager chọn từng ContractTenant/kỳ chưa có invoice chưa hủy, kiểm tra tiền thuê được duyệt và nguồn đã chốt, xem trước, lưu nháp, chỉnh sửa, phát hành. Đúng Tenant nhận invoice/thông báo, người cùng phòng không xem hóa đơn của nhau. Manager không sửa/hủy tiền sau phát hành.
9. **Trả trước áp dụng:** Thủ Đức có nháp mẫu kỳ sau. Hủy nháp mẫu rồi lập mới cho Tenant nguyên phòng cùng kỳ; fixture cho phép dùng FIRST từ nguồn TP-PREPAID-01 còn lại, tối đa 3 triệu trong kỳ đó. Phát hành giảm nợ từ nguồn cũ, không tạo receipt mới. Quan hệ mới không có hạn mức lịch tự động; chính sách Q1–Q5 chưa được chốt.
10. **Thanh toán một phần:** Tenant thanh toán sandbox một phần hóa đơn; PENDING không giảm nợ, VERIFIED chỉ giảm đúng hóa đơn. Manager xem sandbox riêng. Với biên nhận ngân hàng giả lập tại Làng Đại học: tham chiếu **TP-QR-DEMO-01**, số tiền **50.000**, provider ID **BANK-DEMO-0001**. Đối chiếu và nhập ghi chú để xác minh; sai một trường bị từ chối, xác minh lần hai bị chặn. Đây không phải biên nhận ngân hàng thật.
11. **Sửa chữa:** Tenant báo hỏng tài sản/dịch vụ của chỗ mình; Manager tiếp nhận, phân công, bắt đầu, thêm nhiều nhật ký/ảnh và chi phí. Chi phí người thuê luôn chờ duyệt trước; duyệt trách nhiệm, hoàn tất yêu cầu rồi mới chọn khoản đó làm nguồn invoice. Tenant xem cùng timeline. Tài sản có lịch sử sửa chữa không bị xóa mất.
12. **Bảo hành/Zalo/báo cáo:** mở tài sản để thêm/sửa bảo hành; không xóa bảo hành đã có hiệu lực. Đặt nhóm Zalo HTTPS hợp lệ tại Thủ Đức, Tenant Branch đó thấy nhóm, Branch khác không bị đổi. Báo cáo tách số phát hành, công nợ, thu đã xác minh, sandbox và hold; phòng vật lý/SLOT có số riêng. Thông báo dùng hộp thư sẵn có, đi tới record và Branch đúng quyền.

## Đặt lại dữ liệu

DEV + Manager demo có công cụ **Khôi phục dữ liệu liên kết** cuối workspace. Công cụ khôi phục inventory, lịch xem/giữ phòng, contracts/tenancies, services, charges, invoices, payments, reports và phân công; giữ phiên và tài khoản đã đăng ký. Tenant có công cụ reset sẵn có, nay khôi phục cùng kho liên kết để không để lại Contract/Invoice mồ côi.

Đăng nhập Manager đưa các quyết định service/allocation/maintenance vào chế độ xử lý thủ công chung. Bộ thanh toán sandbox vẫn tự xử lý. Tenant demo độc lập trước khi vào Manager giữ luồng tự động trước đây; sau reset Tenant, đăng nhập Manager lại nếu muốn kiểm tra duyệt thủ công. Đây là cơ chế demo, không phải tự động duyệt nghiệp vụ ở production.

## Hybrid / Real

```dotenv
VITE_API_MODE=hybrid
VITE_API_BASE_URL=
VITE_BACKEND_PROXY_TARGET=http://localhost:8080
VITE_MANAGER_PROVIDER=mock
VITE_CATALOG_PROVIDER=mock
VITE_LEASING_PROVIDER=mock
VITE_TENANT_PROVIDER=mock
```

Đổi `.env.local` cần khởi động lại Vite. Hybrid dùng auth/thông báo BE hiện có; không biến tài khoản demo thành tài khoản thật và không cấp Branch cho tài khoản BE chỉ dựa trên role. Tài khoản BE chưa có assignment trong kho mock sẽ thấy không có Branch, đúng nguyên tắc từ chối mặc định. Dùng mock để chạy trọn bộ các kịch bản seed ở trên.

`VITE_MANAGER_PROVIDER=real` chỉ dùng khi BE đã cung cấp các endpoint/DTO/guards bàn giao trong [manager-api-contract.md](manager-api-contract.md). API còn thiếu trả lỗi hiển thị; không có fallback giả thành công. Chưa xác nhận ghi dữ liệu Manager trên BE/ngân hàng thật.

## Kiểm tra tự động

```powershell
npm.cmd run typecheck
npm.cmd run lint
npm.cmd test
npm.cmd run build
npm.cmd run test:e2e -- --workers=2
```

Playwright dùng Edge, desktop 1440×1000 và iPhone 13 qua Chromium; có screenshot và trace khi lỗi. Đừng sửa module app trong lúc chạy suite để tránh Vite tự reload làm gián đoạn ca kiểm thử.
