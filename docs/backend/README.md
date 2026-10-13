# Backend rental — nền tảng Modular Monolith

Branch: `refactor/rental-schema`. Java target **21**, Spring Boot **4.1.1**, Hibernate **7.4.5.Final**. Đây là nền tảng entity/configuration để nhóm ghép nghiệp vụ sau; các API và workflow cũ không được triển khai lại trong bước này.

## Cấu trúc

```text
be-sba-project/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/sba/project/
    │   │   ├── SbaProjectApplication.java
    │   │   ├── common/
    │   │   │   ├── base/{BaseEntity,ApiResponse,PagedResponse}.java
    │   │   │   ├── audit/entity/AuditLog.java
    │   │   │   ├── config/
    │   │   │   ├── exceptions/
    │   │   │   ├── utils/
    │   │   │   └── security/
    │   │   └── modules/
    │   │       ├── identity/
    │   │       ├── property/
    │   │       ├── leasing/
    │   │       ├── utilities/
    │   │       ├── billing/
    │   │       ├── payment/
    │   │       ├── maintenance/
    │   │       ├── notification/
    │   │       ├── reporting/
    │   │       └── community/
    │   └── resources/{application.yml,application-local.yml}
    └── test/java/com/sba/project/
```

Mỗi module có `entity`, `enums`, `mapper`, `dto/request`, `dto/response`, `controller`, `repository`, `specifications`, `service/impl`. Package chưa có Java class được giữ bằng `.gitkeep`, kể cả `reporting`. Không có package `features` hoặc package Java `docs`.

Xem [checklist đủ 34 entity](ENTITY_MAPPING.md), [quy tắc code](../coding_rules.md), [DDL/catalog đã chốt](../database/rental/README.md) và [biên bản kiểm thử](VALIDATION.md).

## Quyết định mapping

- Mọi bảng dùng schema `rental`; **483 cột** được ánh xạ, gồm các cột bổ sung Phase 2/3. SQL, constraint, index, function, trigger và view của `rental` được giữ nguyên.
- UUID id đơn được sinh bằng `GenerationType.UUID`, tương thích kiểu UUID và default `gen_random_uuid()` khi insert bằng SQL. Hai khóa ghép dùng `@EmbeddedId`: `UserRoleId(userId, roleId)`, `OwnerManagerId(ownerId, managerId)`.
- FK khác module là UUID scalar; tuple FK nhiều cột cũng dùng các scalar tương ứng, để database kiểm tra đầy đủ owner/branch/room/member. FK đơn trong cùng module có navigation `@ManyToOne(LAZY)` chỉ đọc. **Gán trường UUID (hoặc embedded id) để ghi FK**; navigation không đồng bộ UUID khi gán bằng builder, và cần refresh nếu đổi UUID trên entity đang managed. Không có cascade hoặc orphan removal.
- `Instant` cho mọi `timestamptz`, `LocalDate` cho date; `BigDecimal` giữ precision/scale của SQL. JSONB dùng `tools.jackson.databind.JsonNode` và `@JdbcTypeCode(SqlTypes.JSON)`, `rooms.amenities` dùng `String[]` và `SqlTypes.ARRAY`. Không chuyển JSONB thành TEXT.
- 48 enum thuộc module sở hữu, dùng `EnumType.STRING`, khớp CHECK; trường mở như `gender`, `notifications.type`, `report_records.status_after` giữ String. Enum cùng ý nghĩa trong một module được dùng lại. Không import enum nội bộ của module khác chỉ để giảm trùng tên.
- **BaseEntity đã điều chỉnh theo xác nhận của người dùng**: bỏ `isDeleted`, đổi `LocalDateTime` thành `Instant`, dùng lifecycle callback để khởi tạo/cập nhật thời gian; không cần bật thêm Spring Data auditing. Chỉ 9 entity có đủ hai cột kế thừa: User, Branch, Room, Appointment, Booking, Contract, ContractTenant, ReportRequest, Thread. Các bảng còn lại tự ánh xạ cột thực tế, không thêm soft-delete hoặc id chung.
- Default hằng số của database có giá trị khởi tạo Java tương ứng và `@Builder.Default`. Timestamp insert có callback khi giá trị null. Đây là default kỹ thuật, không thay thế kiểm tra nghiệp vụ/constraint.
- `Branch.version` và `Room.version` là counter do `lock_financial_branch()` / `lock_room_write()` cập nhật khi ghi bảng liên quan: `insertable=false, updatable=false`, **không `@Version`**. Refresh/clear persistence context sau các ghi liên quan nếu cần đọc giá trị mới; phiên bản trong memory có thể cũ.
- Những bigint `version` còn lại và `users.auth_version` là scalar ghi được, không tự tăng bởi JPA. `BranchPolicy.version` là số phiên bản policy. **Chưa có optimistic locking ứng dụng**: khi ghép nghiệp vụ phải thiết kế CAS (`UPDATE ... WHERE version = expected`) hoặc lock phù hợp; không mặc định `save()` chống mất cập nhật. Retry serialization/deadlock phải bao phủ toàn transaction.
- Audit do trigger quản lý. `AuditLog` dùng `@Immutable`; không có service ghi audit. Entity immutable vẫn không thay thế quyền database.
- `User` không có `toString()` chứa thuộc tính; password/refresh/challenge hash có `@JsonIgnore`. Không trả entity trực tiếp qua API khi ghép nghiệp vụ.

`ApiResponse<T>` giữ `status/message/data` và hai hàm `success`, bổ sung `code` để thống nhất với GlobalExceptionHandler: thành công `code=0`, lỗi ứng dụng từ `1000`. Xem [contract response và mã lỗi](ERROR_RESPONSES.md).

