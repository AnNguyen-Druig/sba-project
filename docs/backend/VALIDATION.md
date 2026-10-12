# Kiểm chứng backend rental foundation

Ngày: 2026-10-13. Branch: `refactor/rental-schema`.

| Kiểm tra | Kết quả và giới hạn |
|---|---|
| Maven compile | PASS — Spring Boot 4.1.1; 88 Java source; target Java 21 |
| JUnit | PASS — 5 tests, 0 failures, 0 errors, 0 skipped |
| Spring context | PASS — profile local, 34 entity, DataSource mock không được gọi; không kết nối DB |
| Hibernate SessionFactory | PASS — Hibernate 7.4.5.Final; metadata access và DDL tắt trong test |
| Bảng/cột | PASS — 34 bảng duy nhất, 483 cột; tên/schema, SQL type, nullable, varchar length, numeric precision/scale khớp catalog |
| Composite PK | PASS — UserRoleId và OwnerManagerId, đúng tập cột và equals/hashCode; thứ tự thuộc tính Hibernate có thể khác thứ tự index của PostgreSQL |
| Enum | PASS — EnumType.STRING, tập giá trị khớp CHECK trong catalog; không dùng enum PostgreSQL |
| JSONB | PASS — metadata JSONB và round-trip JsonNode qua JSON FormatMapper thực tế của Hibernate; chưa round-trip qua JDBC/PostgreSQL |
| SQL ARRAY | PASS ở metadata — rooms.amenities là text[]; JDBC round-trip chưa chạy |
| BaseEntity | PASS — 9 entity kế thừa, không cột is_deleted bị thêm |
| Package structure | PASS — đủ 10 module; 82 package module trống và 4 package common có .gitkeep |
| DDL và frontend | PASS — 10 file SQL rental không thay đổi so với stash trước refactor; frontend không có diff |
| Dữ liệu nhạy cảm | PASS — password/refresh/challenge hash không xuất hiện khi serialize User hoặc toString |
| Hibernate schema validation trên Supabase | **NOT EXECUTED — Database connection/schema validation** |
| API/JWT/authorization/nghiệp vụ/concurrency | **NOT EXECUTED** — không thuộc foundation hiện tại |

Lệnh đã chạy tại root:

```powershell
mvn -f be-sba-project/pom.xml -B -DskipTests compile
mvn -f be-sba-project/pom.xml -B clean test
mvn -f be-sba-project/pom.xml -B test
```

Lần `clean test` đầu compile thành công và phát hiện assertion thứ tự thuộc tính composite key quá chặt. Hibernate sắp thuộc tính theo tên; mapping vẫn đúng cột. Test đã sửa sang so sánh tập cột, lần chạy lại 5/5 PASS. Không đổi schema để làm test pass.

Maven Wrapper Windows đã thử từ root và thư mục backend nhưng dừng ở PowerShell `Cannot index into a null array`. Giữ nguyên wrapper, dùng Maven 3.9.11 cài sẵn. Máy kiểm thử dùng JDK 25.0.1, compiler `--release 21`; runtime JDK 21 riêng chưa được thử. Có warning JDK Unsafe/Mockito; không có lỗi compile/test cuối cùng.

Không gọi Supabase, không chạy GRANT/DDL hoặc kiểm thử CRUD lên database thật. Catalog là snapshot từ bộ DDL đã chốt, không phải introspection Supabase trong lần này. `ddl-auto=validate` trong cấu hình ứng dụng được giữ nguyên; riêng test offline override thành `none` để không chạm database.

Log cục bộ: `target/rental-compile.log`, `target/rental-tests.log`; JUnit XML tại `be-sba-project/target/surefire-reports`. Các file target không commit. Xem [hướng dẫn chạy validation thật](README.md#cấu-hình-và-chạy-local).
