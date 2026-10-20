> LEGACY: Documentation for the previous public-schema backend, retained for reference. Commands and implementation descriptions do not apply to refactor/rental-schema. See [current backend setup](../../backend/README.md).

# Invoice draft — Student 5 / Phase 1

Branch: `feat/invoice-draft`. Phạm vi review: tạo, xem chi tiết, sửa, cập nhật nguồn và hủy
**hóa đơn nháp**. Chưa có endpoint phát hành, công nợ, Payment hoặc webhook.

## Quyết định nghiệp vụ đã chốt

- Một Invoice thuộc một Contract. Thuê nguyên phòng có thể có nhiều thành viên;
  mỗi hợp đồng slot có hóa đơn riêng và chỉ nhận phần phí được phân bổ cho nó.
- Kỳ mặc định theo tháng dương lịch. Bản nháp hiện tạo đúng một tháng.
- Khoản thêm tháng kế tiếp lúc nhận phòng là **tiền thuê trả trước**, không phải tiền
  cọc. Khi triển khai thu nhiều tháng phải giữ từng tháng, không lập hóa đơn thu lại.
- Tiền kỳ đầu/cuối chưa trọn tháng theo thỏa thuận hợp đồng. `firstMonthRent` và
  `lastMonthRent` là số tiền đã được chốt, không phải yêu cầu tùy ý từ client Invoice.
  Nếu thiếu, API báo cần bổ sung điều khoản; không tự lấy giá tháng chia cho 30.
- Nếu hợp đồng nằm hoàn toàn trong một tháng, dùng một khoản thống nhất:
  `firstMonthRent` bắt buộc, `lastMonthRent` nếu có phải bằng khoản đó.
- Owner và Manager đúng Branch được thao tác nháp; Tenant không được xem nháp.
- Được tạo nháp thiếu phí. `warnings` phản ánh nguồn thiếu/mơ hồ tại lần đọc nguồn
  gần nhất. Đây không phải chứng nhận đủ điều kiện phát hành.
- Tổng tiền phải **lớn hơn 0**. Từng dòng làm tròn HALF_UP đến đồng VND.
- Mỗi hợp đồng/tháng chỉ có một hóa đơn chưa hủy. Hủy giữ lịch sử và cho lập lại.
- Nguồn thay đổi không tự đổi nháp. Người quản lý chủ động cập nhật từ nguồn.
- Ngày đóng tiền lấy từ `paymentDay` của hợp đồng (1–31); tháng ngắn lấy ngày cuối.
  Hạn mặc định = ngày đóng tiền + 14 ngày. Bản nháp được xóa hạn; hạn mới nằm trong
  khoảng ngày đóng tiền đến tối đa 14 ngày sau đó.
- Phát hành, thu nhiều tháng theo yêu cầu, gia hạn, phạt chậm trả, xử lý báo trước,
  thu hồi/điều chỉnh hóa đơn đã phát hành làm ở lượt riêng sau khi chốt quy tắc.
  Không có thuật toán phạt hoặc cơ chế tự xóa hóa đơn đã nhận tiền trong lượt này.

## Cấu trúc và ranh giới module

- `InvoiceDraftController`: REST/validation, không tính tiền.
- `InvoiceDraftServiceImpl`: transaction, quyền, trạng thái, chống trùng, tính tổng.
- `InvoiceSourceReader`: lấy tiền thuê và phí nguồn; không sửa dữ liệu Student 4.
- `InvoiceAccessPolicy`: UUID từ principal đã xác thực + role + quan hệ Branch.
- `InvoiceMoney`: tính/làm tròn tiền.
- `InvoiceBillingPeriod`: giữ tháng bằng unique(contract_id, billing_month,
  active_marker). Marker TRUE khi còn giữ; null sau hủy. PostgreSQL cho nhiều null
  trong unique constraint, nên giữ được nhiều hóa đơn hủy cho cùng tháng.
- `InvoiceItemAllocation`: snapshot phần phí dịch vụ đã duyệt theo Tenant và
  ContractTenant. **Không phải** bảng công nợ hoặc phân bổ Payment.
- `InvoiceAuditEvent`: ai tạo/sửa/refresh/hủy, thời điểm, tổng trước/sau hoặc lý do.
  Không phải hệ thống lưu toàn bộ phiên bản từng trường.
- `Branch` và `ManagerBranchAssignment` đã khớp schema UUID dùng chung. Manager
  được cấp quyền qua phân công active, chưa có unassignedAt; không dùng branches.manager_id.
- `Contract`, `ContractTenant`: nền dữ liệu tối thiểu được thêm theo
  yêu cầu vì checkout chưa có các module này. Chưa có CRUD/onboarding cho chúng.
  Dùng khóa `id` theo conventions entity hiện tại; nhóm cần hợp nhất với mapping
  Student 1/3/6 trước khi merge các module. `roomId` và `tenantId` là tham chiếu
  UUID; chưa có entity Room/Tenant để thêm FK ở bước này.

