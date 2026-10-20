# Student 6 API — CSVC, sửa chữa, tenant, owner verification, thread

Phạm vi: BE-06.1 – BE-06.9 trong [be_tasks.md](../be_tasks.md). Mọi response dùng `ApiResponse` ([ERROR_RESPONSES.md](ERROR_RESPONSES.md)); danh sách phân trang dùng `PagedResponse` với `page` bắt đầu từ **1**, `size` tối đa 100.

**Quan trọng:** chưa có JWT/Branch-scope của Student 1 trên branch này. Mọi cổng phụ thuộc ở mục 3 hiện là *placeholder fail-closed* (từ chối tất cả), nên các API dưới đây trả 401/403 cho đến khi được thay bằng adapter thật. Test dùng Mockito double cho các cổng này.

## 1. Endpoint

| Feature | Method + path | Ai gọi | Ghi chú |
|---|---|---|---|
| P1-107/109 | `POST /api/furnitures`, `GET /api/furnitures?roomId&status&type`, `GET/PUT /api/furnitures/{id}` | Owner/Manager của Room | Không xóa cứng; `RETIRED` bị chặn khi còn yêu cầu OPEN/ACCEPTED/IN_PROGRESS (P1-110) |
| P1-108 | `GET/POST /api/furnitures/{id}/warranties`, `PUT/DELETE .../{warrantyId}` | Owner/Manager | `endDate ≥ startDate` |
| P2-29 | `GET /api/furnitures/warranties/expiring?roomId&withinDays`, `GET /api/furnitures/frequent-failures?roomId&minRequests` | Owner/Manager | `minRequests ≥ 2`; trả số lần báo hỏng, không tự đề xuất xử lý |
| P1-111/112 | `POST /api/maintenance-requests` | Tenant đang thuê Room | ≥ 1 furniture hoặc service; item cùng Room, không trùng; `roomServiceId` thuộc Room do FK composite kiểm tra |
| P1-119 | `GET /api/maintenance-requests/my`, `GET /api/maintenance-requests/{id}` | Người gửi / Owner-Manager | Tenant không thấy record COST/LIABILITY |
| P1-114 | `GET /api/maintenance-requests?roomId&status&priority&overdue`, `POST /{id}/accept|reject|start|complete` | Owner/Manager | Xem mục 2 |
| P1-115 | `POST /{id}/assign` | Owner/Manager | Một người phụ trách; CAS theo `version`; ghi record ASSIGNMENT |
| P1-113 | `POST /{id}/evidence` | Người gửi / Owner-Manager | Chỉ tham chiếu Cloudinary (`public_id`, `resource_type`, `stage` BEFORE/AFTER); thêm nguyên tử vào `evidence_refs` |
| P1-116 | `POST /{id}/records` (WORK hoặc COST) | Owner/Manager | WORK không có amount; COST bắt buộc amount, mặc định PENDING |
| P1-117/118 | `POST /{id}/records/{costId}/liabilities`, `POST /{id}/records/{recordId}/approve|reject` | Owner/Manager | Tổng liability ≤ cost; duyệt COST cần tổng liability APPROVED = cost |
| P2-31 | `POST /{id}/rating` | Người gửi | 1–5, một lần, chỉ khi COMPLETED |
| P2-32 | `GET /api/maintenance-requests/cost-stats?roomId` hoặc `?branchId` | Owner/Manager | Liability APPROVED theo Room và bên chịu; không có chiều "nguyên nhân" vì schema không lưu |
| P2-19 | `POST/GET /api/contract-tenants/{id}/handover` | Owner/Manager (ghi); thêm thành viên (đọc) | Snapshot lấy asset/name/status từ DB, người gọi chỉ cung cấp condition/note; ghi một lần |
| P1-31/32/36/34 | `GET /api/tenants?branchId|roomId|contractId`, `GET /api/tenants/me`, `GET /api/tenants/{id}/occupancies` | Owner/Manager; chính Tenant | Chỉ đọc; chỉ trả tên hiển thị |
| P1-08/132 | `GET /api/admin/owner-verifications`, `POST /{userId}/approve|reject` (body `reason`) | ADMIN | Mặc định lọc PENDING; không đổi role |
| P3-01..03 | `POST/GET /api/threads`, `GET /my`, `GET/PUT /{id}`, `POST /{id}/close` | Người đã đăng nhập | HIDDEN chỉ tác giả thấy; sửa/đóng chỉ khi OPEN |
| P3-14 | job `ThreadAutoCloseJob` | hệ thống | Đóng thread OPEN có `expires_at` đã qua |

