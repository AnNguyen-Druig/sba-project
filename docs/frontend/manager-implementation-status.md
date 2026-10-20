# Trạng thái triển khai Manager TroPro

Triển khai M01–M29 trong frontend local của nhánh `feature/fe-rebuild`, giữ Guest/Auth/User/Tenant. Không đổi nhánh, commit/push, sửa source BE hay database. Primary skill thực dùng: **minimalist-ui**; bổ trợ **api-and-interface-design**. Be Vietnam Pro, teal #0F766E, Lucide, Tailwind và các thành phần TroPro hiện có được giữ làm chuẩn.

## Các nhóm màn hình

Các route dưới `/manager` dùng `?branchId=<uuid>`; bỏ query sẽ lấy Branch được phép đã chọn hoặc Branch mặc định. Deep link không thuộc phân công bị từ chối.

| Nhóm | Route / tích hợp | Chức năng thực thi |
| --- | --- | --- |
| M01 Dashboard | `/manager/dashboard` | 6 KPI, work queue, biểu đồ hóa đơn/thu theo kỳ, trạng thái phòng và số SLOT riêng. |
| M02 Phòng | `/manager/rooms` | Tìm kiếm, loại, mode, vận hành, khả dụng, min/max giá, sức chứa, khoảng ngày; bảng desktop/card mobile. |
| M03 Tạo/sửa phòng | `/manager/rooms/new`, `/:roomId/edit`, `/:roomId` | Validate, ảnh, tiện nghi, preview, lưu, publication, vận hành, safe delete; liên kết chỗ/người thuê/dịch vụ. |
| M04 Loại phòng | `/manager/room-types` | List/search/create/edit/deactivate/safe delete theo Branch. |
| M05 Chỗ ở | `/manager/rooms/:roomId/slots` | Tạo/sửa chỗ đúng parent SLOT, availability theo ngày, lịch sử cư trú, bảo vệ sức chứa/chỗ đang thuê. |
| M06 Người thuê | `/manager/tenants` | Tìm kiếm, trạng thái, phòng/chỗ/khoảng cư trú; nhận người mới qua contract. |
| M07 Quan hệ thuê | `/manager/tenancies/:contractTenantId` | Canonical account, vai trò, kỳ/giá cá nhân, trả trước, lịch sử, invoices/services, link contract/room/slot. |
| M08 Lịch xem | `/manager/appointments`, `/:appointmentId` | Người yêu cầu/liên hệ, phòng/thời gian/status filter, quyết định xác nhận/từ chối/hủy/hoàn tất, lịch sử và notification USER. |
| M09 Giữ phòng | `/manager/bookings` | Danh sách scoped, status/phòng/date/search, khoản giữ và môi trường thanh toán. |
| M10 Chi tiết giữ | `/manager/bookings/:bookingId` | Hold verification/state/expiry/conflict guards, giao dịch liên quan, chuyển thành contract đúng một lần. |
| M11 Hợp đồng | `/manager/contracts` | Tìm kiếm/trạng thái, đại diện, số thành viên, kỳ và ROOM/SLOT. |
| M12 Editor hợp đồng | `/manager/contracts/new`, `/:contractId`, `/:contractId/edit` | Draft create/edit, activate đủ đại diện/prepaid, cancel/end có điều kiện, lịch sử, source Booking. |
| M13 Nghĩa vụ cá nhân | `/manager/contracts/:contractId/tenants` | Chọn account ACTIVE đủ điều kiện, role/slot/giá/kỳ/prepaid/terms; sửa nháp, đổi đại diện, kích hoạt thành viên. |
| M14 Danh mục dịch vụ | `/manager/services` | Catalog theo Branch, BillingMethod/ServiceType, bắt buộc/tùy chọn, mã/giá/đơn vị, activation guards. |
| M15 Dịch vụ phòng | `/manager/room-services` | Gán service và giai đoạn giá; update tạo giai đoạn mới, giữ lịch sử. |
| M16 Yêu cầu dịch vụ | `/manager/service-requests` | Queue đăng ký/ngừng, xem lịch sử, approve/reject, cập nhật cùng subscription Tenant. |
| M17 Chi phí | `/manager/utility-charges`, `/new`, `/:chargeId`, `/:chargeId/edit` | Công tơ/không đo, nhiều đoạn, reset/thay meter/đổi giá, ảnh, preview thực, draft edit, finalize, phân bổ chính xác theo từng đoạn. |
| M18 Phân bổ | `/manager/cost-allocations` | So sánh mặc định/đề xuất, tổng nguồn, xét duyệt/từ chối, link charge/invoice, bảo toàn ISSUED. |
| M19 Hóa đơn | `/manager/invoices` | Room/Tenant/month/document/finance/search filters; nghĩa vụ cá nhân, nợ, trạng thái tách biệt. |
| M20 Lập/phát hành | `/manager/invoices/new`, `/:invoiceId`, `/:invoiceId/edit` | Rent đã duyệt, nguồn phí/link nguồn, adjustment có căn cứ, prepaid hợp lệ, preview/save/edit/issue/cancel draft; bảo vệ số tiền đã phát hành. |
| M21 Đối soát | `/manager/payments`, `/:paymentId` | Tìm/lọc target/status/environment/room/Tenant/date/month, bằng chứng, note/audit, eligible manual QR verify/review; sandbox chỉ trusted provider. |
| M22 Công nợ | `/manager/debts` | Nợ theo từng ContractTenant/invoice, filter phòng/người/tháng/quá hạn, link nghĩa vụ/chi tiết. |
| M23 Furniture | `/manager/furnitures`, `/:furnitureId` | CRUD, phòng/chỗ/code/serial/value/condition, maintenance history, chống xóa hồ sơ tham chiếu. |
| M24 Bảo hành | Section trong Furniture Detail | Add/edit/view, thời hạn/điều kiện; chỉ xóa khi chưa hiệu lực. |
| M25 Queue sửa chữa | `/manager/maintenance` | Search/status; hiển thị priority/room/requester/handler, liên kết yêu cầu Tenant. |
| M26 Xử lý sửa chữa | `/manager/maintenance/:reportId` | Accept/reject/assign/start, nhiều ReportRecords và ảnh/cost, cost decision, complete; timeline và nguồn repair có điều kiện. |
| M27 Zalo | `/manager/settings/zalo` | URL hợp lệ/bật tắt riêng Branch; chỉ Tenant đủ quyền thấy nhóm. |
| M28 Báo cáo | `/manager/reports` | Aggregates cùng kho gốc; thời kỳ, series, trạng thái booking/maintenance/contracts, số ROOM/SLOT riêng; không chart hardcode. |
| M29 Thông báo/hồ sơ | `/notifications`, `/profile` dùng chung | Notification Manager có branchId, điều hướng đúng record/scope; reuse Profile/session/logout. |

