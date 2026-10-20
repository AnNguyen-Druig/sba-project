# API response và mã lỗi rental

Response thống nhất có `status`, `code`, `message`, `data`:

- `status`: HTTP status, phải khớp status của `ResponseEntity`.
- `code`: mã ứng dụng; `0` cho thành công, từ `1000` cho lỗi. Không dùng HTTP status làm mã lỗi ứng dụng.
- `message`: thông báo công khai, không chứa exception message, SQL, token hoặc dữ liệu người dùng bị từ chối.
- `data`: dữ liệu thành công, danh sách lỗi validation, hoặc null khi không có dữ liệu.

Ví dụ khi không tìm thấy phòng — HTTP **404**:

```json
{
  "status": 404,
  "code": 1202,
  "message": "Room not found",
  "data": null
}
```

## Sử dụng

```java
return ApiResponse.success(responseDto); // HTTP mặc định 200, code 0
throw new AppException(ErrorCode.ROOM_NOT_FOUND);
```

Hai hàm `success(data)` và `success(message, data)` vẫn hoạt động. Khi muốn trả HTTP 201 hoặc status thành công khác, cần đặt `status` trong body khớp `ResponseEntity`; các helper success hiện mặc định 200.

`AppException(ErrorCode, String)` vẫn tồn tại, nhưng handler chỉ trả message từ catalog; custom message là chi tiết nội bộ, không phải nội dung gửi client. Không truyền secret vào exception. Với lỗi không được dự kiến, handler trả `INTERNAL_ERROR`/1000/500; log hiện ghi loại exception, không dump thông điệp có thể chứa dữ liệu nhạy cảm.

## Quy hoạch mã

| Khoảng | Nhóm |
|---|---|
| 1000–1099 | Request, HTTP, lỗi dữ liệu và hạ tầng |
| 1100–1199 | Identity, xác thực và phân quyền |
| 1200–1299 | Property |
| 1300–1399 | Leasing |
| 1400–1499 | Utilities |
| 1500–1599 | Billing |
| 1600–1699 | Payment |
| 1700–1799 | Maintenance |
| 1800–1899 | Notification |
| 1900–1999 | Reporting |
| 2000–2099 | Community |
| 2100–2199 | Audit |

Nguồn chính xác: [ErrorCode.java](../../be-sba-project/src/main/java/com/sba/project/common/exceptions/ErrorCode.java). Không đổi ý nghĩa hoặc tái sử dụng mã đã công bố. Catalog mô tả lỗi có thể dùng khi triển khai nghiệp vụ; không có nghĩa các API/nghiệp vụ đó đã được triển khai.

## Validation và exception MVC

Có thể tham chiếu enum bằng annotation, ví dụ `@NotBlank(message = "NAME_REQUIRED")`. Các lỗi `@Valid @RequestBody` trả danh sách `{field, code, message}` trong `data`; code/message cấp ngoài lấy từ lỗi đầu tiên. Không phụ thuộc thứ tự lỗi để xử lý nhiều field ở frontend.

Message chuẩn của Bean Validation, khóa không hợp lệ, khóa null hoặc khóa có HTTP status khác 400 dùng `INVALID_REQUEST` làm fallback. Handler không đoán min/max từ thứ tự `getArguments()` và không gửi rejected value về client. Catalog không áp đặt chính sách password hoặc bắt buộc đồng thời email/phone; DTO/nghiệp vụ chịu trách nhiệm chọn constraint phù hợp.

`GlobalExceptionHandler` kế thừa `ResponseEntityExceptionHandler` để giữ status/headers chuẩn của Spring cho request thiếu tham số, sai kiểu, JSON lỗi, HTTP method/media type không hỗ trợ, tài nguyên không tồn tại và lỗi validation method. Validation return value của MVC vẫn là lỗi server. Exception MVC được chuyển sang envelope ApiResponse; với status chưa có mã riêng, giữ HTTP status gốc và dùng mã fallback chung. Trường hợp không thể ghi response (đã commit, client ngắt kết nối hoặc không có media type phù hợp), không bảo đảm có JSON body.

Data integrity exception trả `DATA_CONFLICT`/1007/409, không suy đoán lỗi nghiệp vụ bằng cách parse thông điệp SQL. Service nên phát hiện điều kiện nghiệp vụ đã biết và ném AppException cụ thể. Lỗi cập nhật đồng thời trả `CONCURRENT_MODIFICATION`/1008/409; handler không tự retry transaction tài chính.

## Phạm vi bảo vệ

Advice này xử lý exception đi qua Spring MVC. Exception ở Spring Security filter cần cấu hình `AuthenticationEntryPoint` và `AccessDeniedHandler` dùng cùng ApiResponse khi triển khai security; advice không tự chuẩn hóa mọi lỗi từ filter, scheduler hoặc startup database validation.

Các mã promotion, order, cart, glasses, shipping và mã lỗi trùng từ dự án cũ đã được bỏ. Frontend khi được tích hợp phải đọc `data` và `code` theo contract mới, không dùng envelope `result` của backend cũ.

Kiểm thử HTTP contract nằm trong [GlobalExceptionHandlerTests.java](../../be-sba-project/src/test/java/com/sba/project/GlobalExceptionHandlerTests.java); controller trong file đó chỉ phục vụ test, không được đóng gói vào ứng dụng production.

Đã chạy ngày 2026-10-13: `mvn -f be-sba-project/pom.xml -B test` — **BUILD SUCCESS, 13 tests PASS**, gồm 8 tests response/error contract và 5 tests foundation. Không kết nối Supabase. Log cục bộ: `target/error-response-tests.log`.

Tham khảo: [Spring ResponseEntityExceptionHandler](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/mvc/method/annotation/ResponseEntityExceptionHandler.html).
