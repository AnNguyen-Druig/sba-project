# TroPro Admin — trạng thái triển khai

Ngày 16/10/2026, branch `feature/fe-rebuild`. **A01–A13 đã triển khai trong phạm vi frontend/shared mock và bốn User API real có sẵn.** Đây không phải tuyên bố BE production đã hoàn tất. A14 Thread Moderation hoãn Phase3, không có menu giả.

Workspace sạch tại lúc bắt đầu Admin; giữ code local trước đó. Không commit/push, đổi branch, chỉnh Java/schema hoặc thêm dependency. D41–D50 được áp dụng; tài chính và vận hành vẫn theo scope Owner/Manager/Tenant.

## Màn hình và hành vi

| Mã | Route | Đã thực hiện | Provider |
| --- | --- | --- | --- |
| A01 | `/admin/dashboard` | KPIs tài khoản, hồ sơ chờ, Owners đủ điều kiện, Branch status, việc cần xử lý, safe recent activity; không doanh thu/debt | Admin mock |
| A02 | `/admin/users` | Phân trang/page size, mock global q/status/role/date, URL state, desktop table/mobile cards + filter drawer | Real User API hoặc mock; không giả lọc global khi real chỉ hỗ trợ paging |
| A03 | `/admin/users/:userId` | Safe identity, account/contact/verification/roles/scope nếu có nguồn, copy ID, related activity, links | User detail real; extras không có hiện rõ thiếu API |
| A04 | `/admin/users/new` | RHF/Zod name + email hoặc E.164 phone, DOB/gender optional, duplicate errors; INVITED, không password/grant/delivery claim | Real POST hoặc mock |
| A05 | Trong user detail | Confirm disable/reactivate, pending/error/success, self/lastAdmin, stale version, valid lifecycle; thu hồi mock session | Real Boolean PATCH; activation real bị khóa nếu thiếu eligibility metadata |
| A06 | `/admin/roles` | 5 roles, mô tả/scope/requirements/count/detail; read-only | Admin mock |
| A07 | `/admin/owner-verifications` | Pending default, global q/status/date/page, reviewed records/history navigation | Admin mock, đọc Owner source chung |
| A08 | `/admin/owner-verifications/:userId` | Metadata, restricted expiring sample preview, approve/reject + reason/confirm, reviewer/time/history,409 conflict, safe audit và inbox | Trusted shared mock identity workflow |
| A09 | `/admin/owners` | Canonical OWNER directory, account/verification riêng, reviewed date/Branch count/liên kết readonly | Admin mock |
| A10 | `/admin/branches` | Basic directory + detail, owner/manager, q/status/province/owner/page; không cấu hình/vận hành | Shared Branch projection readonly |
| A11 | `/admin/activity` | Safe events, global q/type/date/page | Admin + imported Owner assignment events |
| A12 | `/admin/activity/:eventId` | Whitelisted named fields, reviewer/reason/time/IDs, copy/link; không raw JSON/edit/delete | Admin mock |
| A13 | `/notifications`, `/profile` | Reuse pages/service/inbox/session, correct deep links, read/all-read, Admin categories | Provider infrastructure hiện có |

AdminLayout dùng Public header/footer và Container hiện có, grouped desktop sidebar, mobile selects/workspace switch, không có Branch selector toàn quyền. Tất cả Admin routes đi qua authenticated guard rồi ADMIN entitlement; canonical mock status/role kiểm tra lại tại API.

## Kiến trúc và tái sử dụng

- Feature presentation: `src/features/admin/{components,context,labels,user-pages,verification-pages,platform-pages}.tsx/ts`; lazy routes và `src/layouts/admin-layout.tsx`.
- Typed Zod contracts `src/interfaces/admin.ts`, một adapter `src/services/admin.ts`; feature split `adminUsers` (API BE đang có) và `admin` (còn thiếu). Dùng HTTP/error handling chung, không mock fallback khi real lỗi.
- AdminEngine bọc **OwnerEngine → ManagerEngine → database/Tenant/inventory** hiện có. Admin metadata/events nằm trong cùng persisted transaction state. Không có tập Users/Branches/Admin inbox riêng.
- Reuse `Container`, `Panel`, `Notice`, `QueryState`, `Field`, `Button`, `Modal`, `Records`, `Facts`, `ManagerForm`, `ActionDialog`, `StatusBadge`, date utilities, auth/session, notifications và profile. Filters URL + query keys gồm actor/filter; invalidation sau mutation.
- Shared fixes cần thiết: User status đủ5; Manager scope kiểm tra canonical roles/status; hybrid bridge không hồi sinh disabled actor; Owner view nhận entitlement đã provision từ authenticated profile; Modal trả focus cho controlled dialog; safe User DTO tại Admin entitlements và Owner Manager acceptance/directory bỏ credential fields.
- Không thêm animation/UI library, không sửa stylesheet/token/framework hoặc rewrite các vai trò cũ.

