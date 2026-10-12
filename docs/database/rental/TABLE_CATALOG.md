# Catalog PostgreSQL rental — thiết kế 34 bảng

Sinh bằng `generate-docs.mjs` từ `schema-catalog.json`; không ghi dữ liệu người dùng. Phase 1: 32 bảng; Phase 2: thêm 1; Phase 3: thêm 1. Phase 2/3 có ALTER TABLE cho các cột gộp; catalog hiển thị cấu hình đầy đủ.

## Danh sách bảng và module sở hữu ghi

| Table | Mục đích | PK | FK quan trọng | Phase | Thay đổi từ ERD cũ | Module ghi |
|---|---|---|---|---|---|---|
| `appointments` | Lịch xem theo khoảng giờ | PRIMARY KEY (id) | users, rooms | 1 | Sửa cấu trúc / ràng buộc | leasing |
| `audit_logs` | Audit chỉ được thêm | PRIMARY KEY (id) | users | 1 | Thêm | common.audit |
| `bookings` | Yêu cầu giữ Room/Slot của User | PRIMARY KEY (id) | appointments, branches, branch_policies, users, rooms, room_slots | 1 | Sửa cấu trúc / ràng buộc | leasing |
| `branch_policies` | Phiên bản chính sách Branch | PRIMARY KEY (id) | branches, users | 1 | Thêm | property |
| `branches` | Cơ sở và Manager hiện tại, QR / Zalo | PRIMARY KEY (id) | users, owner_managers | 1 | Sửa cấu trúc / ràng buộc | property |
| `contract_amendments` | Phụ lục đã duyệt và phiên bản tài liệu bổ sung | PRIMARY KEY (id) | users, contracts, contract_tenants | 2 | Thêm | leasing |
| `contract_services` | Dịch vụ / giá thỏa thuận Contract hoặc Member | PRIMARY KEY (id) | contracts, contract_tenants, users, room_services | 1 | Sửa cấu trúc / ràng buộc | utilities |
| `contract_tenants` | Nghĩa vụ User theo Contract, snapshot lúc ký, move-out/handover | PRIMARY KEY (id) | contracts, users, room_slots, bookings | 1 | Sửa cấu trúc / ràng buộc | leasing |
| `contracts` | Hợp đồng chính của Room và PDF/evidence chính | PRIMARY KEY (id) | branches, users, rooms | 1 | Sửa cấu trúc / ràng buộc | leasing |
| `cost_allocation_proposals` | Đề xuất và phân bổ được duyệt bằng JSONB | PRIMARY KEY (id) | users, room_service_charges | 1 | Thêm | utilities |
| `furniture_warranties` | Bảo hành tài sản | PRIMARY KEY (id) | furnitures | 1 | Sửa cấu trúc / ràng buộc | maintenance |
| `furnitures` | Tài sản phòng | PRIMARY KEY (id) | rooms | 1 | Sửa cấu trúc / ràng buộc | maintenance |
| `invoice_items` | Dòng phí và dòng áp dụng tiền trả trước | PRIMARY KEY (id) | invoices, users, room_service_charges, payments, cost_allocation_proposals, report_records | 1 | Sửa cấu trúc / ràng buộc | billing |
| `invoices` | Hóa đơn riêng ContractTenant/tháng | PRIMARY KEY (id) | users, contract_tenants, invoices | 1 | Sửa cấu trúc / ràng buộc | billing |
| `notifications` | Thông báo theo người nhận/kênh; delivery/retry tối thiểu | PRIMARY KEY (id) | users | 1 | Thêm | notification |
| `owner_managers` | Quan hệ Owner–Manager hiện tại | PRIMARY KEY (owner_id, manager_id) | users | 1 | Thêm | identity |
| `payment_webhook_events` | Bằng chứng xử lý callback và chống lặp | PRIMARY KEY (id) | payments | 1 | Thêm | payment |
| `payments` | Khoản nhận tiền và phân loại / hoàn tiền | PRIMARY KEY (id) | bookings, branches, invoices, users, contract_tenants | 1 | Sửa cấu trúc / ràng buộc | payment |
| `report_records` | Lịch sử xử lý / phân công / COST / LIABILITY | PRIMARY KEY (id) | users, contract_tenants, report_records, report_requests | 1 | Sửa cấu trúc / ràng buộc | maintenance |
| `report_requests` | Yêu cầu sửa chữa | PRIMARY KEY (id) | users, contract_tenants, rooms | 1 | Sửa cấu trúc / ràng buộc | maintenance |
| `report_requests_furnitures` | Tài sản bị báo hỏng | PRIMARY KEY (id) | furnitures, report_requests | 1 | Sửa cấu trúc / ràng buộc | maintenance |
| `report_requests_services` | Dịch vụ bị báo hỏng | PRIMARY KEY (id) | report_requests, room_services | 1 | Sửa cấu trúc / ràng buộc | maintenance |
| `resource_allocations` | Giữ/thuê tài nguyên hoặc chặn bảo trì theo khoảng ngày | PRIMARY KEY (id) | users, bookings, contracts, contract_tenants, rooms, room_slots | 1 | Thêm | leasing |
| `roles` | Danh mục 5 role ứng dụng | PRIMARY KEY (id) | — | 1 | Thêm | identity |
| `room_service_charges` | Phí theo segment, chỉ số, giá snapshot | PRIMARY KEY (id) | users, room_services | 1 | Sửa cấu trúc / ràng buộc | utilities |
| `room_services` | Lịch sử cấu hình và giá dịch vụ phòng | PRIMARY KEY (id) | rooms, services | 1 | Sửa cấu trúc / ràng buộc | utilities |
| `room_slot_occupancies` | Khoảng cư trú thực tế | PRIMARY KEY (id) | contract_tenants, room_slots | 1 | Sửa cấu trúc / ràng buộc | leasing |
| `room_slots` | Slot thuộc Room | PRIMARY KEY (id) | rooms | 1 | Sửa cấu trúc / ràng buộc | property |
| `room_types` | Loại phòng trong Branch | PRIMARY KEY (id) | branches | 1 | Sửa cấu trúc / ràng buộc | property |
| `rooms` | Phòng / chế độ cho thuê / thông tin tìm kiếm | PRIMARY KEY (id) | branches, room_types | 1 | Sửa cấu trúc / ràng buộc | property |
| `services` | Danh mục dịch vụ theo Branch | PRIMARY KEY (id) | branches | 1 | Sửa cấu trúc / ràng buộc | utilities |
| `threads` | Thread cơ bản; không Comment/Suggestion/Report | PRIMARY KEY (id) | users | 3 | Thêm | community |
| `user_roles` | User N–N Role | PRIMARY KEY (user_id, role_id) | users, roles | 1 | Thêm | identity |
| `users` | Hồ sơ cá nhân và tài khoản chung; auth/Owner Verification tối giản | PRIMARY KEY (id) | users | 1 | Thêm | identity |

`MANAGERS` được gộp vào `users` + role `MANAGER` + `owner_managers`; `APPOINTMENT` chuẩn hóa thành `appointments`. Không tạo `tenant_contracts`, `payment_allocations`, `cost_allocation_items`, `bank_transactions`, `owner_bank_accounts`, `payment_requests`, `file_metadata`, bảng meter, deposit ledger hay dashboard. Không có bảng cũ nào được sao chép nguyên DDL: giữ khái niệm nhưng chuẩn hóa PK/FK/type/scope.

`reporting` chỉ đọc bảng/view qua API module hoặc read query có kiểm tra phạm vi; không sở hữu bảng nghiệp vụ. `common.audit` là hạ tầng audit, không thêm module `platform`. `identity` sở hữu hồ sơ tenant; `leasing` sở hữu nghĩa vụ thuê và cư trú.

## appointments — Phase 1

Lịch xem theo khoảng giờ. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| requester_user_id | uuid | Không | — |
| assigned_to | uuid | Có | — |
| start_at | timestamp with time zone | Không | — |
| end_at | timestamp with time zone | Không | — |
| status | character varying(20) | Không | — |
| policy_snapshot | jsonb | Không | — |
| note | text | Có | — |
| version | bigint | Không | 0 |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |

```sql
appointments_assigned_to_fkey: FOREIGN KEY (assigned_to) REFERENCES rental.users(id)
appointments_check: CHECK ((end_at > start_at))
appointments_created_at_not_null: NOT NULL created_at
appointments_end_at_not_null: NOT NULL end_at
appointments_id_not_null: NOT NULL id
appointments_pkey: PRIMARY KEY (id)
appointments_policy_snapshot_not_null: NOT NULL policy_snapshot
appointments_requester_user_id_fkey: FOREIGN KEY (requester_user_id) REFERENCES rental.users(id)
appointments_requester_user_id_not_null: NOT NULL requester_user_id
appointments_room_id_fkey: FOREIGN KEY (room_id) REFERENCES rental.rooms(id)
appointments_room_id_not_null: NOT NULL room_id
appointments_start_at_not_null: NOT NULL start_at
appointments_status_check: CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'COMPLETED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
appointments_status_not_null: NOT NULL status
appointments_updated_at_not_null: NOT NULL updated_at
appointments_version_not_null: NOT NULL version
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX appointments_pkey ON rental.appointments USING btree (id);
CREATE INDEX ix_appointment_room_time ON rental.appointments USING btree (room_id, start_at, end_at);
```

## audit_logs — Phase 1

Audit chỉ được thêm. Module: `common.audit`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| actor_user_id | uuid | Có | — |
| table_name | text | Không | — |
| entity_id | uuid | Có | — |
| action | text | Không | — |
| detail | jsonb | Không | — |
| reason | text | Có | — |
| request_key | text | Có | — |
| occurred_at | timestamp with time zone | Không | now() |

```sql
audit_logs_action_not_null: NOT NULL action
audit_logs_actor_user_id_fkey: FOREIGN KEY (actor_user_id) REFERENCES rental.users(id)
audit_logs_detail_not_null: NOT NULL detail
audit_logs_id_not_null: NOT NULL id
audit_logs_occurred_at_not_null: NOT NULL occurred_at
audit_logs_pkey: PRIMARY KEY (id)
audit_logs_table_name_not_null: NOT NULL table_name
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX audit_logs_pkey ON rental.audit_logs USING btree (id);
CREATE INDEX ix_audit_entity ON rental.audit_logs USING btree (table_name, entity_id, occurred_at);
CREATE UNIQUE INDEX uq_payment_audit_request ON rental.audit_logs USING btree (entity_id, request_key) WHERE ((table_name = 'payments'::text) AND (action = 'UPDATE'::text) AND (request_key IS NOT NULL));
```

## bookings — Phase 1

Yêu cầu giữ Room/Slot của User. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| branch_id | uuid | Không | — |
| room_id | uuid | Không | — |
| room_slot_id | uuid | Có | — |
| requester_user_id | uuid | Không | — |
| appointment_id | uuid | Có | — |
| policy_id | uuid | Không | — |
| rental_mode | character varying(4) | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| hold_amount | numeric(19,0) | Không | — |
| policy_snapshot | jsonb | Không | — |
| payment_due_at | timestamp with time zone | Không | — |
| temporary_hold_until | timestamp with time zone | Không | — |
| confirmed_hold_until | timestamp with time zone | Có | — |
| payment_environment | character varying(10) | Không | — |
| status | character varying(24) | Không | — |
| cancellation_reason | text | Có | — |
| version | bigint | Không | 0 |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |

```sql
bookings_appointment_id_fkey: FOREIGN KEY (appointment_id) REFERENCES rental.appointments(id)
bookings_branch_id_fkey: FOREIGN KEY (branch_id) REFERENCES rental.branches(id)
bookings_branch_id_not_null: NOT NULL branch_id
bookings_check: CHECK ((end_date > start_date))
bookings_check1: CHECK (((((rental_mode)::text = 'ROOM'::text) AND (room_slot_id IS NULL)) OR (((rental_mode)::text = 'SLOT'::text) AND (room_slot_id IS NOT NULL))))
bookings_check2: CHECK ((((status)::text <> 'CONFIRMED'::text) OR (confirmed_hold_until IS NOT NULL)))
bookings_created_at_not_null: NOT NULL created_at
bookings_end_date_not_null: NOT NULL end_date
bookings_hold_amount_check: CHECK ((hold_amount > (0)::numeric))
bookings_hold_amount_not_null: NOT NULL hold_amount
bookings_id_branch_id_key: UNIQUE (id, branch_id)
bookings_id_not_null: NOT NULL id
bookings_id_room_id_key: UNIQUE (id, room_id)
bookings_payment_due_at_not_null: NOT NULL payment_due_at
bookings_payment_environment_check: CHECK (((payment_environment)::text = ANY ((ARRAY['REAL'::character varying, 'SANDBOX'::character varying])::text[])))
bookings_payment_environment_not_null: NOT NULL payment_environment
bookings_pkey: PRIMARY KEY (id)
bookings_policy_id_branch_id_fkey: FOREIGN KEY (policy_id, branch_id) REFERENCES rental.branch_policies(id, branch_id)
bookings_policy_id_not_null: NOT NULL policy_id
bookings_policy_snapshot_not_null: NOT NULL policy_snapshot
bookings_rental_mode_check: CHECK (((rental_mode)::text = ANY ((ARRAY['ROOM'::character varying, 'SLOT'::character varying])::text[])))
bookings_rental_mode_not_null: NOT NULL rental_mode
bookings_requester_user_id_fkey: FOREIGN KEY (requester_user_id) REFERENCES rental.users(id)
bookings_requester_user_id_not_null: NOT NULL requester_user_id
bookings_room_id_branch_id_fkey: FOREIGN KEY (room_id, branch_id) REFERENCES rental.rooms(id, branch_id)
bookings_room_id_not_null: NOT NULL room_id
bookings_room_slot_id_room_id_fkey: FOREIGN KEY (room_slot_id, room_id) REFERENCES rental.room_slots(id, room_id)
bookings_start_date_not_null: NOT NULL start_date
bookings_status_check: CHECK (((status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'AWAITING_PAYMENT'::character varying, 'CONFIRMED'::character varying, 'CONVERTED'::character varying, 'EXPIRED'::character varying, 'CANCELLED'::character varying, 'REJECTED'::character varying])::text[])))
bookings_status_not_null: NOT NULL status
bookings_temporary_hold_until_not_null: NOT NULL temporary_hold_until
bookings_updated_at_not_null: NOT NULL updated_at
bookings_version_not_null: NOT NULL version
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
leasing_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX bookings_id_branch_id_key ON rental.bookings USING btree (id, branch_id);
CREATE UNIQUE INDEX bookings_id_room_id_key ON rental.bookings USING btree (id, room_id);
CREATE UNIQUE INDEX bookings_pkey ON rental.bookings USING btree (id);
CREATE INDEX ix_booking_expiry ON rental.bookings USING btree (temporary_hold_until) WHERE ((status)::text = 'AWAITING_PAYMENT'::text);
CREATE INDEX ix_booking_requester ON rental.bookings USING btree (requester_user_id, created_at DESC);
```