Invoice không lấy danh tính từ `X-User-Id`. Student 1 cần cung cấp principal có
`Authentication.getName()` là UUID tài khoản và authority `ROLE_OWNER` hoặc
`ROLE_MANAGER`. Nếu principal sau này dùng email, chỉ thay adapter
`InvoiceAccessPolicy`, không đưa ID tự khai từ client vào nghiệp vụ.

Trạng thái `ISSUED` được định nghĩa để chặn API nháp sửa hóa đơn đã phát hành.
Chưa có thao tác công khai nào chuyển sang trạng thái này.

## Đọc phí và cảnh báo

Nguồn dịch vụ phải thuộc đúng Branch/phòng, có `finalized=true`, đúng một phương
án `APPROVED`, tổng phân bổ bằng phí gốc, mỗi Tenant chỉ xuất hiện một lần và số
tiền phân bổ là số đồng nguyên không âm. Quan hệ cư trú của mỗi người phải xác
định được duy nhất trong kỳ phí. Dữ liệu nguồn sai sẽ được cảnh báo và không cộng
vào nháp.

Với slot, chỉ cộng phần của các thành viên thuộc Contract hiện tại. Dòng SERVICE
có quantity=1, unitPrice=phần phí của hợp đồng; các trường `sourceQuantity`,
`sourceUnitPrice`, `sourceTotalAmount` giữ chỉ số/giá/tổng gốc cả phòng.

Lượt này lấy các kỳ phí nằm hoàn toàn trong tháng hóa đơn. Kỳ đồng hồ vắt qua hai
tháng chưa có quy tắc chốt được xác nhận; không tự chia hoặc tự chuyển kỳ để thu.
Không có charge phù hợp sẽ trả `SERVICE_CHARGE_MISSING:<roomServiceId>`.
Nhiều charge cùng dịch vụ được phép nếu các khoảng ngày không chồng nhau.

Dịch vụ tùy chọn phải có đăng ký APPROVED giao với kỳ; điện/nước và dịch vụ mặc
định luôn được kiểm tra. Module hiện có đổi đăng ký sang CANCELLED mà chưa lưu đủ
lịch sử hiệu lực; trường hợp này trả `SERVICE_REGISTRATION_HISTORY_REVIEW` để
Student 4 bổ sung dữ liệu, không tự suy ra nghĩa vụ lịch sử.

Các mã còn lại: `CONTRACT_MEMBERS_MISSING`, `SERVICE_CONFIGURATION_MISSING`,
`SERVICE_BRANCH_MISMATCH`, `SERVICE_CHARGES_OVERLAP`,
`SERVICE_CHARGE_NOT_FINALIZED`, `SERVICE_ALLOCATION_NOT_UNAMBIGUOUS`,
`SERVICE_ALLOCATION_INVALID`, `SERVICE_ALLOCATION_MEMBERSHIP_AMBIGUOUS`,
`SERVICE_ALLOCATION_TOTAL_OR_MEMBERS_MISMATCH`. Phần sau dấu `:` là ID nguồn
hoặc loại dịch vụ để người quản lý tìm bản ghi cần bổ sung.

## API

| Method | Path | Kết quả |
| --- | --- | --- |
| POST | /api/v1/invoices | 201 + Location, tạo nháp |
| GET | /api/v1/invoices/{id} | 200, chi tiết trong phạm vi quản lý |
| PUT | /api/v1/invoices/{id}/draft | 200, thay toàn bộ trường sửa được |
| POST | /api/v1/invoices/{id}/source-refreshes | 200, cập nhật nguồn |
| POST | /api/v1/invoices/{id}/cancellations | 200, hủy nháp |

Các response thành công dùng `ApiResponse<InvoiceDraftResponse>` với ba trường
`code`, `message`, `result`. `code` trùng HTTP status: tạo trả 201, các thao tác còn
lại trả 200. Dữ liệu hóa đơn nằm trong `result`; frontend đọc `result.id`,
`result.version`, `result.totalAmount` thay cho các trường ở cấp ngoài cùng.
POST tạo vẫn trả header `Location` trỏ đến hóa đơn mới. Response lỗi từ
`InvoiceExceptionHandler` vẫn dùng cấu trúc lỗi được mô tả bên dưới.

Tạo (không nhận totalAmount, giá thuê, Owner ID hoặc trạng thái từ client):

```json
{
  "contractId": "30000000-0000-0000-0000-000000000001",
  "billingMonth": "2026-10-01",
  "note": "Hóa đơn tháng 10"
}
```

Sửa (thay `version` bằng giá trị response mới nhất; mảng rỗng xóa các điều chỉnh):

```json
{
  "version": 0,
  "dueDate": "2026-10-15",
  "note": "Đã kiểm tra phí",
  "adjustments": [
    {"type": "SURCHARGE", "reason": "Khoản thu đã thống nhất", "quantity": 1, "unitPrice": 10000.5},
    {"type": "DISCOUNT", "reason": "Hỗ trợ người thuê", "quantity": 1, "unitPrice": 5000}
  ]
}
```

Cập nhật nguồn: `{"version": 1}`. Hủy: `{"version": 2, "reason": "Lập lại do chọn sai kỳ"}`.
Version có thể tăng sau từng thao tác; không dựa vào các số minh họa để gửi request.