## Bảo mật và workflow đã kiểm tra

Unauthorized guest/USER/TENANT/MANAGER/OWNER bị chặn Admin route lẫn direct API; giả role trên caller không có tác dụng. ADMIN không tự có quyền sửa Branch, assignment hay xem/ghi tài chính riêng tư. Multi-role hợp lệ chỉ dùng scope role và tài nguyên đã cấp.

INVITED không có default credential và không force activate. Không tự disable Admin, không mất last effective Admin; disable mock xóa sessions/refresh principal và chặn mọi role. Reactivation hợp lệ không phục hồi token cũ. Quan hệ thuê/quản lý/sở hữu được giữ, không xóa Users. Chưa có unlock account workflow cho LOCKED.

Owner submission không cấp role. Review phải ADMIN canonical, không self-review, PENDING/ACTIVE/doc/contact đủ điều kiện và version khớp. Trusted approved transition provision OWNER theo policy cố định, ghi audit/notification cùng transaction; không có arbitrary grant. APPROVED không kèm entitlement vẫn bị Owner engine từ chối. Rejected có lý do và resubmit mới. Concurrent POST: một200, một409; không duplicate audit/notification.

Document API kiểm tra ADMIN, chỉ synthetic SVG sample vô danh TTL60s; UI revoke Blob URL khi đóng/unmount/expiry, query gcTime0. Không giữ raw giấy tờ trong DB/audit/notification. Audit schema strip trường lạ; UI không render raw JSON. Test regression chèn passwordHash thật vào canonical object chứng minh projection không xuất hash mà không xóa credential gốc.

### Cross-role

- Browser: USER onboarding → ADMIN queue/detail/sample/review → OWNER kết quả → OWNER workspace/create Branch → ADMIN thấy Branch readonly.
- Domain integration test S15: USER→ADMIN approval→OWNER tạo Branch/policy→verified User nhận Manager invitation và Owner assignment→MANAGER tạo phòng/service/contract→TENANT sandbox first prepay, activate tenancy, optional service, issued Invoice/payment/maintenance→OWNER summary dùng cùng dữ liệu→ADMIN basic summary; Admin financial bypass bị chặn.
- Sandbox receipt tách actual receipt; không tự trừ booking hold/prepayment hoặc tạo thuật toán tài chính Q1–Q5 chưa được chốt. Assignment event nhập một lần, counters/notification dựa shared sources.
- Đổi account trong cùng tab giữ domain, xóa session/query cache cũ; API kiểm tra identity hiện tại. Không tuyên bố mock đồng bộ đa tab hoặc đa thiết bị.

## Kiểm thử thực thi

Kết quả cuối trên máy local ngày 16/10/2026; không suy ra “đã pass” từ code test tồn tại.

| Check | Kết quả |
| --- | --- |
| `npm.cmd run typecheck` | PASS |
| `npm.cmd run lint` | PASS |
| `npm.cmd test` | 312/312 PASS, 12 files, gồm các bộ trước đó và Admin/privacy mới |
| `npm.cmd run build` | PASS; cảnh báo chunk MSW browser 564.50kB trước gzip, không lỗi build |
| Admin-focused Edge E2E vòng đầu | 24/24 PASS, desktop + mobile |
| `npm.cmd run test:e2e -- --workers=3` | 122/122 PASS trong7 phút: 98 core-role regressions +24 Admin desktop/mobile |

Focused source: `src/mocks/admin-engine.test.ts` (domain/security/cross-role), `admin-handler.test.ts` (MSW HTTP boundary), `src/services/admin-provider.test.ts` (real/mock/hybrid adapters), thêm Owner credential projection regression trong `owner-engine.test.ts`. Những ca parameterized được tính theo kết quả Vitest, không đồng nghĩa mỗi dòng `it` là một ca.

70 expectations của prompt được kiểm chứng theo nhóm: auth/cache/direct bypass (1–9); real adapter/paging/create/lifecycle (10–20); queue/review/document/metadata/concurrency/provision/inbox (21–36); role/directory/financial scope (37–42); safe immutable audit và dedup notifications (43–48); shared cross-role journey và reset (49–60); responsive/render/dialog/form/refresh/P0/regression/build (61–70). Không thay thế kiểm tra server production bằng các ca mock này.