## branch_policies — Phase 1

Phiên bản chính sách Branch. Module: `property`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| branch_id | uuid | Không | — |
| version | integer | Không | — |
| effective_from | timestamp with time zone | Không | — |
| collection_day | integer | Không | — |
| payment_grace_days | integer | Không | — |
| booking_hold_amount | numeric(19,0) | Không | — |
| payment_window_minutes | integer | Không | — |
| temporary_hold_minutes | integer | Không | — |
| confirmed_hold_minutes | integer | Không | — |
| appointment_policy | jsonb | Không | '{}'::jsonb |
| cancellation_terms | text | Có | — |
| created_by | uuid | Không | — |
| created_at | timestamp with time zone | Không | now() |

```sql
branch_policies_appointment_policy_not_null: NOT NULL appointment_policy
branch_policies_booking_hold_amount_check: CHECK ((booking_hold_amount > (0)::numeric))
branch_policies_booking_hold_amount_not_null: NOT NULL booking_hold_amount
branch_policies_branch_id_fkey: FOREIGN KEY (branch_id) REFERENCES rental.branches(id)
branch_policies_branch_id_not_null: NOT NULL branch_id
branch_policies_branch_id_version_key: UNIQUE (branch_id, version)
branch_policies_collection_day_check: CHECK (((collection_day >= 1) AND (collection_day <= 31)))
branch_policies_collection_day_not_null: NOT NULL collection_day
branch_policies_confirmed_hold_minutes_check: CHECK ((confirmed_hold_minutes > 0))
branch_policies_confirmed_hold_minutes_not_null: NOT NULL confirmed_hold_minutes
branch_policies_created_at_not_null: NOT NULL created_at
branch_policies_created_by_fkey: FOREIGN KEY (created_by) REFERENCES rental.users(id)
branch_policies_created_by_not_null: NOT NULL created_by
branch_policies_effective_from_not_null: NOT NULL effective_from
branch_policies_id_branch_id_key: UNIQUE (id, branch_id)
branch_policies_id_not_null: NOT NULL id
branch_policies_payment_grace_days_check: CHECK ((payment_grace_days >= 0))
branch_policies_payment_grace_days_not_null: NOT NULL payment_grace_days
branch_policies_payment_window_minutes_check: CHECK ((payment_window_minutes > 0))
branch_policies_payment_window_minutes_not_null: NOT NULL payment_window_minutes
branch_policies_pkey: PRIMARY KEY (id)
branch_policies_temporary_hold_minutes_check: CHECK ((temporary_hold_minutes > 0))
branch_policies_temporary_hold_minutes_not_null: NOT NULL temporary_hold_minutes
branch_policies_version_check: CHECK ((version > 0))
branch_policies_version_not_null: NOT NULL version
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX branch_policies_branch_id_version_key ON rental.branch_policies USING btree (branch_id, version);
CREATE UNIQUE INDEX branch_policies_id_branch_id_key ON rental.branch_policies USING btree (id, branch_id);
CREATE UNIQUE INDEX branch_policies_pkey ON rental.branch_policies USING btree (id);
```

## branches — Phase 1

Cơ sở và Manager hiện tại, QR / Zalo. Module: `property`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| owner_id | uuid | Không | — |
| manager_id | uuid | Có | — |
| code | character varying(50) | Không | — |
| name | character varying(255) | Không | — |
| address | text | Không | — |
| province | character varying(100) | Có | — |
| district | character varying(100) | Có | — |
| ward | character varying(100) | Có | — |
| latitude | numeric(10,7) | Có | — |
| longitude | numeric(10,7) | Có | — |
| description | text | Có | — |
| rules | text | Có | — |
| public_contact | text | Có | — |
| is_public | boolean | Không | false |
| image_refs | jsonb | Không | '[]'::jsonb |
| qr_public_id | text | Có | — |
| qr_url | text | Có | — |
| zalo_group_url | text | Có | — |
| zalo_enabled | boolean | Không | false |
| status | character varying(20) | Không | — |
| version | bigint | Không | 0 |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |

```sql
branches_address_not_null: NOT NULL address
branches_check: CHECK ((((status)::text <> 'ACTIVE'::text) OR (manager_id IS NOT NULL)))
branches_code_not_null: NOT NULL code
branches_created_at_not_null: NOT NULL created_at
branches_id_not_null: NOT NULL id
branches_id_owner_id_key: UNIQUE (id, owner_id)
branches_image_refs_check: CHECK ((jsonb_typeof(image_refs) = 'array'::text))
branches_image_refs_not_null: NOT NULL image_refs
branches_is_public_not_null: NOT NULL is_public
branches_latitude_check: CHECK (((latitude >= ('-90'::integer)::numeric) AND (latitude <= (90)::numeric)))
branches_longitude_check: CHECK (((longitude >= ('-180'::integer)::numeric) AND (longitude <= (180)::numeric)))
branches_name_not_null: NOT NULL name
branches_owner_id_code_key: UNIQUE (owner_id, code)
branches_owner_id_fkey: FOREIGN KEY (owner_id) REFERENCES rental.users(id)
branches_owner_id_manager_id_fkey: FOREIGN KEY (owner_id, manager_id) REFERENCES rental.owner_managers(owner_id, manager_id)
branches_owner_id_not_null: NOT NULL owner_id
branches_pkey: PRIMARY KEY (id)
branches_status_check: CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
branches_status_not_null: NOT NULL status
branches_updated_at_not_null: NOT NULL updated_at
branches_version_not_null: NOT NULL version
branches_zalo_enabled_not_null: NOT NULL zalo_enabled
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX branches_id_owner_id_key ON rental.branches USING btree (id, owner_id);
CREATE UNIQUE INDEX branches_owner_id_code_key ON rental.branches USING btree (owner_id, code);
CREATE UNIQUE INDEX branches_pkey ON rental.branches USING btree (id);
CREATE INDEX ix_branches_manager ON rental.branches USING btree (manager_id);
```

## contract_amendments — Phase 2

Phụ lục đã duyệt và phiên bản tài liệu bổ sung. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| contract_id | uuid | Không | — |
| contract_tenant_id | uuid | Có | — |
| amendment_number | integer | Không | — |
| effective_from | date | Không | — |
| previous_terms | jsonb | Không | — |
| new_terms | jsonb | Không | — |
| reason | text | Không | — |
| status | character varying(20) | Không | — |
| approved_by | uuid | Có | — |
| approved_at | timestamp with time zone | Có | — |
| created_at | timestamp with time zone | Không | now() |
| document_public_id | text | Có | — |
| document_resource_type | character varying(20) | Có | — |
| document_sha256 | character varying(64) | Có | — |
| document_version | integer | Có | — |
| document_snapshot | jsonb | Có | — |
| document_status | character varying(24) | Có | — |
| confirmation_evidence | jsonb | Không | '[]'::jsonb |

```sql
ck_document_complete: CHECK (((document_status IS NULL) OR ((document_status)::text = 'DRAFT'::text) OR ((document_public_id IS NOT NULL) AND (document_resource_type IS NOT NULL) AND (document_sha256 IS NOT NULL) AND (document_version IS NOT NULL) AND (document_snapshot IS NOT NULL))))
contract_amendments_amendment_number_check: CHECK ((amendment_number > 0))
contract_amendments_amendment_number_not_null: NOT NULL amendment_number
contract_amendments_approved_by_fkey: FOREIGN KEY (approved_by) REFERENCES rental.users(id)
contract_amendments_check: CHECK ((((status)::text <> 'APPROVED'::text) OR ((approved_by IS NOT NULL) AND (approved_at IS NOT NULL))))
contract_amendments_confirmation_evidence_check: CHECK ((jsonb_typeof(confirmation_evidence) = 'array'::text))
contract_amendments_confirmation_evidence_not_null: NOT NULL confirmation_evidence
contract_amendments_contract_id_amendment_number_key: UNIQUE (contract_id, amendment_number)
contract_amendments_contract_id_fkey: FOREIGN KEY (contract_id) REFERENCES rental.contracts(id)
contract_amendments_contract_id_not_null: NOT NULL contract_id
contract_amendments_contract_tenant_id_contract_id_fkey: FOREIGN KEY (contract_tenant_id, contract_id) REFERENCES rental.contract_tenants(id, contract_id)
contract_amendments_created_at_not_null: NOT NULL created_at
contract_amendments_document_sha256_check: CHECK (((document_sha256)::text ~ '^[0-9a-f]{64}$'::text))
contract_amendments_document_snapshot_check: CHECK ((jsonb_typeof(document_snapshot) = 'object'::text))
contract_amendments_document_status_check: CHECK (((document_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'AWAITING_CONFIRMATION'::character varying, 'CONFIRMED'::character varying])::text[])))
contract_amendments_document_version_check: CHECK ((document_version > 0))
contract_amendments_effective_from_not_null: NOT NULL effective_from
contract_amendments_id_not_null: NOT NULL id
contract_amendments_new_terms_not_null: NOT NULL new_terms
contract_amendments_pkey: PRIMARY KEY (id)
contract_amendments_previous_terms_not_null: NOT NULL previous_terms
contract_amendments_reason_not_null: NOT NULL reason
contract_amendments_status_check: CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
contract_amendments_status_not_null: NOT NULL status
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX contract_amendments_contract_id_amendment_number_key ON rental.contract_amendments USING btree (contract_id, amendment_number);
CREATE UNIQUE INDEX contract_amendments_pkey ON rental.contract_amendments USING btree (id);
```

## contract_services — Phase 1

Dịch vụ / giá thỏa thuận Contract hoặc Member. Module: `utilities`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| contract_id | uuid | Không | — |
| contract_tenant_id | uuid | Có | — |
| room_id | uuid | Không | — |
| room_service_id | uuid | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| agreed_unit_price | numeric(19,4) | Có | — |
| status | character varying(24) | Không | — |
| requested_by | uuid | Không | — |
| processed_by | uuid | Có | — |
| processed_at | timestamp with time zone | Có | — |
| reason | text | Có | — |

```sql
contract_services_agreed_unit_price_check: CHECK ((agreed_unit_price >= (0)::numeric))
contract_services_check: CHECK ((end_date > start_date))
contract_services_contract_id_not_null: NOT NULL contract_id
contract_services_contract_id_room_id_fkey: FOREIGN KEY (contract_id, room_id) REFERENCES rental.contracts(id, room_id)
contract_services_contract_tenant_id_contract_id_fkey: FOREIGN KEY (contract_tenant_id, contract_id) REFERENCES rental.contract_tenants(id, contract_id)
contract_services_end_date_not_null: NOT NULL end_date
contract_services_id_not_null: NOT NULL id
contract_services_pkey: PRIMARY KEY (id)
contract_services_processed_by_fkey: FOREIGN KEY (processed_by) REFERENCES rental.users(id)
contract_services_requested_by_fkey: FOREIGN KEY (requested_by) REFERENCES rental.users(id)
contract_services_requested_by_not_null: NOT NULL requested_by
contract_services_room_id_not_null: NOT NULL room_id
contract_services_room_service_id_not_null: NOT NULL room_service_id
contract_services_room_service_id_room_id_fkey: FOREIGN KEY (room_service_id, room_id) REFERENCES rental.room_services(id, room_id)
contract_services_start_date_not_null: NOT NULL start_date
contract_services_status_check: CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'CANCEL_REQUESTED'::character varying, 'CANCELLED'::character varying])::text[])))
contract_services_status_not_null: NOT NULL status
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX contract_services_pkey ON rental.contract_services USING btree (id);
CREATE INDEX ix_contract_services_member ON rental.contract_services USING btree (contract_tenant_id);
```