## Kiến trúc và dữ liệu chung

- `src/interfaces/manager.ts`, `src/services/manager.ts`: Zod DTO và typed provider; giữ ranh giới HTTP/envelope/error cùng dự án.
- `src/features/manager/`: các module property/catalog/leasing/operations/finance/dashboard; Query keys gồm user + Branch + resource/filter, mutation invalidates dữ liệu chung. Generic forms dùng React Hook Form + Zod, không thêm thư viện.
- `src/layouts/manager-layout.tsx`, `src/router.tsx`: menu nhóm, Branch selector, mobile navigation, lazy routes, pending mutation không cho đổi Branch.
- `src/mocks/inventory.ts`: inventory cùng nguồn public/Manager; RoomType đúng Branch, migration bổ sung type tương ứng cho inventory demo cũ mà không xóa bản ghi người dùng.
- `manager-engine.ts`, `manager-leasing.ts`, `manager-finance.ts`, `manager-operations.ts`, `manager-handler.ts`, `manager-fixtures.ts`: guards ở API, transactions/rollback toàn bộ kho, persistence errors có ý nghĩa, fixtures và transitions thực.
- Existing `MockDatabase`/`TenantEngine`: dùng chung Booking, ContractTenant, service, utility projections, invoices, receipts, reports và notification; không tạo các bản sao độc lập cho từng role.
- Tiền thuê trả trước cho DRAFT được nối ở My Terms bằng `prepaid-panel.tsx`; sandbox xử lý receipt có verifiedAt, activation giữ guard. Không tự chuyển hold thành prepaid.
- DRAFT tenancy đã gán tài nguyên giữ chỗ trong public/Manager/slot reads; sau conversion, ContractTenant là nguồn allocation, booking trở thành lịch sử, không kéo dài giữ chỗ sau kết thúc contract.
- POST/PATCH/DELETE Manager không chỉ toast: thay đổi kho thật của demo, validate scope/state/số tiền, rollback nếu lỗi. Chống idempotency/provider transaction trùng và không xác minh nghĩa vụ chính mình.
- Shared Header vào workspace theo assignment thực; notification links mang tenancyId/branchId; 401 từ request phiên cũ không xóa phiên mới khi đổi tài khoản. Logout tránh điều hướng ngược vào route bảo vệ trong lúc clear session.

