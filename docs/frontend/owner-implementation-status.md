# TroPro Owner — implementation status

Cập nhật sau pha ADMIN: review UI thật trên frontend/shared mock hiện đã có tại `/admin/owner-verifications`, thay cho harness-only ghi ở báo cáo Owner ban đầu dưới đây. Xem [trạng thái ADMIN](admin-implementation-status.md); Backend verification APIs vẫn là phần bàn giao.

Ngày: 16/10/2026. Branch làm việc: `feature/fe-rebuild`. Không commit/push, đổi branch, sửa BE hoặc schema.

**COMPLETED — O01–O20 trong phạm vi frontend/mock đã duyệt.** Các phần production BE và nghiệp vụ được yêu cầu hoãn nằm ở mục giới hạn, không được báo thành tích hợp thật.

## Phạm vi O01–O20

| Mã | Màn hình / hành vi | Route | Tích hợp |
| --- | --- | --- | --- |
| O01 | Onboarding từ User verified, chọn tài liệu, kiểm tra trước gửi, PENDING, không self-grant | `/owner/onboarding`; public entry `/owner-onboarding` | Auth/Profile chung; Owner mock |
| O02 | NOT_SUBMITTED/PENDING/APPROVED/REJECTED, metadata, kết quả, resubmit, eligibility độc lập role | `/owner/verification` | Owner mock; trusted ADMIN harness chỉ unit tests |
| O03 | Tổng quan owned/all Branch, trạng thái, thu/nợ, approvals, assignment issues, ROOM/SLOT riêng, feed | `/owner/dashboard` | Aggregation từ cùng Manager/Tenant/Booking sources |
| O04 | Branch list, q/status/Manager/scope, pagination, detail/edit/operations | `/owner/branches` | Owner mock |
| O05 | Create/edit code/name/address/province/district/ward/coordinates/description/rules/contact/images/public/status/Manager; xác nhận trước ghi | `/owner/branches/new`, `/owner/branches/:id/edit` | Validation, optimistic version, safe state guards |
| O06 | Branch detail, ảnh, policy/QR/Zalo, rooms, finance, operations/assignment/config links | `/owner/branches/:id` | Owner projection + reused operational modules |
| O07 | Current/history, preview new version, effective sequencing, immutable snapshots | `/owner/branches/:id/policies` | Shared policy resolver cho Booking/Appointment mới |
| O08 | OwnerManager + canonical USERS, contact/status/link date/assignments/search | `/owner/managers` | Không duplicate Manager profile |
| O09 | Invite existing/new email, proposed assignments, pending/expiry/resend/cancel; no password takeover | `/owner/managers/invite`; acceptance `/manager-invitations` | Người nhận verified tự xác nhận; Owner phân công riêng |
| O10 | Assign/reassign/revoke khi hợp lệ, reason/confirmation/version, primary Manager invariant | `/owner/manager-assignments` | Cập nhật trực tiếp cùng Branch assignments Manager |
| O11 | Profile projection, current relationship, Branches, dates, transfer/remove relation safe, activity link | `/owner/managers/:id` | Không khóa global USER |
| O12 | Upload/replace/preview Branch QR; actual image validation, version | `/owner/branches/:id/payments/settings` | qrPublicId/qrUrl; mock resource, không bank integration |
| O13 | Period/scope, issued vs actual receipts/debt/overdue; hold/prepaid/applied/unused/unmatched/sandbox riêng | `/owner/finance` | BigInt/shared domain aggregates |
| O14 | Queue/detail/before-after/reason/evidence/history; approve/reject/stale guards | `/owner/approvals/invoices` | Decision mock; execution NOT_APPLIED; issued Invoice immutable |
| O15 | Payments filters, amounts/allocations/environment/status, linked sources, evidence/history and trusted review | `/owner/payments` → shared payment detail | Reuses Manager reconciliation; Tenant QR claim remains PENDING |
| O16 | Rooms/types/slots/tenant/leasing/contract/services/utilities/allocation/invoices/payments/debt/assets/warranty/maintenance/Zalo | `/manager/*?branchId=:id&workspace=owner` | Same pages/services/engines; explicit Owner resource authorization |
| O17 | Phase 1 Branch comparison, issued/received, ROOM/SLOT counts, bookings/contracts/maintenance/assignment coverage | `/owner/reports` | Same summary service; không speculative revenue/exports |
| O18 | Public visibility/images/description/rules/contact; public preview uses same data | Branch editor/detail → `/branches/:id` | Shared inventory + public Branch provider; hidden Branch cannot be booked |
| O19 | Basic safe activity feed, actor/scope/time/source, filters | `/owner/activity` | Owner mutations + system summaries từ shared operations |
| O20 | Verification/assignment/invitation/approval/payment/overdue/maintenance links and account/profile | `/notifications`, `/profile` | Same inbox/profile/auth; no Owner clones |