## contract_tenants — Phase 1

Nghĩa vụ User theo Contract, snapshot lúc ký, move-out/handover. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| contract_id | uuid | Không | — |
| tenant_user_id | uuid | Không | — |
| owner_id | uuid | Không | — |
| room_id | uuid | Không | — |
| room_slot_id | uuid | Có | — |
| source_booking_id | uuid | Có | — |
| tenant_snapshot | jsonb | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| actual_leave_date | date | Có | — |
| role | character varying(20) | Không | — |
| monthly_rent | numeric(19,0) | Không | — |
| first_month_rent | numeric(19,0) | Không | — |
| last_month_rent | numeric(19,0) | Không | — |
| prepaid_first_required | numeric(19,0) | Không | — |
| prepaid_last_required | numeric(19,0) | Không | — |
| terms | text | Không | — |
| status | character varying(20) | Không | — |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |
| version | bigint | Không | 0 |
| move_out_requested_at | timestamp with time zone | Có | — |
| requested_leave_date | date | Có | — |
| move_out_reason | text | Có | — |
| move_out_status | character varying(20) | Có | — |
| move_out_processed_by | uuid | Có | — |
| move_out_processed_at | timestamp with time zone | Có | — |
| handover_at | timestamp with time zone | Có | — |
| handover_by | uuid | Có | — |
| handover_snapshot | jsonb | Có | — |
| handover_evidence_refs | jsonb | Không | '[]'::jsonb |
| prepaid_last_disposition | character varying(20) | Có | — |
| disposition_amount | numeric(19,0) | Có | — |
| disposition_reason | text | Có | — |
| disposition_approved_by | uuid | Có | — |
| disposition_approved_at | timestamp with time zone | Có | — |

```sql
ck_disposition: CHECK (((prepaid_last_disposition IS NULL) OR ((prepaid_last_disposition)::text = 'PENDING'::text) OR ((disposition_amount IS NOT NULL) AND (NULLIF(btrim(disposition_reason), ''::text) IS NOT NULL) AND (disposition_approved_by IS NOT NULL) AND (disposition_approved_by = owner_id) AND (disposition_approved_at IS NOT NULL))))
ck_handover: CHECK ((num_nonnulls(handover_at, handover_by, handover_snapshot) = ANY (ARRAY[0, 3])))
ck_moveout_completed: CHECK ((((move_out_status)::text IS DISTINCT FROM 'COMPLETED'::text) OR (((status)::text = 'LEFT'::text) AND (actual_leave_date IS NOT NULL) AND (handover_at IS NOT NULL))))
ck_moveout_decision: CHECK (((move_out_status IS NULL) OR ((move_out_status)::text <> ALL ((ARRAY['APPROVED'::character varying, 'REJECTED'::character varying, 'COMPLETED'::character varying])::text[])) OR ((move_out_processed_by IS NOT NULL) AND (move_out_processed_at IS NOT NULL))))
ck_moveout_request: CHECK (((move_out_status IS NULL) OR ((move_out_requested_at IS NOT NULL) AND (requested_leave_date IS NOT NULL) AND (move_out_reason IS NOT NULL))))
contract_tenants_check: CHECK ((end_date > start_date))
contract_tenants_check1: CHECK (((actual_leave_date IS NULL) OR (actual_leave_date > start_date)))
contract_tenants_check2: CHECK (((prepaid_first_required = first_month_rent) AND (prepaid_last_required = last_month_rent)))
contract_tenants_check3: CHECK ((((status)::text = 'DRAFT'::text) OR COALESCE(((jsonb_typeof((tenant_snapshot -> 'full_name'::text)) = 'string'::text) AND (NULLIF(btrim((tenant_snapshot ->> 'full_name'::text)), ''::text) IS NOT NULL)), false)))
contract_tenants_contract_id_not_null: NOT NULL contract_id
contract_tenants_contract_id_owner_id_fkey: FOREIGN KEY (contract_id, owner_id) REFERENCES rental.contracts(id, owner_id)
contract_tenants_contract_id_room_id_fkey: FOREIGN KEY (contract_id, room_id) REFERENCES rental.contracts(id, room_id)
contract_tenants_created_at_not_null: NOT NULL created_at
contract_tenants_disposition_amount_check: CHECK ((disposition_amount >= (0)::numeric))
contract_tenants_disposition_approved_by_fkey: FOREIGN KEY (disposition_approved_by) REFERENCES rental.users(id)
contract_tenants_end_date_not_null: NOT NULL end_date
contract_tenants_first_month_rent_check: CHECK ((first_month_rent >= (0)::numeric))
contract_tenants_first_month_rent_not_null: NOT NULL first_month_rent
contract_tenants_handover_by_fkey: FOREIGN KEY (handover_by) REFERENCES rental.users(id)
contract_tenants_handover_evidence_refs_check: CHECK ((jsonb_typeof(handover_evidence_refs) = 'array'::text))
contract_tenants_handover_evidence_refs_not_null: NOT NULL handover_evidence_refs
contract_tenants_handover_snapshot_check: CHECK ((jsonb_typeof(handover_snapshot) = 'object'::text))
contract_tenants_id_contract_id_key: UNIQUE (id, contract_id)
contract_tenants_id_not_null: NOT NULL id
contract_tenants_id_room_id_key: UNIQUE (id, room_id)
contract_tenants_last_month_rent_check: CHECK ((last_month_rent >= (0)::numeric))
contract_tenants_last_month_rent_not_null: NOT NULL last_month_rent
contract_tenants_monthly_rent_check: CHECK ((monthly_rent >= (0)::numeric))
contract_tenants_monthly_rent_not_null: NOT NULL monthly_rent
contract_tenants_move_out_processed_by_fkey: FOREIGN KEY (move_out_processed_by) REFERENCES rental.users(id)
contract_tenants_move_out_status_check: CHECK (((move_out_status)::text = ANY ((ARRAY['REQUESTED'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
contract_tenants_owner_id_not_null: NOT NULL owner_id
contract_tenants_pkey: PRIMARY KEY (id)
contract_tenants_prepaid_first_required_check: CHECK ((prepaid_first_required >= (0)::numeric))
contract_tenants_prepaid_first_required_not_null: NOT NULL prepaid_first_required
contract_tenants_prepaid_last_disposition_check: CHECK (((prepaid_last_disposition)::text = ANY ((ARRAY['PENDING'::character varying, 'REFUND'::character varying, 'REALLOCATE'::character varying, 'RETAIN'::character varying])::text[])))
contract_tenants_prepaid_last_required_check: CHECK ((prepaid_last_required >= (0)::numeric))
contract_tenants_prepaid_last_required_not_null: NOT NULL prepaid_last_required
contract_tenants_role_check: CHECK (((role)::text = ANY ((ARRAY['REPRESENTATIVE'::character varying, 'MEMBER'::character varying])::text[])))
contract_tenants_role_not_null: NOT NULL role
contract_tenants_room_id_not_null: NOT NULL room_id
contract_tenants_room_slot_id_room_id_fkey: FOREIGN KEY (room_slot_id, room_id) REFERENCES rental.room_slots(id, room_id)
contract_tenants_source_booking_id_key: UNIQUE (source_booking_id)
contract_tenants_source_booking_id_room_id_fkey: FOREIGN KEY (source_booking_id, room_id) REFERENCES rental.bookings(id, room_id)
contract_tenants_start_date_not_null: NOT NULL start_date
contract_tenants_status_check: CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ACTIVE'::character varying, 'LEFT'::character varying, 'CANCELLED'::character varying])::text[])))
contract_tenants_status_not_null: NOT NULL status
contract_tenants_tenant_snapshot_check: CHECK ((jsonb_typeof(tenant_snapshot) = 'object'::text))
contract_tenants_tenant_snapshot_not_null: NOT NULL tenant_snapshot
contract_tenants_tenant_user_id_contract_id_start_date_key: UNIQUE (tenant_user_id, contract_id, start_date)
contract_tenants_tenant_user_id_fkey: FOREIGN KEY (tenant_user_id) REFERENCES rental.users(id)
contract_tenants_tenant_user_id_not_null: NOT NULL tenant_user_id
contract_tenants_terms_not_null: NOT NULL terms
contract_tenants_updated_at_not_null: NOT NULL updated_at
contract_tenants_version_not_null: NOT NULL version
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
leasing_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX contract_tenants_id_contract_id_key ON rental.contract_tenants USING btree (id, contract_id);
CREATE UNIQUE INDEX contract_tenants_id_room_id_key ON rental.contract_tenants USING btree (id, room_id);
CREATE UNIQUE INDEX contract_tenants_pkey ON rental.contract_tenants USING btree (id);
CREATE UNIQUE INDEX contract_tenants_source_booking_id_key ON rental.contract_tenants USING btree (source_booking_id);
CREATE UNIQUE INDEX contract_tenants_tenant_user_id_contract_id_start_date_key ON rental.contract_tenants USING btree (tenant_user_id, contract_id, start_date);
CREATE INDEX ix_member_moveout ON rental.contract_tenants USING btree (move_out_status, requested_leave_date) WHERE (move_out_status IS NOT NULL);
CREATE INDEX ix_member_tenant ON rental.contract_tenants USING btree (tenant_user_id);
CREATE UNIQUE INDEX uq_contract_current_member ON rental.contract_tenants USING btree (contract_id, tenant_user_id) WHERE ((status)::text = 'ACTIVE'::text);
CREATE UNIQUE INDEX uq_contract_representative ON rental.contract_tenants USING btree (contract_id) WHERE (((role)::text = 'REPRESENTATIVE'::text) AND ((status)::text = 'ACTIVE'::text));
```

## contracts — Phase 1

Hợp đồng chính của Room và PDF/evidence chính. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| owner_id | uuid | Không | — |
| branch_id | uuid | Không | — |
| room_id | uuid | Không | — |
| code | character varying(50) | Không | — |
| rental_mode | character varying(4) | Không | — |
| payment_environment | character varying(10) | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| terms | text | Không | — |
| status | character varying(20) | Không | — |
| created_by | uuid | Không | — |
| version | bigint | Không | 0 |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |
| document_public_id | text | Có | — |
| document_resource_type | character varying(20) | Có | — |
| document_sha256 | character varying(64) | Có | — |
| document_version | integer | Có | — |
| document_snapshot | jsonb | Có | — |
| document_status | character varying(24) | Có | — |
| confirmation_evidence | jsonb | Không | '[]'::jsonb |

```sql
ck_document_complete: CHECK (((document_status IS NULL) OR ((document_status)::text = 'DRAFT'::text) OR ((document_public_id IS NOT NULL) AND (document_resource_type IS NOT NULL) AND (document_sha256 IS NOT NULL) AND (document_version IS NOT NULL) AND (document_snapshot IS NOT NULL))))
contracts_branch_id_code_key: UNIQUE (branch_id, code)
contracts_branch_id_not_null: NOT NULL branch_id
contracts_branch_id_owner_id_fkey: FOREIGN KEY (branch_id, owner_id) REFERENCES rental.branches(id, owner_id)
contracts_check: CHECK ((end_date > start_date))
contracts_code_not_null: NOT NULL code
contracts_confirmation_evidence_check: CHECK ((jsonb_typeof(confirmation_evidence) = 'array'::text))
contracts_confirmation_evidence_not_null: NOT NULL confirmation_evidence
contracts_created_at_not_null: NOT NULL created_at
contracts_created_by_fkey: FOREIGN KEY (created_by) REFERENCES rental.users(id)
contracts_created_by_not_null: NOT NULL created_by
contracts_document_sha256_check: CHECK (((document_sha256)::text ~ '^[0-9a-f]{64}$'::text))
contracts_document_snapshot_check: CHECK ((jsonb_typeof(document_snapshot) = 'object'::text))
contracts_document_status_check: CHECK (((document_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'AWAITING_CONFIRMATION'::character varying, 'CONFIRMED'::character varying])::text[])))
contracts_document_version_check: CHECK ((document_version > 0))
contracts_end_date_not_null: NOT NULL end_date
contracts_id_branch_id_key: UNIQUE (id, branch_id)
contracts_id_not_null: NOT NULL id
contracts_id_owner_id_key: UNIQUE (id, owner_id)
contracts_id_room_id_key: UNIQUE (id, room_id)
contracts_owner_id_not_null: NOT NULL owner_id
contracts_payment_environment_check: CHECK (((payment_environment)::text = ANY ((ARRAY['REAL'::character varying, 'SANDBOX'::character varying])::text[])))
contracts_payment_environment_not_null: NOT NULL payment_environment
contracts_pkey: PRIMARY KEY (id)
contracts_rental_mode_check: CHECK (((rental_mode)::text = ANY ((ARRAY['ROOM'::character varying, 'SLOT'::character varying])::text[])))
contracts_rental_mode_not_null: NOT NULL rental_mode
contracts_room_id_branch_id_fkey: FOREIGN KEY (room_id, branch_id) REFERENCES rental.rooms(id, branch_id)
contracts_room_id_not_null: NOT NULL room_id
contracts_start_date_not_null: NOT NULL start_date
contracts_status_check: CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ACTIVE'::character varying, 'ENDED'::character varying, 'CANCELLED'::character varying])::text[])))
contracts_status_not_null: NOT NULL status
contracts_terms_not_null: NOT NULL terms
contracts_updated_at_not_null: NOT NULL updated_at
contracts_version_not_null: NOT NULL version
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
leasing_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX contracts_branch_id_code_key ON rental.contracts USING btree (branch_id, code);
CREATE UNIQUE INDEX contracts_id_branch_id_key ON rental.contracts USING btree (id, branch_id);
CREATE UNIQUE INDEX contracts_id_owner_id_key ON rental.contracts USING btree (id, owner_id);
CREATE UNIQUE INDEX contracts_id_room_id_key ON rental.contracts USING btree (id, room_id);
CREATE UNIQUE INDEX contracts_pkey ON rental.contracts USING btree (id);
CREATE INDEX ix_contract_room ON rental.contracts USING btree (room_id, start_date, end_date);
```