`PagedResponse<T>` giữ nguyên nội dung được cung cấp, chỉ đổi package: **number bắt đầu từ 1**, trong khi `PageRequest` bắt đầu từ 0; các hàm next/previous có thể trả null tại biên. Trước khi dùng trong API cần thống nhất adapter phân trang và validate `pageSize > 0`; không truyền trực tiếp PagedResponse như một Spring Page có quy ước zero-based.

## Cấu hình và chạy local

`application.yml`: datasource từ `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; `ddl-auto=validate`, `default_schema=rental`, timezone UTC, `open-in-view=false`, SQL init `never`, Flyway tắt. Không tự tạo/sửa bảng, seed hay chạy lại SQL.

`application-local.yml` trong **src/main/resources** chỉ có override log/profile, không có secret, được Git theo dõi. `.env` thật và file cấu hình local ở ngoài resources tiếp tục bị ignore; không bị ghi đè.

1. Dùng [`.env.example`](../../.env.example) làm danh sách biến. Lấy JDBC host/port/user từ mục **Connect** của Supabase cho kết nối PostgreSQL direct hoặc session pooler phù hợp mạng hiện tại. Không dùng Project API URL làm JDBC URL. Giữ SSL theo connection details thực tế; không tự suy hostname/user.
2. IntelliJ: tạo Spring Boot Run Configuration cho `SbaProjectApplication`, đặt **Environment variables** gồm `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE=local`. `.env` chỉ tồn tại không khiến Spring tự nạp; dùng tính năng nạp env của IDE nếu có, hoặc nhập biến vào Run Configuration.
3. Nếu đã có biến môi trường trong PowerShell, từ root repository chạy:

   ```powershell
   $env:SPRING_PROFILES_ACTIVE = 'local'
   mvn -f be-sba-project/pom.xml spring-boot:run '-Dspring-boot.run.arguments=--spring.config.location=classpath:/application.yml,classpath:/application-local.yml'
   ```

   `spring.config.location` ở lệnh này loại cấu hình local cũ bên ngoài classpath khỏi lần chạy. Nó tránh nạp nhầm `be-sba-project/application-local.yml` của backend cũ; file cũ vẫn được giữ nguyên. Khi chạy IDE, có thể dùng cùng program argument `--spring.config.location=classpath:/application.yml,classpath:/application-local.yml`.
4. Chỉ chạy với môi trường Supabase test/dev được cho phép. Khi Hibernate tạo EntityManagerFactory với `validate` thành công mới xác nhận validation database thật; compile hoặc test offline không đủ chứng minh điều này.

Login PostgreSQL của backend phải có quyền schema/table/function phù hợp. Nếu bị `permission denied`, đối chiếu [09_access.sql](../database/rental/09_access.sql) và nhờ owner review quyền theo thao tác cần thiết. Không dùng API key thay password PostgreSQL, không expose `rental`, không cấp quyền cho `anon`/`authenticated` để sửa lỗi JDBC. Không có GRANT/ALTER ROLE nào được chạy trong task này.

Spring Security dependency được giữ để chuẩn bị ghép auth. Chưa có JWT workflow, controller hay cấu hình `permitAll`; cấu hình bảo vệ mặc định của Spring vẫn áp dụng khi chạy ứng dụng web. Không coi foundation này là ứng dụng nghiệp vụ hoàn chỉnh.

## Kiểm tra cục bộ và ghép nghiệp vụ

```powershell
mvn -f be-sba-project/pom.xml clean test
```

Tests dùng Hibernate metadata và Spring context với DataSource mock, không đọc credentials hoặc mở kết nối database. Test bootstrap vô hiệu hóa schema validation **chỉ trong test**. Production/local YAML vẫn là `validate`. Maven Wrapper được giữ nguyên; xem biên bản kiểm thử nếu môi trường hiện tại không chạy được wrapper.

Tiếp theo: cấu hình môi trường → validation Supabase test/dev → ghép từng nghiệp vụ theo module → bổ sung authorization và transaction → DTO/MapStruct/controller → kiểm thử tích hợp. Không cherry-pick các entity, cấu hình public-schema hoặc Flyway profile cũ đè lên foundation mới. Các view tài chính dùng projection/native query sau này, không thêm entity ghi cho view.

## Tài liệu và mã cũ

Tài liệu backend cũ chuyển về [docs/legacy/backend](../legacy/backend/README.md), đánh dấu Legacy và cập nhật link bị ảnh hưởng. Những SQL migration cũ trong resources giữ nguyên như tài nguyên lịch sử, không chạy khi startup vì dependency/profile Flyway đã bỏ. Script kiểm thử migration cũ có guard dừng trên foundation mới; chỉ phục vụ checkout cũ với các test/nghiệp vụ tương ứng.

Working tree cũ được giữ trong stash `5569ec1a71811e154f5c5607b6c4639dca00ff83` (nhãn `backup before rental foundation refactor 2026-10-13`). **Không pop/apply stash vào branch mới**. Nếu cần phục hồi, dùng checkout/worktree riêng tại commit cha của stash rồi apply bản đó; stash chứa cả file untracked và index cũ. Bản sao source trước thay đổi cũng nằm cục bộ trong `target/rental-refactor-backup` (ignored, không commit). Không chia sẻ bản backup vì có thể chứa cấu hình local.

Nguồn đối chiếu runtime: [Spring Boot 4 — JSON/Jackson](https://docs.spring.io/spring-boot/4.0/reference/features/json.html), [Hibernate — JSON mapping](https://docs.jboss.org/hibernate/orm/7.0/userguide/html_single/Hibernate_User_Guide.html#basic-mapping-json). Tương thích thực tế được kiểm tra trên dependencies của pom, không chỉ dựa vào tài liệu phiên bản khác.