## Kiến trúc

- `interfaces/owner.ts`: Zod DTO/input contracts. BranchPolicy/User/Branch/Manager types được tái dùng; fields Owner bổ sung có chủ đích.
- `services/owner.ts`: provider-aware boundary, typed reads, proposed writes; real lỗi không fallback mock.
- `features/owner/context.ts`: query keys scoped user/Owner/Branch/filter, client Branch preference ở Zustand, mutation invalidation.
- `layouts/owner-layout.tsx`: eligibility gate, grouped sidebar, mobile navigation, all-owned selector, operations entry.
- `features/owner/{onboarding,branch,manager,finance}-pages.tsx`: giao diện chuyên trách Owner.
- `mocks/owner-engine.ts`: verification/branches/relationships/invites/policies/QR/approvals/summary/activity, canonical authorization. State nằm trong `ManagerState.owner`.
- `mocks/owner-handler.ts`: HTTP authorization, pagination/filter boundary. Auth server-style canonical actor được lấy từ mock session hiện có.
- `features/manager/adjustment-request.tsx`: thêm request form vào issued invoice detail, không duplicate invoice CRUD.
- `features/tenant/manual-payment.tsx`: đọc QR mới, gửi fake receipt qua cùng Tenant payment domain; không thay sandbox flow.

Focused changes vào code chung: thêm DRAFT Branch; Owner eligibility callback; shared public metadata/visibility/policy/QR binding; optional manualQr DTO; role-context header/query key; status-safe notification references; receipt history cho hold review; clone demo User seed để không rò thay đổi role giữa các independent engines.

Không thêm dependency/framework, Bank/Brand/Wallet bảng hay global Owner store chứa mọi server data. Teal #0F766E, Be Vietnam Pro, Tailwind, Lucide và shared components giữ nguyên. Primary visual skill thực dùng: `minimalist-ui`; bổ trợ contracts: `api-and-interface-design`.

## Các tình huống liên vai trò

| Scenario | Bằng chứng thực thi |
| --- | --- |
| S01 User onboarding → trusted review → entitlement | Unit: reject/resubmit, no self approval, no forged ADMIN, approved without role remains denied, explicit trusted grant |
| S02 Draft → policy → Manager → ACTIVE | Unit + E2E forms desktop/mobile |
| S03 Invitation inactive → actual verified acceptance | Unit/API/E2E; không duplicate USERS, proposed assignments chưa có access |
| S04 One Manager nhiều owned Branch | Unit + initial assigned Branch source chung |
| S05 Reassignment | Unit/API/E2E: old Manager 403, new Manager 200 |
| S06 Versioned policy | Unit future activation/new Booking snapshot; E2E new version và previous snapshots unchanged |
| S07 QR | Unit/API/E2E Owner upload → Tenant instruction; no receipt created by QR write |
| S08 Owner direct operations | Unit/API/E2E reused Manager pages + header scope, foreign deny, path back |
| S09 Verified payment | Unit/E2E: Tenant PENDING claim → Manager verifies → Owner actual receipts + Tenant debt update |
| S10 Adjustment approval | Unit/API/E2E: Manager request → Owner decision; original Invoice byte-for-byte unchanged |
| S11 Financial aggregation | Unit typed source sums, selected Branch vs all, actual/sandbox/hold/prepaid separation; E2E filter |
| S12 Owner isolation | Unit/API/E2E Owner A/B branches/Manager/payment/approval deny |
| S13 Revocation | Unit/API/E2E assignment transfer; existing Manager permission/cache regression suite |
| S14 Continuity | Existing Manager E2E User booking → draft ContractTenant → Tenant prepaid → activation; Owner summary uses same contracts/bookings/invoices. Existing Guest/User/Tenant tests retained |