## cost_allocation_proposals — Phase 1

Đề xuất và phân bổ được duyệt bằng JSONB. Module: `utilities`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_service_charge_id | uuid | Không | — |
| method | character varying(20) | Không | — |
| allocation_details | jsonb | Không | — |
| calculation_snapshot | jsonb | Không | — |
| reason | text | Có | — |
| proposed_by | uuid | Có | — |
| status | character varying(20) | Không | — |
| processed_by | uuid | Có | — |
| processed_at | timestamp with time zone | Có | — |
| created_at | timestamp with time zone | Không | now() |

```sql
cost_allocation_proposals_allocation_details_check: CHECK ((jsonb_typeof(allocation_details) = 'array'::text))
cost_allocation_proposals_allocation_details_not_null: NOT NULL allocation_details
cost_allocation_proposals_calculation_snapshot_not_null: NOT NULL calculation_snapshot
cost_allocation_proposals_check: CHECK ((((status)::text <> 'APPROVED'::text) OR ((processed_by IS NOT NULL) AND (processed_at IS NOT NULL))))
cost_allocation_proposals_created_at_not_null: NOT NULL created_at
cost_allocation_proposals_id_not_null: NOT NULL id
cost_allocation_proposals_method_check: CHECK (((method)::text = ANY ((ARRAY['RESIDENT_DAYS'::character varying, 'APPROVED_CUSTOM'::character varying])::text[])))
cost_allocation_proposals_method_not_null: NOT NULL method
cost_allocation_proposals_pkey: PRIMARY KEY (id)
cost_allocation_proposals_processed_by_fkey: FOREIGN KEY (processed_by) REFERENCES rental.users(id)
cost_allocation_proposals_proposed_by_fkey: FOREIGN KEY (proposed_by) REFERENCES rental.users(id)
cost_allocation_proposals_room_service_charge_id_fkey: FOREIGN KEY (room_service_charge_id) REFERENCES rental.room_service_charges(id)
cost_allocation_proposals_room_service_charge_id_not_null: NOT NULL room_service_charge_id
cost_allocation_proposals_status_check: CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying, 'SUPERSEDED'::character varying])::text[])))
cost_allocation_proposals_status_not_null: NOT NULL status
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX cost_allocation_proposals_pkey ON rental.cost_allocation_proposals USING btree (id);
CREATE UNIQUE INDEX uq_approved_proposal ON rental.cost_allocation_proposals USING btree (room_service_charge_id) WHERE ((status)::text = 'APPROVED'::text);
```

## furniture_warranties — Phase 1

Bảo hành tài sản. Module: `maintenance`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| furniture_id | uuid | Không | — |
| provider | text | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| terms | text | Có | — |
| warranty_code | text | Có | — |

```sql
furniture_warranties_check: CHECK ((end_date >= start_date))
furniture_warranties_end_date_not_null: NOT NULL end_date
furniture_warranties_furniture_id_fkey: FOREIGN KEY (furniture_id) REFERENCES rental.furnitures(id)
furniture_warranties_furniture_id_not_null: NOT NULL furniture_id
furniture_warranties_id_not_null: NOT NULL id
furniture_warranties_pkey: PRIMARY KEY (id)
furniture_warranties_provider_not_null: NOT NULL provider
furniture_warranties_start_date_not_null: NOT NULL start_date
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX furniture_warranties_pkey ON rental.furniture_warranties USING btree (id);
```

## furnitures — Phase 1

Tài sản phòng. Module: `maintenance`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| asset_code | character varying(50) | Không | — |
| name | text | Không | — |
| furniture_type | character varying(100) | Có | — |
| serial_number | text | Có | — |
| purchase_date | date | Có | — |
| asset_value | numeric(19,0) | Có | — |
| condition | text | Có | — |
| status | character varying(20) | Không | — |

```sql
furnitures_asset_code_not_null: NOT NULL asset_code
furnitures_asset_value_check: CHECK ((asset_value >= (0)::numeric))
furnitures_id_not_null: NOT NULL id
furnitures_id_room_id_key: UNIQUE (id, room_id)
furnitures_name_not_null: NOT NULL name
furnitures_pkey: PRIMARY KEY (id)
furnitures_room_id_asset_code_key: UNIQUE (room_id, asset_code)
furnitures_room_id_fkey: FOREIGN KEY (room_id) REFERENCES rental.rooms(id)
furnitures_room_id_not_null: NOT NULL room_id
furnitures_status_check: CHECK (((status)::text = ANY ((ARRAY['IN_USE'::character varying, 'DAMAGED'::character varying, 'REPAIRING'::character varying, 'RETIRED'::character varying])::text[])))
furnitures_status_not_null: NOT NULL status
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX furnitures_id_room_id_key ON rental.furnitures USING btree (id, room_id);
CREATE UNIQUE INDEX furnitures_pkey ON rental.furnitures USING btree (id);
CREATE UNIQUE INDEX furnitures_room_id_asset_code_key ON rental.furnitures USING btree (room_id, asset_code);
```

## invoice_items — Phase 1

Dòng phí và dòng áp dụng tiền trả trước. Module: `billing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| invoice_id | uuid | Không | — |
| line_number | integer | Không | — |
| item_type | character varying(24) | Không | — |
| description | text | Không | — |
| quantity | numeric(19,4) | Không | — |
| unit_price | numeric(19,4) | Không | — |
| amount | numeric(19,0) | Không | — |
| source_charge_id | uuid | Có | — |
| source_proposal_id | uuid | Có | — |
| source_report_record_id | uuid | Có | — |
| source_payment_id | uuid | Có | — |
| prepaid_bucket | character varying(12) | Có | — |
| source_snapshot | jsonb | Không | '{}'::jsonb |
| idempotency_key | text | Có | — |
| created_at | timestamp with time zone | Không | now() |
| reversed_at | timestamp with time zone | Có | — |
| reversal_approved_by | uuid | Có | — |
| reversal_reason | text | Có | — |

```sql
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
invoice_items_amount_check: CHECK ((amount >= (0)::numeric))
invoice_items_amount_not_null: NOT NULL amount
invoice_items_check: CHECK ((amount = round((quantity * unit_price))))
invoice_items_check1: CHECK (((((item_type)::text = 'PREPAID_APPLICATION'::text) AND (source_payment_id IS NOT NULL) AND (prepaid_bucket IS NOT NULL) AND (idempotency_key IS NOT NULL)) OR (((item_type)::text <> 'PREPAID_APPLICATION'::text) AND (source_payment_id IS NULL) AND (prepaid_bucket IS NULL) AND (reversed_at IS NULL))))
invoice_items_check2: CHECK ((((item_type)::text = 'SERVICE'::text) = (source_charge_id IS NOT NULL)))
invoice_items_check3: CHECK ((((item_type)::text = 'REPAIR'::text) = (source_report_record_id IS NOT NULL)))
invoice_items_check4: CHECK (((reversed_at IS NULL) OR ((reversal_approved_by IS NOT NULL) AND (reversal_reason IS NOT NULL))))
invoice_items_created_at_not_null: NOT NULL created_at
invoice_items_description_not_null: NOT NULL description
invoice_items_id_not_null: NOT NULL id
invoice_items_idempotency_key_key: UNIQUE (idempotency_key)
invoice_items_invoice_id_fkey: FOREIGN KEY (invoice_id) REFERENCES rental.invoices(id)
invoice_items_invoice_id_line_number_key: UNIQUE (invoice_id, line_number)
invoice_items_invoice_id_not_null: NOT NULL invoice_id
invoice_items_item_type_check: CHECK (((item_type)::text = ANY ((ARRAY['RENT'::character varying, 'SERVICE'::character varying, 'REPAIR'::character varying, 'SURCHARGE'::character varying, 'DISCOUNT'::character varying, 'PREPAID_APPLICATION'::character varying])::text[])))
invoice_items_item_type_not_null: NOT NULL item_type
invoice_items_line_number_check: CHECK ((line_number > 0))
invoice_items_line_number_not_null: NOT NULL line_number
invoice_items_pkey: PRIMARY KEY (id)
invoice_items_prepaid_bucket_check: CHECK (((prepaid_bucket)::text = ANY ((ARRAY['FIRST_MONTH'::character varying, 'LAST_MONTH'::character varying])::text[])))
invoice_items_quantity_check: CHECK ((quantity >= (0)::numeric))
invoice_items_quantity_not_null: NOT NULL quantity
invoice_items_reversal_approved_by_fkey: FOREIGN KEY (reversal_approved_by) REFERENCES rental.users(id)
invoice_items_source_charge_id_fkey: FOREIGN KEY (source_charge_id) REFERENCES rental.room_service_charges(id)
invoice_items_source_payment_id_fkey: FOREIGN KEY (source_payment_id) REFERENCES rental.payments(id)
invoice_items_source_proposal_id_fkey: FOREIGN KEY (source_proposal_id) REFERENCES rental.cost_allocation_proposals(id)
invoice_items_source_report_record_id_fkey: FOREIGN KEY (source_report_record_id) REFERENCES rental.report_records(id)
invoice_items_source_snapshot_not_null: NOT NULL source_snapshot
invoice_items_unit_price_check: CHECK ((unit_price >= (0)::numeric))
invoice_items_unit_price_not_null: NOT NULL unit_price
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX invoice_items_idempotency_key_key ON rental.invoice_items USING btree (idempotency_key);
CREATE UNIQUE INDEX invoice_items_invoice_id_line_number_key ON rental.invoice_items USING btree (invoice_id, line_number);
CREATE UNIQUE INDEX invoice_items_pkey ON rental.invoice_items USING btree (id);
CREATE INDEX ix_items_charge ON rental.invoice_items USING btree (source_charge_id) WHERE (source_charge_id IS NOT NULL);
CREATE INDEX ix_items_payment ON rental.invoice_items USING btree (source_payment_id) WHERE (source_payment_id IS NOT NULL);
CREATE INDEX ix_items_repair ON rental.invoice_items USING btree (source_report_record_id) WHERE (source_report_record_id IS NOT NULL);
```

## invoices — Phase 1

Hóa đơn riêng ContractTenant/tháng. Module: `billing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| contract_tenant_id | uuid | Không | — |
| billing_month | date | Không | — |
| due_date | date | Không | — |
| policy_snapshot | jsonb | Không | — |
| document_status | character varying(12) | Không | — |
| payment_environment | character varying(10) | Không | — |
| subtotal_amount | numeric(19,0) | Không | — |
| adjustment_amount | numeric(19,0) | Không | — |
| total_amount | numeric(19,0) | Không | — |
| replaces_invoice_id | uuid | Có | — |
| issued_by | uuid | Có | — |
| issued_at | timestamp with time zone | Có | — |
| cancellation_reason | text | Có | — |
| adjustment_approved_by | uuid | Có | — |
| adjustment_approved_at | timestamp with time zone | Có | — |
| created_by | uuid | Không | — |
| created_at | timestamp with time zone | Không | now() |
| version | bigint | Không | 0 |