Mã lỗi mới (không đổi mã cũ): 1704–1713 (maintenance), 1190–1191 (owner verification). Thread dùng 2000/2001 sẵn có.

## 2. Trạng thái yêu cầu sửa chữa

Tài liệu chỉ định nghĩa tập trạng thái (`OPEN, ACCEPTED, IN_PROGRESS, COMPLETED, REJECTED, CANCELLED`) và các động từ của P1-114 (tiếp nhận, từ chối, bắt đầu xử lý, hoàn thành); **không** định nghĩa đồ thị chuyển trạng thái. Code chỉ cho phép:

| Từ | Hành động | Sang |
|---|---|---|
| OPEN | accept | ACCEPTED |
| OPEN | reject | REJECTED |
| ACCEPTED | start | IN_PROGRESS |
| IN_PROGRESS | complete | COMPLETED (ghi `completed_at`) |

`CANCELLED` không có hành động vì chưa có feature nào mô tả. Mỗi chuyển trạng thái ghi record STATUS và gửi thông báo type `REPAIR_REQUEST_<STATUS>` (khóa dedupe ổn định, P1-126).

## 3. Cổng phụ thuộc cần thay khi merge

| Cổng (interface) | Placeholder hiện tại | Chủ sở hữu |
|---|---|---|
| `MaintenanceAccessPolicy` (user hiện tại, `canManageRoom`, `canManageBranch`) | `PendingSecurityMaintenanceAccessPolicy` | Student 1 (+ Student 2 cho Room→Branch) |
| `RoomTenancyPort` (tenancy ACTIVE của user trong Room) | `PendingLeasingRoomTenancyPort` | Student 3 |
| `MaintenanceNotifier` | `PendingNotificationMaintenanceNotifier` (no-op) | Student 4 (`NotificationEvent`) |
| `OwnerVerificationAccessPolicy` (ADMIN) | `PendingSecurityOwnerVerificationAccessPolicy` | Student 1 |
| `TenantAccessPolicy` | `PendingSecurityTenantAccessPolicy` | Student 1 |
| `CurrentUserProvider` (Thread) | `PendingSecurityCurrentUserProvider` | Student 1 |

Các ghi có trigger/audit gọi `AuditActorContext.bind(userId)` trong transaction để `rental.actor_id` đúng người thao tác.

## 4. Cấu hình

| Thuộc tính | Mặc định | Ý nghĩa |
|---|---|---|
| `app.maintenance.sla-hours.<PRIORITY>` | không đặt | P2-30: số giờ xử lý theo mức ưu tiên; không đặt thì `sla_due_at` null |
| `app.threads.expiry-days` | `0` | Số ngày thread tự hết hạn; 0 = không hết hạn (dự án chưa chốt giá trị) |
| `app.threads.auto-close-interval-ms` | `600000` | Chu kỳ job tự đóng |
| `app.threads.auto-close-initial-delay-ms` | `60000` | Trễ chạy lần đầu |

Biến môi trường kết nối DB giữ như [README](README.md) (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).

## 5. Kiểm thử và vận hành

- `mvn -f be-sba-project/pom.xml clean test` — chạy offline (DataSource mock). Các truy vấn JPQL được Hibernate phân tích khi nạp context; **native query `appendEvidence` và các `UPDATE ... SET <jsonb>` (thread criteria, handover snapshot) chưa chạy trên PostgreSQL thật**.
- Chưa chạy được: test tích hợp PostgreSQL (P2-38), kiểm thử tranh chấp nhiều kết nối (P1-135) — máy này không có Docker/PostgreSQL. Cần môi trường PostgreSQL thật theo [TEST_RESULTS.md](../database/rental/TEST_RESULTS.md).
- P2-39: schema `rental` có sẵn index cho `report_requests(room_id,status)`, `report_records(report_request_id,occurred_at)` và `ix_repair_sla`; chưa có `EXPLAIN` trên dữ liệu thật. Sao lưu/khôi phục dùng cơ chế của Supabase/`pg_dump` cho schema `rental`; chưa có quy trình nào được nhóm chốt.