Playwright Admin có 12 scenarios ×2 viewports. Desktop1440×1000, iPhone13, tablet820×1180. Kiểm tra11 Admin routes, no overflow/pageerror, filters drawer, keyboard Escape/focus return, inline errors, confirmations, competing review và cache sau logout. Edge dùng tooling cũ của repo; in-app browser bootstrap báo `Browser is not available: iab`, nên không claim đã dùng in-app browser.

Ảnh thật trong [admin-screenshots](admin-screenshots/): dashboard-desktop, users-mobile, verification-mobile, users-tablet. Đã mở ảnh để đánh giá hierarchy/whitespace/responsive; ảnh được làm mới trong vòng full E2E cuối sau cải thiện navigation mobile và chờ dữ liệu tablet tải xong.

## Taste Skills Applied

Global routing được đọc tại `C:/Users/Admin/.codex/AGENTS.md`. Catalog có `design-taste-frontend`, `redesign-existing-projects`, `minimalist-ui`, `high-end-visual-design`, `image-to-code`, `industrial-brutalist-ui`, `gpt-taste`, `stitch-design-taste`; không claim đã load tất cả. Admin là dashboard/form/data app mới theo minimalist TroPro, primary phù hợp là **minimalist-ui**, không phải public marketing workflow.

- **Đã đọc đầy đủ:** `C:/Users/Admin/.agents/skills/minimalist-ui/SKILL.md`. Áp dụng flat restrained surfaces, hierarchy/typographic contrast, whitespace, minimal decoration: AdminLayout grouped nav; dashboard KPI strip/status distribution; Records tables→mobile cards; focused mobile filter drawer; concise create form; separated verification metadata/decision/history; safe audit Facts. Mobile nav thu gọn sau xem ảnh; không gradients/heavy shadows/unnecessary motion.
- Explicit brief ưu tiên **TroPro teal #0F766E, Be Vietnam Pro, Tailwind, Lucide và token hiện có**, thay các gợi ý palette/font/icon khác của skill. Không áp dụng style rule xung đột một cách máy móc.
- Complementary domain skill **đã đọc đầy đủ**, không phải Taste: `C:/Users/Admin/.codex/skills/API/SKILL.md` (`api-and-interface-design`). Áp dụng `adminUsers`/`admin` boundaries, typed nullable DTO, Spring page normalization, strict requests/whitelist responses, explicit errors/version conflict, no arbitrary grants và no real fallback.
- Browser workflow hướng dẫn tái sử dụng từ `C:/Users/Admin/.codex/plugins/cache/openai-bundled/browser/26.609.41114/skills/control-in-app-browser/SKILL.md` đã đọc ở công việc trước; bootstrap Admin thất bại như trên, xác minh thực tế bằng Edge Playwright. Không liệt kê browser skill như Taste Skill.

## Giới hạn và phần hoãn

1. API Admin dashboard/verification/documents/roles/directories/audit chưa có BE; real adapter chỉ đối chiếu source và HTTP stubs, chưa chạy live BE/DB/SMTP/private storage.
2. Real Boolean active-status chưa bảo đảm self/lastAdmin/concurrency/revocation; JWT filter không đọc current status. FE không thể bảo đảm thu hồi JWT thật. Real activation UI cần eligibility DTO trước khi mở.
3. Hybrid dùng real Admin User APIs; missing Admin mock fail closed cho real identity vì không có trusted ADMIN bridge. Full review/cross-role demo dùng fully mock auth.
4. Preview là tài liệu mẫu synthetic, không phải raw uploaded document. Seed createdAt là mock ingestion time; Branch createdAt chưa có nguồn trả null. Existing seed verification history không được dựng audit giả. Mock reset giữ identity ngoài3 Admin/applicant seed; xem demo guide để reset toàn bộ.
5. SessionStorage một tab, transaction/concurrency không thay DB lock/outbox production. Không lưu token auth FE vào localStorage. State demo nhìn được trong DevTools nên không sử dụng giấy tờ thật.
6. Hoãn A14/moderation Phase3; eKYC thật, multi-step verification, arbitrary role grant/revoke, hard-delete, SIEM/login history đa thiết bị, infrastructure/security admin; mọi quyền sửa dữ liệu tài chính Tenant hoặc toàn quyền Branch không thuộc phạm vi. Các financial policy Q1–Q5 từ Owner vẫn OPEN.

Chi tiết bàn giao: [API contract](admin-api-contract.md). Cách thao tác: [Demo guide](admin-demo-guide.md).