```sql
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
invoices_adjustment_amount_not_null: NOT NULL adjustment_amount
invoices_adjustment_approved_by_fkey: FOREIGN KEY (adjustment_approved_by) REFERENCES rental.users(id)
invoices_billing_month_check: CHECK ((EXTRACT(day FROM billing_month) = (1)::numeric))
invoices_billing_month_not_null: NOT NULL billing_month
invoices_check: CHECK ((total_amount = (subtotal_amount + adjustment_amount)))
invoices_check1: CHECK ((((document_status)::text <> 'ISSUED'::text) OR ((issued_by IS NOT NULL) AND (issued_at IS NOT NULL))))
invoices_contract_tenant_id_fkey: FOREIGN KEY (contract_tenant_id) REFERENCES rental.contract_tenants(id)
invoices_contract_tenant_id_not_null: NOT NULL contract_tenant_id
invoices_created_at_not_null: NOT NULL created_at
invoices_created_by_fkey: FOREIGN KEY (created_by) REFERENCES rental.users(id)
invoices_created_by_not_null: NOT NULL created_by
invoices_document_status_check: CHECK (((document_status)::text = ANY ((ARRAY['DRAFT'::character varying, 'ISSUED'::character varying, 'CANCELLED'::character varying])::text[])))
invoices_document_status_not_null: NOT NULL document_status
invoices_due_date_not_null: NOT NULL due_date
invoices_id_not_null: NOT NULL id
invoices_issued_by_fkey: FOREIGN KEY (issued_by) REFERENCES rental.users(id)
invoices_payment_environment_check: CHECK (((payment_environment)::text = ANY ((ARRAY['REAL'::character varying, 'SANDBOX'::character varying])::text[])))
invoices_payment_environment_not_null: NOT NULL payment_environment
invoices_pkey: PRIMARY KEY (id)
invoices_policy_snapshot_not_null: NOT NULL policy_snapshot
invoices_replaces_invoice_id_fkey: FOREIGN KEY (replaces_invoice_id) REFERENCES rental.invoices(id)
invoices_replaces_invoice_id_key: UNIQUE (replaces_invoice_id)
invoices_subtotal_amount_check: CHECK ((subtotal_amount >= (0)::numeric))
invoices_subtotal_amount_not_null: NOT NULL subtotal_amount
invoices_total_amount_check: CHECK ((total_amount >= (0)::numeric))
invoices_total_amount_not_null: NOT NULL total_amount
invoices_version_not_null: NOT NULL version
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX invoices_pkey ON rental.invoices USING btree (id);
CREATE UNIQUE INDEX invoices_replaces_invoice_id_key ON rental.invoices USING btree (replaces_invoice_id);
CREATE INDEX ix_invoice_due ON rental.invoices USING btree (due_date) WHERE ((document_status)::text = 'ISSUED'::text);
CREATE UNIQUE INDEX uq_member_month_invoice ON rental.invoices USING btree (contract_tenant_id, billing_month) WHERE ((document_status)::text <> 'CANCELLED'::text);
```

## notifications — Phase 1

Thông báo theo người nhận/kênh; delivery/retry tối thiểu. Module: `notification`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| recipient_user_id | uuid | Không | — |
| event_key | text | Không | — |
| type | character varying(50) | Không | — |
| title | text | Không | — |
| message | text | Không | — |
| reference_type | text | Có | — |
| reference_id | uuid | Có | — |
| read_at | timestamp with time zone | Có | — |
| created_at | timestamp with time zone | Không | now() |
| channel | character varying(10) | Không | 'IN_APP'::character varying |
| delivery_status | character varying(12) | Không | 'PENDING'::character varying |
| attempt_count | integer | Không | 0 |
| next_attempt_at | timestamp with time zone | Có | — |
| sent_at | timestamp with time zone | Có | — |
| provider_message_id | text | Có | — |
| last_error | text | Có | — |

```sql
notifications_attempt_count_check: CHECK ((attempt_count >= 0))
notifications_attempt_count_not_null: NOT NULL attempt_count
notifications_channel_check: CHECK (((channel)::text = ANY ((ARRAY['IN_APP'::character varying, 'EMAIL'::character varying, 'SMS'::character varying])::text[])))
notifications_channel_not_null: NOT NULL channel
notifications_created_at_not_null: NOT NULL created_at
notifications_delivery_status_check: CHECK (((delivery_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENDING'::character varying, 'SENT'::character varying, 'FAILED'::character varying, 'CANCELLED'::character varying])::text[])))
notifications_delivery_status_not_null: NOT NULL delivery_status
notifications_event_key_not_null: NOT NULL event_key
notifications_id_not_null: NOT NULL id
notifications_message_not_null: NOT NULL message
notifications_pkey: PRIMARY KEY (id)
notifications_recipient_user_id_fkey: FOREIGN KEY (recipient_user_id) REFERENCES rental.users(id)
notifications_recipient_user_id_not_null: NOT NULL recipient_user_id
notifications_title_not_null: NOT NULL title
notifications_type_not_null: NOT NULL type
uq_notification_delivery: UNIQUE (recipient_user_id, event_key, type, channel)
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_notification_retry ON rental.notifications USING btree (next_attempt_at) WHERE (((channel)::text <> 'IN_APP'::text) AND ((delivery_status)::text = ANY ((ARRAY['PENDING'::character varying, 'FAILED'::character varying])::text[])));
CREATE INDEX ix_notifications_user ON rental.notifications USING btree (recipient_user_id, created_at DESC);
CREATE UNIQUE INDEX notifications_pkey ON rental.notifications USING btree (id);
CREATE UNIQUE INDEX uq_notification_delivery ON rental.notifications USING btree (recipient_user_id, event_key, type, channel);
```

## owner_managers — Phase 1

Quan hệ Owner–Manager hiện tại. Module: `identity`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| owner_id | uuid | Không | — |
| manager_id | uuid | Không | — |
| assigned_at | timestamp with time zone | Không | now() |
| assigned_by | uuid | Không | — |

```sql
owner_managers_assigned_at_not_null: NOT NULL assigned_at
owner_managers_assigned_by_fkey: FOREIGN KEY (assigned_by) REFERENCES rental.users(id)
owner_managers_assigned_by_not_null: NOT NULL assigned_by
owner_managers_check: CHECK ((owner_id <> manager_id))
owner_managers_manager_id_fkey: FOREIGN KEY (manager_id) REFERENCES rental.users(id)
owner_managers_manager_id_key: UNIQUE (manager_id)
owner_managers_manager_id_not_null: NOT NULL manager_id
owner_managers_owner_id_fkey: FOREIGN KEY (owner_id) REFERENCES rental.users(id)
owner_managers_owner_id_not_null: NOT NULL owner_id
owner_managers_pkey: PRIMARY KEY (owner_id, manager_id)
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX owner_managers_manager_id_key ON rental.owner_managers USING btree (manager_id);
CREATE UNIQUE INDEX owner_managers_pkey ON rental.owner_managers USING btree (owner_id, manager_id);
```

## payment_webhook_events — Phase 1

Bằng chứng xử lý callback và chống lặp. Module: `payment`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| payment_id | uuid | Có | — |
| provider | character varying(10) | Không | — |
| environment | character varying(10) | Không | — |
| provider_account_key | text | Không | — |
| event_key | text | Không | — |
| payload_hash | text | Không | — |
| safe_payload | jsonb | Không | — |
| signature_verified | boolean | Không | — |
| processing_status | character varying(20) | Không | — |
| received_at | timestamp with time zone | Không | now() |
| processed_at | timestamp with time zone | Có | — |

```sql
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
payment_webhook_events_check: CHECK ((((processing_status)::text <> 'ACCEPTED'::text) OR (signature_verified AND (payment_id IS NOT NULL) AND (processed_at IS NOT NULL))))
payment_webhook_events_environment_check: CHECK (((environment)::text = 'SANDBOX'::text))
payment_webhook_events_environment_not_null: NOT NULL environment
payment_webhook_events_event_key_not_null: NOT NULL event_key
payment_webhook_events_id_not_null: NOT NULL id
payment_webhook_events_payload_hash_not_null: NOT NULL payload_hash
payment_webhook_events_payment_id_fkey: FOREIGN KEY (payment_id) REFERENCES rental.payments(id)
payment_webhook_events_pkey: PRIMARY KEY (id)
payment_webhook_events_processing_status_check: CHECK (((processing_status)::text = ANY ((ARRAY['RECEIVED'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'FAILED'::character varying])::text[])))
payment_webhook_events_processing_status_not_null: NOT NULL processing_status
payment_webhook_events_provider_account_key_not_null: NOT NULL provider_account_key
payment_webhook_events_provider_check: CHECK (((provider)::text = ANY ((ARRAY['VNPAY'::character varying, 'MOMO'::character varying])::text[])))
payment_webhook_events_provider_environment_provider_accoun_key: UNIQUE (provider, environment, provider_account_key, event_key)
payment_webhook_events_provider_not_null: NOT NULL provider
payment_webhook_events_received_at_not_null: NOT NULL received_at
payment_webhook_events_safe_payload_not_null: NOT NULL safe_payload
payment_webhook_events_signature_verified_not_null: NOT NULL signature_verified
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX payment_webhook_events_pkey ON rental.payment_webhook_events USING btree (id);
CREATE UNIQUE INDEX payment_webhook_events_provider_environment_provider_accoun_key ON rental.payment_webhook_events USING btree (provider, environment, provider_account_key, event_key);
```

## payments — Phase 1

Khoản nhận tiền và phân loại / hoàn tiền. Module: `payment`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| branch_id | uuid | Không | — |
| invoice_id | uuid | Có | — |
| booking_id | uuid | Có | — |
| payer_user_id | uuid | Có | — |
| payer_name_snapshot | text | Có | — |
| method | character varying(20) | Không | — |
| environment | character varying(10) | Không | — |
| status | character varying(20) | Không | — |
| amount_received | numeric(19,0) | Không | — |
| allocated_amount | numeric(19,0) | Không | 0 |
| prepaid_contract_tenant_id | uuid | Có | — |
| prepaid_first_amount | numeric(19,0) | Không | 0 |
| prepaid_last_amount | numeric(19,0) | Không | 0 |
| unallocated_refunded | numeric(19,0) | Không | 0 |
| prepaid_first_refunded | numeric(19,0) | Không | 0 |
| prepaid_last_refunded | numeric(19,0) | Không | 0 |
| provider_account_key | text | Có | — |
| provider_transaction_id | text | Có | — |
| idempotency_key | text | Không | — |
| reference_text | text | Có | — |
| evidence_refs | jsonb | Không | '[]'::jsonb |
| received_at | timestamp with time zone | Có | — |
| verified_by | uuid | Có | — |
| verified_at | timestamp with time zone | Có | — |
| verification_note | text | Có | — |
| created_at | timestamp with time zone | Không | now() |
| version | bigint | Không | 0 |

```sql
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
payments_allocated_amount_check: CHECK ((allocated_amount >= (0)::numeric))
payments_allocated_amount_not_null: NOT NULL allocated_amount
payments_amount_received_check: CHECK ((amount_received > (0)::numeric))
payments_amount_received_not_null: NOT NULL amount_received
payments_booking_id_fkey: FOREIGN KEY (booking_id) REFERENCES rental.bookings(id)
payments_branch_id_fkey: FOREIGN KEY (branch_id) REFERENCES rental.branches(id)
payments_branch_id_not_null: NOT NULL branch_id
payments_check: CHECK ((num_nonnulls(invoice_id, booking_id) <= 1))
payments_check1: CHECK (((allocated_amount = (0)::numeric) OR (num_nonnulls(invoice_id, booking_id) = 1)))
payments_check2: CHECK ((((prepaid_first_amount + prepaid_last_amount) = (0)::numeric) OR (prepaid_contract_tenant_id IS NOT NULL)))
payments_check3: CHECK (((((allocated_amount + prepaid_first_amount) + prepaid_last_amount) + unallocated_refunded) <= amount_received))
payments_check4: CHECK (((prepaid_first_refunded <= prepaid_first_amount) AND (prepaid_last_refunded <= prepaid_last_amount)))
payments_check5: CHECK ((((status)::text = 'VERIFIED'::text) OR ((((allocated_amount + prepaid_first_amount) + prepaid_last_amount) + unallocated_refunded) = (0)::numeric)))
payments_check6: CHECK ((((status)::text <> 'VERIFIED'::text) OR ((received_at IS NOT NULL) AND (verified_at IS NOT NULL))))
payments_check7: CHECK ((((status)::text <> 'VERIFIED'::text) OR ((method)::text = 'MANUAL_QR'::text) OR ((provider_account_key IS NOT NULL) AND (provider_transaction_id IS NOT NULL))))
payments_check8: CHECK ((((status)::text <> 'VERIFIED'::text) OR ((method)::text <> 'MANUAL_QR'::text) OR ((verified_by IS NOT NULL) AND (verification_note IS NOT NULL))))
payments_check9: CHECK (((((method)::text = 'MANUAL_QR'::text) AND ((environment)::text = 'REAL'::text)) OR (((method)::text = ANY ((ARRAY['VNPAY'::character varying, 'MOMO'::character varying])::text[])) AND ((environment)::text = 'SANDBOX'::text))))
payments_created_at_not_null: NOT NULL created_at
payments_environment_check: CHECK (((environment)::text = ANY ((ARRAY['REAL'::character varying, 'SANDBOX'::character varying])::text[])))
payments_environment_not_null: NOT NULL environment
payments_evidence_refs_not_null: NOT NULL evidence_refs
payments_id_not_null: NOT NULL id
payments_idempotency_key_key: UNIQUE (idempotency_key)
payments_idempotency_key_not_null: NOT NULL idempotency_key
payments_invoice_id_fkey: FOREIGN KEY (invoice_id) REFERENCES rental.invoices(id)
payments_method_check: CHECK (((method)::text = ANY ((ARRAY['MANUAL_QR'::character varying, 'VNPAY'::character varying, 'MOMO'::character varying])::text[])))
payments_method_not_null: NOT NULL method
payments_payer_user_id_fkey: FOREIGN KEY (payer_user_id) REFERENCES rental.users(id)
payments_pkey: PRIMARY KEY (id)
payments_prepaid_contract_tenant_id_fkey: FOREIGN KEY (prepaid_contract_tenant_id) REFERENCES rental.contract_tenants(id)
payments_prepaid_first_amount_check: CHECK ((prepaid_first_amount >= (0)::numeric))
payments_prepaid_first_amount_not_null: NOT NULL prepaid_first_amount
payments_prepaid_first_refunded_check: CHECK ((prepaid_first_refunded >= (0)::numeric))
payments_prepaid_first_refunded_not_null: NOT NULL prepaid_first_refunded
payments_prepaid_last_amount_check: CHECK ((prepaid_last_amount >= (0)::numeric))
payments_prepaid_last_amount_not_null: NOT NULL prepaid_last_amount
payments_prepaid_last_refunded_check: CHECK ((prepaid_last_refunded >= (0)::numeric))
payments_prepaid_last_refunded_not_null: NOT NULL prepaid_last_refunded
payments_status_check: CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'VERIFIED'::character varying, 'FAILED'::character varying, 'REVIEW'::character varying])::text[])))
payments_status_not_null: NOT NULL status
payments_unallocated_refunded_check: CHECK ((unallocated_refunded >= (0)::numeric))
payments_unallocated_refunded_not_null: NOT NULL unallocated_refunded
payments_verified_by_fkey: FOREIGN KEY (verified_by) REFERENCES rental.users(id)
payments_version_not_null: NOT NULL version
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_payments_booking ON rental.payments USING btree (booking_id) WHERE (booking_id IS NOT NULL);
CREATE INDEX ix_payments_branch_time ON rental.payments USING btree (branch_id, received_at);
CREATE INDEX ix_payments_invoice ON rental.payments USING btree (invoice_id) WHERE (invoice_id IS NOT NULL);
CREATE INDEX ix_payments_prepaid ON rental.payments USING btree (prepaid_contract_tenant_id) WHERE (prepaid_contract_tenant_id IS NOT NULL);
CREATE UNIQUE INDEX payments_idempotency_key_key ON rental.payments USING btree (idempotency_key);
CREATE UNIQUE INDEX payments_pkey ON rental.payments USING btree (id);
CREATE UNIQUE INDEX uq_provider_payment ON rental.payments USING btree (method, environment, provider_account_key, provider_transaction_id) WHERE (provider_transaction_id IS NOT NULL);
```