Seed có ba Branch tổng cộng: Owner Hà sở hữu Thủ Đức và Làng Đại học; Owner Phúc sở hữu Bình Thạnh. Aggregate không lấy cả ba vào một Owner. Có thể tạo thêm Branch C dưới cùng Owner bằng workflow được phép; không gộp Branch của Owner khác để làm đẹp báo cáo.

## Validation

| Check | Kết quả |
| --- | --- |
| `npm.cmd run typecheck` | PASS |
| `npm.cmd run lint` | PASS |
| `npm.cmd test -- --reporter=dot` | PASS — 242/242, 9 files (171 baseline + 71 Owner/domain/provider tests) |
| `npm.cmd run build` | PASS; cảnh báo chunk mock/MSW lazy ~548 kB; các page chunk Owner ~6–17 kB |
| Full Playwright desktop/mobile | PASS — 98/98 scenarios, gồm 76 baseline + 22 Owner |
| Owner final rerun | PASS — 22/22 sau bổ sung room totals và assignment audit; thêm 4/4 summary/scope/screen checks sau sửa unavailable slots |
| Visual review | PASS — đọc ảnh desktop dashboard và mobile finance viewport; all-screen checks xác nhận không lỗi JavaScript/tràn ngang trên cả hai viewport |

Kiểm tra tập trung cuối cho summary/scope/screens được chạy sau khi sửa availability: chỗ trong phòng/chi nhánh ngừng hoạt động được tách khỏi “trống” và hiển thị “không phục vụ”. Unit xác nhận tổng các nhóm SLOT bằng tổng tài nguyên, đồng thời toàn bộ 242 unit/API tests đã qua trên mã cuối.

Screenshots: `docs/frontend/owner-screenshots/dashboard-desktop.png`, `finance-mobile.png`, `finance-mobile-viewport.png`. Browser plugin được thử nhưng trả unavailable; sử dụng Edge Playwright có sẵn, không tuyên bố đã dùng in-app browser thành công.

## Giới hạn còn lại

1. BE Owner endpoints, secure asset upload, trusted invitation delivery/identity provisioning và administrative review chưa có; toàn bộ domain Owner chạy Mock, real missing endpoints hiển thị lỗi.
2. Verification production chưa triển khai eKYC. Demo không giữ raw identity documents; chỉ thử bằng ảnh giả lập. Không có Owner tự duyệt.
3. Approval là quyết định; correction/replacement execution, Q1–Q5, refunds và accounting revenue còn chờ chính sách/API được duyệt.
4. QR/receipt demo không xác minh ngân hàng thật. Chứng cứ Tenant khai cần Manager đối chiếu độc lập; trusted mock decision không phải banking integration.
5. Activity là basic summary, seed records có thời điểm ingest của phiên; không full historical audit search. Advanced analytics, export, multisession synchronization chưa triển khai.
6. Mock state theo session/tab, tối đa dung lượng sessionStorage; storage failure rollback atomic và báo lỗi. Demo liên vai trò bằng đổi tài khoản trong cùng tab, không tuyên bố concurrent production service.
7. Branch deactivate giữ fail-closed khi chưa xử lý nghĩa vụ/hold/unused prepaid/unbilled charges; không dựng refund/settlement policy để bỏ qua.

Hướng dẫn dùng: [owner-demo-guide.md](owner-demo-guide.md). Hợp đồng BE cần bổ sung: [owner-api-contract.md](owner-api-contract.md).