Phụ thu/giảm trừ chỉ nhận số lượng/đơn giá dương; DISCOUNT lưu magnitude dương rồi
trừ trong tổng. Không dùng phụ thu tự nhập để tự động tích hợp phí sửa chữa chưa
được duyệt; kết nối nguồn sửa chữa của Student 6 sẽ làm riêng.

Lỗi có `timestamp, status, error, code, message, details`:
400 dữ liệu/quy tắc; 403 sai quyền; 404 không tìm thấy; 409 trùng kỳ/version cũ.
Lỗi 401 và CSRF 403 do Spring Security xử lý trước controller.

## Database và dữ liệu review

Làm theo [DATABASE_MIGRATION.md](DATABASE_MIGRATION.md): local mới chạy Flyway
V1→V2→V3→V4→V5, Hibernate dùng `ddl-auto:validate`. Schema Invoice local cũ từ 001/002
không còn khớp mapping Branch hiện tại; giữ database cũ và tạo local mới khi
review phiên bản này. Không dùng Hibernate update để đồng bộ.

Các file 001/002 được giữ làm tài liệu/test demo cũ, không dùng để bootstrap
code hiện tại hoặc chạy trên Supabase. Sau Flyway V5, có thể chạy
`docs/sql/006_uuid_local_demo_data.sql` một lần trên database local `sba_uuid_*`:
Branch đầy đủ, phân công Manager, hợp đồng thuê 3.000.000đ và ba Tenant. Phí
điện/nước chưa có sẽ được cảnh báo. Account placeholder trong seed bị LOCKED,
không có mật khẩu đăng nhập thật; demo authentication bên dưới là in-memory.

Auth thật đã dùng `accounts`/UUID và JWT; xem [AUTH_MERGE.md](AUTH_MERGE.md).
Profile **invoice-demo** vẫn cung cấp riêng hai tài khoản review local:
- Owner: `10000000-0000-0000-0000-000000000001`.
- Manager: `10000000-0000-0000-0000-000000000002`.

Đặt `INVOICE_DEMO_OWNER_PASSWORD`, `INVOICE_DEMO_MANAGER_PASSWORD` trong môi trường
hoặc Run Configuration cá nhân, thêm `JWT_SECRET`, bật profile `invoice-demo`, rồi chạy backend.
Không dùng profile demo khi tích hợp Auth thật hoặc deploy môi trường vận hành.
Không bật profile demo thì dùng Bearer JWT từ `/api/auth/login`; tài khoản phải là
Owner đúng chi nhánh hoặc Manager được phân công. JWT không cần CSRF token.

Profile demo dùng UserDetailsService và filter chain riêng, không mở public Invoice hay tắt CSRF.
Có thể đăng nhập form hoặc dùng HTTP Basic. Với POST/PUT JSON trong Postman, trước
tiên GET `/login` bằng cùng thông tin Basic Auth, giữ cookie và lấy giá trị hidden
input `_csrf`; gửi giá trị đó ở header `X-CSRF-TOKEN` cùng cookie trong request ghi.
Swagger GET xem được sau đăng nhập; Try it out ghi cũng cần CSRF token.

## Kiểm thử và tiêu chí review

```powershell
mvn test
```

Test dùng profile test và H2 riêng, không import credential Supabase:
- Tính tổng, làm tròn HALF_UP, từ chối tổng 0/âm, rollback dữ liệu không hợp lệ.
- Chống trùng tháng, hủy nhiều lần qua các bản thay thế vẫn giữ lịch sử.
- Hai luồng tạo đồng thời chỉ một thành công; hai luồng sửa cùng version chỉ một thành công.
- Owner khác, Tenant, Manager đã chuyển Branch, giả header và chưa đăng nhập bị từ chối.
- Nguồn không tự thay đổi; refresh giữ điều chỉnh.
- Phí chưa chốt/chưa duyệt không bị thu; hợp đồng slot chỉ nhận đúng phần phí.
- SQL migration và dữ liệu mẫu chạy được trong H2 PostgreSQL mode.
- Profile demo xác thực được cả Owner/Manager bằng HTTP Basic; token lấy từ trang
  đăng nhập cho phép gọi API ghi, thiếu CSRF hoặc sai mật khẩu bị chặn.
- Swagger và OpenAPI vẫn hoạt động.

H2 không chứng minh mọi hành vi PostgreSQL. Bộ V1–V5 đã được kiểm thử trực tiếp
trên PostgreSQL 17.11, gồm JWT → Invoice và Hibernate validate toàn bộ entity;
xem [biên bản V5](database/migration-v5-validation.md). Vẫn cần diễn tập trên backup
Supabase mới trước khi migration database dùng chung.

Ví dụ A trả 500.000đ, B trả hộ B/C 2.400.000đ, A trả tiếp 700.000đ đã được ghi nhận
là yêu cầu BE-05.2/05.4. Lượt này chưa tạo Payment hoặc ghi giảm nợ; không đánh dấu
ví dụ đó đã hoàn thành chỉ vì có dữ liệu Invoice.