## report_records — Phase 1

Lịch sử xử lý / phân công / COST / LIABILITY. Module: `maintenance`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| report_request_id | uuid | Không | — |
| record_type | character varying(20) | Không | — |
| parent_cost_record_id | uuid | Có | — |
| contract_tenant_id | uuid | Có | — |
| responsible_party | character varying(10) | Có | — |
| amount | numeric(19,0) | Có | — |
| description | text | Không | — |
| performed_by | uuid | Không | — |
| assigned_to | uuid | Có | — |
| status_after | character varying(20) | Có | — |
| approval_status | character varying(20) | Có | — |
| approved_by | uuid | Có | — |
| approved_at | timestamp with time zone | Có | — |
| evidence_refs | jsonb | Không | '[]'::jsonb |
| occurred_at | timestamp with time zone | Không | now() |

```sql
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
repair_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
report_records_amount_check: CHECK ((amount >= (0)::numeric))
report_records_approval_status_check: CHECK (((approval_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
report_records_approved_by_fkey: FOREIGN KEY (approved_by) REFERENCES rental.users(id)
report_records_assigned_to_fkey: FOREIGN KEY (assigned_to) REFERENCES rental.users(id)
report_records_check: CHECK (((((record_type)::text = 'LIABILITY'::text) AND (parent_cost_record_id IS NOT NULL) AND (responsible_party IS NOT NULL) AND (amount IS NOT NULL)) OR (((record_type)::text <> 'LIABILITY'::text) AND (parent_cost_record_id IS NULL) AND (responsible_party IS NULL) AND (contract_tenant_id IS NULL))))
report_records_check1: CHECK ((((record_type)::text <> 'LIABILITY'::text) OR ((((responsible_party)::text = 'TENANT'::text) AND (contract_tenant_id IS NOT NULL)) OR (((responsible_party)::text = 'OWNER'::text) AND (contract_tenant_id IS NULL)))))
report_records_check2: CHECK ((((record_type)::text <> ALL ((ARRAY['COST'::character varying, 'LIABILITY'::character varying])::text[])) OR ((amount IS NOT NULL) AND (approval_status IS NOT NULL))))
report_records_check3: CHECK ((((approval_status)::text IS DISTINCT FROM 'APPROVED'::text) OR ((approved_by IS NOT NULL) AND (approved_at IS NOT NULL))))
report_records_contract_tenant_id_fkey: FOREIGN KEY (contract_tenant_id) REFERENCES rental.contract_tenants(id)
report_records_description_not_null: NOT NULL description
report_records_evidence_refs_not_null: NOT NULL evidence_refs
report_records_id_not_null: NOT NULL id
report_records_occurred_at_not_null: NOT NULL occurred_at
report_records_parent_cost_record_id_fkey: FOREIGN KEY (parent_cost_record_id) REFERENCES rental.report_records(id)
report_records_performed_by_fkey: FOREIGN KEY (performed_by) REFERENCES rental.users(id)
report_records_performed_by_not_null: NOT NULL performed_by
report_records_pkey: PRIMARY KEY (id)
report_records_record_type_check: CHECK (((record_type)::text = ANY ((ARRAY['WORK'::character varying, 'ASSIGNMENT'::character varying, 'STATUS'::character varying, 'COST'::character varying, 'LIABILITY'::character varying])::text[])))
report_records_record_type_not_null: NOT NULL record_type
report_records_report_request_id_fkey: FOREIGN KEY (report_request_id) REFERENCES rental.report_requests(id)
report_records_report_request_id_not_null: NOT NULL report_request_id
report_records_responsible_party_check: CHECK (((responsible_party)::text = ANY ((ARRAY['OWNER'::character varying, 'TENANT'::character varying])::text[])))
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_record_request ON rental.report_records USING btree (report_request_id, occurred_at);
CREATE UNIQUE INDEX report_records_pkey ON rental.report_records USING btree (id);
```

## report_requests — Phase 1

Yêu cầu sửa chữa. Module: `maintenance`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| requester_user_id | uuid | Không | — |
| contract_tenant_id | uuid | Có | — |
| title | text | Không | — |
| description | text | Không | — |
| priority | character varying(10) | Không | — |
| status | character varying(20) | Không | — |
| assigned_to | uuid | Có | — |
| evidence_refs | jsonb | Không | '[]'::jsonb |
| completed_at | timestamp with time zone | Có | — |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |
| version | bigint | Không | 0 |
| sla_due_at | timestamp with time zone | Có | — |
| rating | integer | Có | — |
| rating_comment | text | Có | — |
| rated_at | timestamp with time zone | Có | — |

```sql
report_requests_assigned_to_fkey: FOREIGN KEY (assigned_to) REFERENCES rental.users(id)
report_requests_contract_tenant_id_room_id_fkey: FOREIGN KEY (contract_tenant_id, room_id) REFERENCES rental.contract_tenants(id, room_id)
report_requests_created_at_not_null: NOT NULL created_at
report_requests_description_not_null: NOT NULL description
report_requests_evidence_refs_not_null: NOT NULL evidence_refs
report_requests_id_not_null: NOT NULL id
report_requests_id_room_id_key: UNIQUE (id, room_id)
report_requests_pkey: PRIMARY KEY (id)
report_requests_priority_check: CHECK (((priority)::text = ANY ((ARRAY['LOW'::character varying, 'NORMAL'::character varying, 'HIGH'::character varying, 'URGENT'::character varying])::text[])))
report_requests_priority_not_null: NOT NULL priority
report_requests_rating_check: CHECK (((rating >= 1) AND (rating <= 5)))
report_requests_requester_user_id_fkey: FOREIGN KEY (requester_user_id) REFERENCES rental.users(id)
report_requests_requester_user_id_not_null: NOT NULL requester_user_id
report_requests_room_id_fkey: FOREIGN KEY (room_id) REFERENCES rental.rooms(id)
report_requests_room_id_not_null: NOT NULL room_id
report_requests_status_check: CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'ACCEPTED'::character varying, 'IN_PROGRESS'::character varying, 'COMPLETED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
report_requests_status_not_null: NOT NULL status
report_requests_title_not_null: NOT NULL title
report_requests_updated_at_not_null: NOT NULL updated_at
report_requests_version_not_null: NOT NULL version
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_repair_sla ON rental.report_requests USING btree (sla_due_at) WHERE ((status)::text = ANY ((ARRAY['OPEN'::character varying, 'ACCEPTED'::character varying, 'IN_PROGRESS'::character varying])::text[]));
CREATE INDEX ix_report_room_status ON rental.report_requests USING btree (room_id, status);
CREATE UNIQUE INDEX report_requests_id_room_id_key ON rental.report_requests USING btree (id, room_id);
CREATE UNIQUE INDEX report_requests_pkey ON rental.report_requests USING btree (id);
```

## report_requests_furnitures — Phase 1

Tài sản bị báo hỏng. Module: `maintenance`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| report_request_id | uuid | Không | — |
| room_id | uuid | Không | — |
| furniture_id | uuid | Không | — |
| damage_description | text | Có | — |

```sql
report_requests_furnitures_furniture_id_not_null: NOT NULL furniture_id
report_requests_furnitures_furniture_id_room_id_fkey: FOREIGN KEY (furniture_id, room_id) REFERENCES rental.furnitures(id, room_id)
report_requests_furnitures_id_not_null: NOT NULL id
report_requests_furnitures_pkey: PRIMARY KEY (id)
report_requests_furnitures_report_request_id_furniture_id_key: UNIQUE (report_request_id, furniture_id)
report_requests_furnitures_report_request_id_not_null: NOT NULL report_request_id
report_requests_furnitures_report_request_id_room_id_fkey: FOREIGN KEY (report_request_id, room_id) REFERENCES rental.report_requests(id, room_id)
report_requests_furnitures_room_id_not_null: NOT NULL room_id
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX report_requests_furnitures_pkey ON rental.report_requests_furnitures USING btree (id);
CREATE UNIQUE INDEX report_requests_furnitures_report_request_id_furniture_id_key ON rental.report_requests_furnitures USING btree (report_request_id, furniture_id);
```

## report_requests_services — Phase 1

Dịch vụ bị báo hỏng. Module: `maintenance`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| report_request_id | uuid | Không | — |
| room_id | uuid | Không | — |
| room_service_id | uuid | Không | — |
| issue_description | text | Có | — |

```sql
report_requests_services_id_not_null: NOT NULL id
report_requests_services_pkey: PRIMARY KEY (id)
report_requests_services_report_request_id_not_null: NOT NULL report_request_id
report_requests_services_report_request_id_room_id_fkey: FOREIGN KEY (report_request_id, room_id) REFERENCES rental.report_requests(id, room_id)
report_requests_services_report_request_id_room_service_id_key: UNIQUE (report_request_id, room_service_id)
report_requests_services_room_id_not_null: NOT NULL room_id
report_requests_services_room_service_id_not_null: NOT NULL room_service_id
report_requests_services_room_service_id_room_id_fkey: FOREIGN KEY (room_service_id, room_id) REFERENCES rental.room_services(id, room_id)
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX report_requests_services_pkey ON rental.report_requests_services USING btree (id);
CREATE UNIQUE INDEX report_requests_services_report_request_id_room_service_id_key ON rental.report_requests_services USING btree (report_request_id, room_service_id);
```

## resource_allocations — Phase 1

Giữ/thuê tài nguyên hoặc chặn bảo trì theo khoảng ngày. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| room_slot_id | uuid | Có | — |
| booking_id | uuid | Có | — |
| contract_id | uuid | Có | — |
| contract_tenant_id | uuid | Có | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| expires_at | timestamp with time zone | Có | — |
| allocation_kind | character varying(20) | Không | 'RENTAL'::character varying |
| block_reason | text | Có | — |
| blocked_by | uuid | Có | — |
| status | character varying(20) | Không | — |
| created_at | timestamp with time zone | Không | now() |
| released_at | timestamp with time zone | Có | — |