Reuse: Container, Page/Panel, QueryState, Field, InlineError, Notice, Modal/Radix focus trap, Button, StatusBadge, Pagination, Facts, date/currency helpers, validated attachments, auth Zustand, QueryClient, HTTP adapters, MSW, Tenant finance helpers và inbox/profile sẵn có. Không dựng design system cạnh tranh hay redesign Guest/User/Tenant.

## Real / Mock và giới hạn

**Manager domain hiện là mock có contract rõ ràng.** Auth/notifications thật vẫn qua adapter hiện có; không tuyên bố nối thành công toàn bộ BE Manager. Real provider thiếu endpoint trả lỗi, không fallback. Hybrid không tự cấp assignment cho tài khoản BE từ role header. Chi tiết phần BE còn thiếu nằm trong [manager-api-contract.md](manager-api-contract.md).

Không có quyết định mới về Q1–Q5. Dữ liệu rent/prepaid theo snapshot hoặc số thỏa thuận có căn cứ; hạn mức áp dụng tháng là dữ liệu đã duyệt của fixture. Hạn ngày 05 chỉ là gợi ý demo, chưa thay BranchPolicy BE. Nháp mẫu kỳ sau có các phí sao chép từ ví dụ cũ, cần chỉnh và kiểm tra lại nguồn trước issue; phí đã dùng không được lập lại.

Các hợp đồng ACTIVE có sẵn là hồ sơ nhập được duyệt từ Tenant baseline; dữ liệu minh họa không tái dựng toàn bộ sổ thu lịch sử. Giao dịch LIVE mẫu cũng là biên nhận giả lập, không phải giao dịch ngân hàng thật. Dữ liệu demo lưu sessionStorage, cùng tab là đơn vị chia sẻ liên vai trò; không phải multi-client persistence hay authorization production.

Không phát triển chuyển/rời phòng sớm, phụ lục/gia hạn, điều chỉnh invoice ISSUED đầy đủ, OTP/e-signature, import meter hàng loạt, export/analytics nâng cao, Owner/Admin workspace. Owner chỉ có khả năng vận hành cùng services trên Branch của mình như yêu cầu S15.

Production build có cảnh báo chunk MSW/mock khoảng 519 kB (minified); đây là chunk tải động khi có provider mock. Các module Manager đã lazy load. Chưa tối ưu riêng thư viện MSW vì không ảnh hưởng tính đúng nghiệp vụ và không cần thay stack.

## Kiểm chứng

- `npm.cmd run typecheck`: PASS.
- `npm.cmd run lint`: PASS, không lỗi/cảnh báo ESLint.
- `npm.cmd test`: **171/171 PASS**, 6 test files — 89 baseline và 82 kiểm thử Manager/provider bổ sung.
- `npm.cmd run build`: PASS; cảnh báo kích thước mock chunk như trên.
- `npm.cmd run test:e2e -- --workers=3`: **76/76 PASS** — 52 regression Guest/User/Tenant và 24 Manager, desktop + mobile.
- Sau các chỉnh sửa cuối: **4/4 focused E2E PASS**, gồm conversion/prepaid/activation và toàn bộ Manager groups trên desktop/mobile; không tràn viewport/form và không có pageerror.
- Browser: Edge/Chromium desktop 1440×1000 và iPhone 13; kiểm tra scrollWidth theo viewport cấu hình, từng form không tràn, dialogs/điều hướng/deep links/refresh; thu pageerror, xem screenshot thực tế. In-app browser không có runtime khả dụng, dùng Playwright của dự án.
- API/provider tests xác nhận hybrid routing, real 404 không fallback giả, malformed DTO fail, delayed old-session 401 giữ phiên mới.
- Domain tests bao phủ permission/revocation/Owner scope; room/slot/identity conflicts; verified hold và exactly-once conversion; representative/prepaid guards; service approvals; meter reset/replacement/periods; exact allocation; nguồn và issuance; partial/duplicate/mismatched payments; sandbox reporting; assets/warranty/report records/cost approvals; Zalo; atomic rollback/reset.

Hướng dẫn QA/account/Branch và reset: [manager-demo-guide.md](manager-demo-guide.md). API/DTO/transition/validation và việc cần BE: [manager-api-contract.md](manager-api-contract.md).

Ảnh đã xem và lưu từ trình duyệt thật: [Dashboard desktop](manager-screenshots/dashboard-desktop.png), [Hóa đơn mobile](manager-screenshots/invoices-mobile.png).