```sql
leasing_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
resource_allocations_allocation_kind_check: CHECK (((allocation_kind)::text = ANY ((ARRAY['RENTAL'::character varying, 'MAINTENANCE_BLOCK'::character varying])::text[])))
resource_allocations_allocation_kind_not_null: NOT NULL allocation_kind
resource_allocations_blocked_by_fkey: FOREIGN KEY (blocked_by) REFERENCES rental.users(id)
resource_allocations_booking_id_room_id_fkey: FOREIGN KEY (booking_id, room_id) REFERENCES rental.bookings(id, room_id)
resource_allocations_check: CHECK ((end_date > start_date))
resource_allocations_check1: CHECK (((((allocation_kind)::text = 'RENTAL'::text) AND (num_nonnulls(booking_id, contract_id) = 1) AND ((status)::text <> 'BLOCKED'::text) AND (block_reason IS NULL) AND (blocked_by IS NULL)) OR (((allocation_kind)::text = 'MAINTENANCE_BLOCK'::text) AND (num_nonnulls(booking_id, contract_id, contract_tenant_id) = 0) AND ((status)::text = ANY ((ARRAY['BLOCKED'::character varying, 'RELEASED'::character varying])::text[])) AND (NULLIF(btrim(block_reason), ''::text) IS NOT NULL) AND (blocked_by IS NOT NULL))))
resource_allocations_check2: CHECK (((contract_tenant_id IS NULL) OR (contract_id IS NOT NULL)))
resource_allocations_check3: CHECK ((((status)::text <> 'HELD'::text) OR (expires_at IS NOT NULL)))
resource_allocations_contract_id_room_id_fkey: FOREIGN KEY (contract_id, room_id) REFERENCES rental.contracts(id, room_id)
resource_allocations_contract_tenant_id_contract_id_fkey: FOREIGN KEY (contract_tenant_id, contract_id) REFERENCES rental.contract_tenants(id, contract_id)
resource_allocations_created_at_not_null: NOT NULL created_at
resource_allocations_end_date_not_null: NOT NULL end_date
resource_allocations_id_not_null: NOT NULL id
resource_allocations_pkey: PRIMARY KEY (id)
resource_allocations_room_id_fkey: FOREIGN KEY (room_id) REFERENCES rental.rooms(id)
resource_allocations_room_id_not_null: NOT NULL room_id
resource_allocations_room_slot_id_room_id_fkey: FOREIGN KEY (room_slot_id, room_id) REFERENCES rental.room_slots(id, room_id)
resource_allocations_start_date_not_null: NOT NULL start_date
resource_allocations_status_check: CHECK (((status)::text = ANY ((ARRAY['HELD'::character varying, 'LEASED'::character varying, 'BLOCKED'::character varying, 'RELEASED'::character varying])::text[])))
resource_allocations_status_not_null: NOT NULL status
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_allocation_conflict ON rental.resource_allocations USING btree (room_id, start_date, end_date) WHERE ((status)::text <> 'RELEASED'::text);
CREATE UNIQUE INDEX resource_allocations_pkey ON rental.resource_allocations USING btree (id);
```

## roles — Phase 1

Danh mục 5 role ứng dụng. Module: `identity`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| code | character varying(20) | Không | — |

```sql
roles_code_check: CHECK (((code)::text = ANY ((ARRAY['USER'::character varying, 'OWNER'::character varying, 'MANAGER'::character varying, 'TENANT'::character varying, 'ADMIN'::character varying])::text[])))
roles_code_key: UNIQUE (code)
roles_code_not_null: NOT NULL code
roles_id_not_null: NOT NULL id
roles_pkey: PRIMARY KEY (id)
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX roles_code_key ON rental.roles USING btree (code);
CREATE UNIQUE INDEX roles_pkey ON rental.roles USING btree (id);
```

## room_service_charges — Phase 1

Phí theo segment, chỉ số, giá snapshot. Module: `utilities`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_service_id | uuid | Không | — |
| room_id | uuid | Không | — |
| billing_month | date | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| segment_number | integer | Không | — |
| meter_reference | text | Có | — |
| segment_reason | character varying(20) | Không | — |
| start_reading | numeric(19,4) | Có | — |
| end_reading | numeric(19,4) | Có | — |
| quantity | numeric(19,4) | Không | — |
| unit_price | numeric(19,4) | Không | — |
| total_amount | numeric(19,0) | Không | — |
| calculation_snapshot | jsonb | Không | — |
| evidence_refs | jsonb | Không | '[]'::jsonb |
| status | character varying(20) | Không | — |
| finalized_by | uuid | Có | — |
| finalized_at | timestamp with time zone | Có | — |

```sql
financial_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
room_service_charges_billing_month_check: CHECK ((EXTRACT(day FROM billing_month) = (1)::numeric))
room_service_charges_billing_month_not_null: NOT NULL billing_month
room_service_charges_calculation_snapshot_not_null: NOT NULL calculation_snapshot
room_service_charges_check: CHECK ((end_date > start_date))
room_service_charges_check1: CHECK ((num_nonnulls(start_reading, end_reading) = ANY (ARRAY[0, 2])))
room_service_charges_check2: CHECK (((start_reading IS NULL) OR ((start_reading >= (0)::numeric) AND (end_reading >= start_reading) AND (quantity = (end_reading - start_reading)))))
room_service_charges_check3: CHECK ((total_amount = round((quantity * unit_price))))
room_service_charges_check4: CHECK (((start_date >= billing_month) AND (end_date <= ((billing_month + '1 mon'::interval))::date)))
room_service_charges_check5: CHECK ((((status)::text <> 'FINALIZED'::text) OR ((finalized_by IS NOT NULL) AND (finalized_at IS NOT NULL))))
room_service_charges_end_date_not_null: NOT NULL end_date
room_service_charges_evidence_refs_not_null: NOT NULL evidence_refs
room_service_charges_finalized_by_fkey: FOREIGN KEY (finalized_by) REFERENCES rental.users(id)
room_service_charges_id_not_null: NOT NULL id
room_service_charges_id_room_id_key: UNIQUE (id, room_id)
room_service_charges_pkey: PRIMARY KEY (id)
room_service_charges_quantity_check: CHECK ((quantity >= (0)::numeric))
room_service_charges_quantity_not_null: NOT NULL quantity
room_service_charges_room_id_not_null: NOT NULL room_id
room_service_charges_room_service_id_billing_month_segment__key: UNIQUE (room_service_id, billing_month, segment_number)
room_service_charges_room_service_id_not_null: NOT NULL room_service_id
room_service_charges_room_service_id_room_id_fkey: FOREIGN KEY (room_service_id, room_id) REFERENCES rental.room_services(id, room_id)
room_service_charges_segment_number_check: CHECK ((segment_number > 0))
room_service_charges_segment_number_not_null: NOT NULL segment_number
room_service_charges_segment_reason_check: CHECK (((segment_reason)::text = ANY ((ARRAY['REGULAR'::character varying, 'METER_CHANGE'::character varying, 'METER_RESET'::character varying, 'PRICE_CHANGE'::character varying, 'CORRECTION'::character varying])::text[])))
room_service_charges_segment_reason_not_null: NOT NULL segment_reason
room_service_charges_start_date_not_null: NOT NULL start_date
room_service_charges_status_check: CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'FINALIZED'::character varying, 'CANCELLED'::character varying])::text[])))
room_service_charges_status_not_null: NOT NULL status
room_service_charges_total_amount_check: CHECK ((total_amount >= (0)::numeric))
room_service_charges_total_amount_not_null: NOT NULL total_amount
room_service_charges_unit_price_check: CHECK ((unit_price >= (0)::numeric))
room_service_charges_unit_price_not_null: NOT NULL unit_price
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_charge_period ON rental.room_service_charges USING btree (room_id, billing_month);
CREATE UNIQUE INDEX room_service_charges_id_room_id_key ON rental.room_service_charges USING btree (id, room_id);
CREATE UNIQUE INDEX room_service_charges_pkey ON rental.room_service_charges USING btree (id);
CREATE UNIQUE INDEX room_service_charges_room_service_id_billing_month_segment__key ON rental.room_service_charges USING btree (room_service_id, billing_month, segment_number);
```

## room_services — Phase 1

Lịch sử cấu hình và giá dịch vụ phòng. Module: `utilities`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| branch_id | uuid | Không | — |
| service_id | uuid | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| unit_price | numeric(19,4) | Không | — |
| billing_config | jsonb | Không | '{}'::jsonb |
| status | character varying(20) | Không | — |

```sql
room_services_billing_config_not_null: NOT NULL billing_config
room_services_branch_id_not_null: NOT NULL branch_id
room_services_check: CHECK ((end_date > start_date))
room_services_end_date_not_null: NOT NULL end_date
room_services_id_not_null: NOT NULL id
room_services_id_room_id_key: UNIQUE (id, room_id)
room_services_pkey: PRIMARY KEY (id)
room_services_room_id_branch_id_fkey: FOREIGN KEY (room_id, branch_id) REFERENCES rental.rooms(id, branch_id)
room_services_room_id_not_null: NOT NULL room_id
room_services_service_id_branch_id_fkey: FOREIGN KEY (service_id, branch_id) REFERENCES rental.services(id, branch_id)
room_services_service_id_not_null: NOT NULL service_id
room_services_start_date_not_null: NOT NULL start_date
room_services_status_check: CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[])))
room_services_status_not_null: NOT NULL status
room_services_unit_price_check: CHECK ((unit_price >= (0)::numeric))
room_services_unit_price_not_null: NOT NULL unit_price
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_room_services_room ON rental.room_services USING btree (room_id, start_date, end_date);
CREATE UNIQUE INDEX room_services_id_room_id_key ON rental.room_services USING btree (id, room_id);
CREATE UNIQUE INDEX room_services_pkey ON rental.room_services USING btree (id);
```

## room_slot_occupancies — Phase 1

Khoảng cư trú thực tế. Module: `leasing`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| room_slot_id | uuid | Có | — |
| contract_tenant_id | uuid | Không | — |
| start_date | date | Không | — |
| end_date | date | Không | — |
| created_at | timestamp with time zone | Không | now() |

```sql
leasing_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
room_slot_occupancies_check: CHECK ((end_date > start_date))
room_slot_occupancies_contract_tenant_id_not_null: NOT NULL contract_tenant_id
room_slot_occupancies_contract_tenant_id_room_id_fkey: FOREIGN KEY (contract_tenant_id, room_id) REFERENCES rental.contract_tenants(id, room_id)
room_slot_occupancies_created_at_not_null: NOT NULL created_at
room_slot_occupancies_end_date_not_null: NOT NULL end_date
room_slot_occupancies_id_not_null: NOT NULL id
room_slot_occupancies_pkey: PRIMARY KEY (id)
room_slot_occupancies_room_id_not_null: NOT NULL room_id
room_slot_occupancies_room_slot_id_room_id_fkey: FOREIGN KEY (room_slot_id, room_id) REFERENCES rental.room_slots(id, room_id)
room_slot_occupancies_start_date_not_null: NOT NULL start_date
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_occupancy_member ON rental.room_slot_occupancies USING btree (contract_tenant_id);
CREATE INDEX ix_occupancy_room_dates ON rental.room_slot_occupancies USING btree (room_id, start_date, end_date);
CREATE UNIQUE INDEX room_slot_occupancies_pkey ON rental.room_slot_occupancies USING btree (id);
```

## room_slots — Phase 1

Slot thuộc Room. Module: `property`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| room_id | uuid | Không | — |
| code | character varying(50) | Không | — |
| name | text | Không | — |
| status | character varying(20) | Không | — |

```sql
room_slots_code_not_null: NOT NULL code
room_slots_id_not_null: NOT NULL id
room_slots_id_room_id_key: UNIQUE (id, room_id)
room_slots_name_not_null: NOT NULL name
room_slots_pkey: PRIMARY KEY (id)
room_slots_room_id_code_key: UNIQUE (room_id, code)
room_slots_room_id_fkey: FOREIGN KEY (room_id) REFERENCES rental.rooms(id)
room_slots_room_id_not_null: NOT NULL room_id
room_slots_status_check: CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'MAINTENANCE'::character varying, 'INACTIVE'::character varying])::text[])))
room_slots_status_not_null: NOT NULL status
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX room_slots_id_room_id_key ON rental.room_slots USING btree (id, room_id);
CREATE UNIQUE INDEX room_slots_pkey ON rental.room_slots USING btree (id);
CREATE UNIQUE INDEX room_slots_room_id_code_key ON rental.room_slots USING btree (room_id, code);
```

## room_types — Phase 1

Loại phòng trong Branch. Module: `property`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| branch_id | uuid | Không | — |
| code | character varying(50) | Không | — |
| name | text | Không | — |
| default_capacity | integer | Không | — |
| description | text | Có | — |
| active | boolean | Không | true |

```sql
room_types_active_not_null: NOT NULL active
room_types_branch_id_code_key: UNIQUE (branch_id, code)
room_types_branch_id_fkey: FOREIGN KEY (branch_id) REFERENCES rental.branches(id)
room_types_branch_id_not_null: NOT NULL branch_id
room_types_code_not_null: NOT NULL code
room_types_default_capacity_check: CHECK ((default_capacity > 0))
room_types_default_capacity_not_null: NOT NULL default_capacity
room_types_id_branch_id_key: UNIQUE (id, branch_id)
room_types_id_not_null: NOT NULL id
room_types_name_not_null: NOT NULL name
room_types_pkey: PRIMARY KEY (id)
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX room_types_branch_id_code_key ON rental.room_types USING btree (branch_id, code);
CREATE UNIQUE INDEX room_types_id_branch_id_key ON rental.room_types USING btree (id, branch_id);
CREATE UNIQUE INDEX room_types_pkey ON rental.room_types USING btree (id);
```

## rooms — Phase 1

Phòng / chế độ cho thuê / thông tin tìm kiếm. Module: `property`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| branch_id | uuid | Không | — |
| room_type_id | uuid | Không | — |
| code | character varying(50) | Không | — |
| rental_mode | character varying(4) | Không | — |
| capacity | integer | Không | — |
| area_sqm | numeric(10,2) | Có | — |
| floor_number | integer | Có | — |
| reference_price | numeric(19,0) | Không | — |
| description | text | Có | — |
| amenities | text[] | Không | '{}'::text[] |
| image_refs | jsonb | Không | '[]'::jsonb |
| operational_status | character varying(20) | Không | — |
| is_public | boolean | Không | false |
| version | bigint | Không | 0 |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |

```sql
leasing_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
rooms_amenities_not_null: NOT NULL amenities
rooms_area_sqm_check: CHECK ((area_sqm > (0)::numeric))
rooms_branch_id_code_key: UNIQUE (branch_id, code)
rooms_branch_id_fkey: FOREIGN KEY (branch_id) REFERENCES rental.branches(id)
rooms_branch_id_not_null: NOT NULL branch_id
rooms_capacity_check: CHECK ((capacity > 0))
rooms_capacity_not_null: NOT NULL capacity
rooms_code_not_null: NOT NULL code
rooms_created_at_not_null: NOT NULL created_at
rooms_id_branch_id_key: UNIQUE (id, branch_id)
rooms_id_not_null: NOT NULL id
rooms_image_refs_check: CHECK ((jsonb_typeof(image_refs) = 'array'::text))
rooms_image_refs_not_null: NOT NULL image_refs
rooms_is_public_not_null: NOT NULL is_public
rooms_operational_status_check: CHECK (((operational_status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'MAINTENANCE'::character varying, 'INACTIVE'::character varying])::text[])))
rooms_operational_status_not_null: NOT NULL operational_status
rooms_pkey: PRIMARY KEY (id)
rooms_reference_price_check: CHECK ((reference_price >= (0)::numeric))
rooms_reference_price_not_null: NOT NULL reference_price
rooms_rental_mode_check: CHECK (((rental_mode)::text = ANY ((ARRAY['ROOM'::character varying, 'SLOT'::character varying])::text[])))
rooms_rental_mode_not_null: NOT NULL rental_mode
rooms_room_type_id_branch_id_fkey: FOREIGN KEY (room_type_id, branch_id) REFERENCES rental.room_types(id, branch_id)
rooms_room_type_id_not_null: NOT NULL room_type_id
rooms_updated_at_not_null: NOT NULL updated_at
rooms_version_not_null: NOT NULL version
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX rooms_branch_id_code_key ON rental.rooms USING btree (branch_id, code);
CREATE UNIQUE INDEX rooms_id_branch_id_key ON rental.rooms USING btree (id, branch_id);
CREATE UNIQUE INDEX rooms_pkey ON rental.rooms USING btree (id);
```

## services — Phase 1

Danh mục dịch vụ theo Branch. Module: `utilities`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| branch_id | uuid | Không | — |
| code | character varying(50) | Không | — |
| name | text | Không | — |
| unit | character varying(30) | Không | — |
| service_type | character varying(20) | Không | — |
| billing_method | character varying(20) | Không | — |
| reference_price | numeric(19,4) | Không | — |
| mandatory | boolean | Không | — |
| active | boolean | Không | true |

```sql
services_active_not_null: NOT NULL active
services_billing_method_check: CHECK (((billing_method)::text = ANY ((ARRAY['METERED'::character varying, 'PER_PERSON'::character varying, 'PER_ROOM'::character varying, 'FIXED'::character varying, 'PER_USE'::character varying])::text[])))
services_billing_method_not_null: NOT NULL billing_method
services_branch_id_code_key: UNIQUE (branch_id, code)
services_branch_id_fkey: FOREIGN KEY (branch_id) REFERENCES rental.branches(id)
services_branch_id_not_null: NOT NULL branch_id
services_check: CHECK ((((service_type)::text <> ALL ((ARRAY['ELECTRICITY'::character varying, 'WATER'::character varying])::text[])) OR mandatory))
services_code_not_null: NOT NULL code
services_id_branch_id_key: UNIQUE (id, branch_id)
services_id_not_null: NOT NULL id
services_mandatory_not_null: NOT NULL mandatory
services_name_not_null: NOT NULL name
services_pkey: PRIMARY KEY (id)
services_reference_price_check: CHECK ((reference_price >= (0)::numeric))
services_reference_price_not_null: NOT NULL reference_price
services_service_type_check: CHECK (((service_type)::text = ANY ((ARRAY['ELECTRICITY'::character varying, 'WATER'::character varying, 'INTERNET'::character varying, 'PARKING'::character varying, 'CLEANING'::character varying, 'SECURITY'::character varying, 'OTHER'::character varying])::text[])))
services_service_type_not_null: NOT NULL service_type
services_unit_not_null: NOT NULL unit
utilities_integrity: TRIGGER DEFERRABLE INITIALLY DEFERRED
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX services_branch_id_code_key ON rental.services USING btree (branch_id, code);
CREATE UNIQUE INDEX services_id_branch_id_key ON rental.services USING btree (id, branch_id);
CREATE UNIQUE INDEX services_pkey ON rental.services USING btree (id);
```

## threads — Phase 3

Thread cơ bản; không Comment/Suggestion/Report. Module: `community`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| author_user_id | uuid | Không | — |
| title | text | Không | — |
| description | text | Không | — |
| criteria | jsonb | Không | — |
| status | character varying(20) | Không | — |
| expires_at | timestamp with time zone | Có | — |
| closed_at | timestamp with time zone | Có | — |
| moderation_reason | text | Có | — |
| moderated_by | uuid | Có | — |
| moderated_at | timestamp with time zone | Có | — |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |

```sql
threads_author_user_id_fkey: FOREIGN KEY (author_user_id) REFERENCES rental.users(id)
threads_author_user_id_not_null: NOT NULL author_user_id
threads_created_at_not_null: NOT NULL created_at
threads_criteria_not_null: NOT NULL criteria
threads_description_not_null: NOT NULL description
threads_id_not_null: NOT NULL id
threads_moderated_by_fkey: FOREIGN KEY (moderated_by) REFERENCES rental.users(id)
threads_pkey: PRIMARY KEY (id)
threads_status_check: CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'CLOSED'::character varying, 'HIDDEN'::character varying])::text[])))
threads_status_not_null: NOT NULL status
threads_title_not_null: NOT NULL title
threads_updated_at_not_null: NOT NULL updated_at
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_threads_status_time ON rental.threads USING btree (status, created_at DESC);
CREATE UNIQUE INDEX threads_pkey ON rental.threads USING btree (id);
```

## user_roles — Phase 1

User N–N Role. Module: `identity`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| user_id | uuid | Không | — |
| role_id | uuid | Không | — |
| assigned_by | uuid | Có | — |
| assigned_at | timestamp with time zone | Không | now() |

```sql
user_roles_assigned_at_not_null: NOT NULL assigned_at
user_roles_assigned_by_fkey: FOREIGN KEY (assigned_by) REFERENCES rental.users(id)
user_roles_pkey: PRIMARY KEY (user_id, role_id)
user_roles_role_id_fkey: FOREIGN KEY (role_id) REFERENCES rental.roles(id)
user_roles_role_id_not_null: NOT NULL role_id
user_roles_user_id_fkey: FOREIGN KEY (user_id) REFERENCES rental.users(id)
user_roles_user_id_not_null: NOT NULL user_id
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE INDEX ix_roles_reverse ON rental.user_roles USING btree (role_id);
CREATE UNIQUE INDEX user_roles_pkey ON rental.user_roles USING btree (user_id, role_id);
```

## users — Phase 1

Hồ sơ cá nhân và tài khoản chung; auth/Owner Verification tối giản. Module: `identity`.

| Cột | Kiểu | NULL | Default |
|---|---|---|---|
| id | uuid | Không | gen_random_uuid() |
| email | character varying(320) | Có | — |
| phone | character varying(32) | Có | — |
| password_hash | text | Có | — |
| full_name | character varying(255) | Không | — |
| status | character varying(24) | Không | — |
| date_of_birth | date | Có | — |
| gender | character varying(30) | Có | — |
| identity_document_public_id | text | Có | — |
| identity_document_resource_type | character varying(20) | Có | — |
| owner_verification_status | character varying(20) | Không | 'NOT_SUBMITTED'::character varying |
| owner_document_public_id | text | Có | — |
| owner_document_resource_type | character varying(20) | Có | — |
| owner_reviewed_by | uuid | Có | — |
| owner_reviewed_at | timestamp with time zone | Có | — |
| owner_review_reason | text | Có | — |
| verification_purpose | character varying(24) | Có | — |
| verification_secret_hash | text | Có | — |
| verification_expires_at | timestamp with time zone | Có | — |
| verification_attempts | integer | Không | 0 |
| verification_max_attempts | integer | Có | — |
| verification_consumed_at | timestamp with time zone | Có | — |
| failed_login_count | integer | Không | 0 |
| last_failed_login_at | timestamp with time zone | Có | — |
| refresh_token_hash | text | Có | — |
| refresh_token_expires_at | timestamp with time zone | Có | — |
| email_verified_at | timestamp with time zone | Có | — |
| phone_verified_at | timestamp with time zone | Có | — |
| locked_until | timestamp with time zone | Có | — |
| auth_version | bigint | Không | 0 |
| created_at | timestamp with time zone | Không | now() |
| updated_at | timestamp with time zone | Không | now() |
| notification_preferences | jsonb | Không | '{}'::jsonb |

```sql
users_auth_version_not_null: NOT NULL auth_version
users_check: CHECK ((((status)::text = 'INVITED'::text) OR (email IS NOT NULL) OR (phone IS NOT NULL)))
users_check1: CHECK ((((status)::text <> 'ACTIVE'::text) OR ((password_hash IS NOT NULL) AND (password_hash <> ''::text) AND (((email IS NOT NULL) AND (email_verified_at IS NOT NULL)) OR ((phone IS NOT NULL) AND (phone_verified_at IS NOT NULL))))))
users_check2: CHECK ((num_nonnulls(refresh_token_hash, refresh_token_expires_at) = ANY (ARRAY[0, 2])))
users_check3: CHECK ((((verification_secret_hash IS NULL) AND (verification_purpose IS NULL) AND (verification_expires_at IS NULL) AND (verification_max_attempts IS NULL) AND (verification_consumed_at IS NULL)) OR ((verification_secret_hash IS NOT NULL) AND (verification_purpose IS NOT NULL) AND (verification_expires_at IS NOT NULL) AND (verification_max_attempts IS NOT NULL))))
users_check4: CHECK ((((owner_verification_status)::text = 'NOT_SUBMITTED'::text) OR ((owner_document_public_id IS NOT NULL) AND (owner_document_resource_type IS NOT NULL))))
users_check5: CHECK ((((owner_verification_status)::text <> ALL ((ARRAY['APPROVED'::character varying, 'REJECTED'::character varying])::text[])) OR ((owner_reviewed_by IS NOT NULL) AND (owner_reviewed_at IS NOT NULL) AND (owner_review_reason IS NOT NULL))))
users_created_at_not_null: NOT NULL created_at
users_email_check: CHECK (((email IS NULL) OR (((email)::text = lower(btrim((email)::text))) AND ((email)::text <> ''::text))))
users_failed_login_count_check: CHECK ((failed_login_count >= 0))
users_failed_login_count_not_null: NOT NULL failed_login_count
users_full_name_not_null: NOT NULL full_name
users_id_not_null: NOT NULL id
users_notification_preferences_check: CHECK ((jsonb_typeof(notification_preferences) = 'object'::text))
users_notification_preferences_not_null: NOT NULL notification_preferences
users_owner_reviewed_by_fkey: FOREIGN KEY (owner_reviewed_by) REFERENCES rental.users(id)
users_owner_verification_status_check: CHECK (((owner_verification_status)::text = ANY ((ARRAY['NOT_SUBMITTED'::character varying, 'PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
users_owner_verification_status_not_null: NOT NULL owner_verification_status
users_phone_check: CHECK (((phone IS NULL) OR ((phone)::text ~ '^\+[1-9][0-9]{7,14}$'::text)))
users_pkey: PRIMARY KEY (id)
users_refresh_token_hash_check: CHECK ((refresh_token_hash ~ '^[0-9a-f]{64}$'::text))
users_status_check: CHECK (((status)::text = ANY ((ARRAY['INVITED'::character varying, 'PENDING_VERIFICATION'::character varying, 'ACTIVE'::character varying, 'LOCKED'::character varying, 'DISABLED'::character varying])::text[])))
users_status_not_null: NOT NULL status
users_updated_at_not_null: NOT NULL updated_at
users_verification_attempts_check: CHECK ((verification_attempts >= 0))
users_verification_attempts_not_null: NOT NULL verification_attempts
users_verification_max_attempts_check: CHECK ((verification_max_attempts > 0))
users_verification_purpose_check: CHECK (((verification_purpose)::text = ANY ((ARRAY['EMAIL'::character varying, 'PHONE'::character varying, 'PASSWORD_RESET'::character varying, 'INVITATION'::character varying, 'CONTRACT_CONFIRMATION'::character varying])::text[])))
users_verification_secret_hash_check: CHECK ((verification_secret_hash ~ '^[0-9a-f]{64}$'::text))
```

Index (bao gồm index do PK/UNIQUE tự tạo):

```sql
CREATE UNIQUE INDEX uq_user_email ON rental.users USING btree (email) WHERE (email IS NOT NULL);
CREATE UNIQUE INDEX uq_user_phone ON rental.users USING btree (phone) WHERE (phone IS NOT NULL);
CREATE UNIQUE INDEX users_pkey ON rental.users USING btree (id);
```
