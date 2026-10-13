package com.sba.project.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/** Stable application codes. HTTP status is separate; never reuse a published code. */
@Getter
public enum ErrorCode {
    // 1000-1099: common request, persistence and infrastructure errors.
    INTERNAL_ERROR(1000, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(1001, "Invalid request", HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(1002, "Request body is missing or malformed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(1003, "Resource not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(1004, "HTTP method is not supported", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_ACCEPTABLE(1005, "Requested response media type is not supported", HttpStatus.NOT_ACCEPTABLE),
    UNSUPPORTED_MEDIA_TYPE(1006, "Request media type is not supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    DATA_CONFLICT(1007, "The operation conflicts with existing data or a data constraint", HttpStatus.CONFLICT),
    CONCURRENT_MODIFICATION(1008, "Data changed concurrently; reload and try again", HttpStatus.CONFLICT),
    SERVICE_UNAVAILABLE(1009, "Service is temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    REQUEST_FAILED(1010, "The request could not be completed", HttpStatus.BAD_REQUEST),
    UPLOAD_TOO_LARGE(1011, "Uploaded file exceeds the allowed size", HttpStatus.PAYLOAD_TOO_LARGE),

    // 1100-1199: identity, authentication and authorization.
    UNAUTHENTICATED(1100, "Authentication is required or has expired", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1101, "You do not have permission to perform this operation", HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS(1102, "Invalid login credentials", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(1103, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1104, "User already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(1105, "Email is already in use", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(1106, "Phone number is already in use", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(1107, "Role not found", HttpStatus.NOT_FOUND),
    USER_ACCOUNT_UNAVAILABLE(1108, "User account is not available for this operation", HttpStatus.FORBIDDEN),
    OWNER_NOT_VERIFIED(1109, "Owner verification is required", HttpStatus.FORBIDDEN),
    VERIFICATION_INVALID(1110, "Verification code is invalid or expired", HttpStatus.BAD_REQUEST),
    EMAIL_REQUIRED(1111, "Email is required", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1112, "Email format is invalid", HttpStatus.BAD_REQUEST),
    PHONE_REQUIRED(1113, "Phone number is required", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1114, "Phone number must use international format, for example +84901234567", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1115, "Password is required", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1116, "Password does not meet the required policy", HttpStatus.BAD_REQUEST),
    NAME_REQUIRED(1117, "Full name is required", HttpStatus.BAD_REQUEST),
    DATE_OF_BIRTH_INVALID(1118, "Date of birth is invalid", HttpStatus.BAD_REQUEST),

    // 1200-1299: property.
    BRANCH_NOT_FOUND(1200, "Branch not found", HttpStatus.NOT_FOUND),
    ROOM_TYPE_NOT_FOUND(1201, "Room type not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_FOUND(1202, "Room not found", HttpStatus.NOT_FOUND),
    ROOM_SLOT_NOT_FOUND(1203, "Room slot not found", HttpStatus.NOT_FOUND),
    RESOURCE_UNAVAILABLE(1204, "Room or slot is unavailable for the requested period", HttpStatus.CONFLICT),
    BRANCH_POLICY_NOT_FOUND(1205, "Branch policy not found", HttpStatus.NOT_FOUND),

    // 1300-1399: leasing.
    APPOINTMENT_NOT_FOUND(1300, "Appointment not found", HttpStatus.NOT_FOUND),
    BOOKING_NOT_FOUND(1301, "Booking not found", HttpStatus.NOT_FOUND),
    BOOKING_EXPIRED(1302, "Booking has expired", HttpStatus.CONFLICT),
    CONTRACT_NOT_FOUND(1303, "Contract not found", HttpStatus.NOT_FOUND),
    CONTRACT_TENANT_NOT_FOUND(1304, "Contract member not found", HttpStatus.NOT_FOUND),
    INVALID_RENTAL_PERIOD(1305, "Rental period is invalid", HttpStatus.BAD_REQUEST),
    LEASING_STATE_CONFLICT(1306, "Current leasing state does not allow this operation", HttpStatus.CONFLICT),

    // 1400-1499: utilities.
    SERVICE_NOT_FOUND(1400, "Rental service not found", HttpStatus.NOT_FOUND),
    ROOM_SERVICE_NOT_FOUND(1401, "Room service configuration not found", HttpStatus.NOT_FOUND),
    SERVICE_CHARGE_NOT_FOUND(1402, "Service charge not found", HttpStatus.NOT_FOUND),
    INVALID_METER_READING(1403, "Meter readings are invalid", HttpStatus.BAD_REQUEST),
    INVALID_COST_ALLOCATION(1404, "Cost allocation does not match the charge", HttpStatus.BAD_REQUEST),

    // 1500-1599: billing.
    INVOICE_NOT_FOUND(1500, "Invoice not found", HttpStatus.NOT_FOUND),
    INVOICE_ALREADY_EXISTS(1501, "An invoice already exists for this member and billing month", HttpStatus.CONFLICT),
    INVOICE_STATE_CONFLICT(1502, "Current invoice state does not allow this operation", HttpStatus.CONFLICT),

    // 1600-1699: payment. Booking hold and prepaid rent are distinct concepts.
    PAYMENT_NOT_FOUND(1600, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_SUPPORTED(1601, "Payment method is not supported", HttpStatus.BAD_REQUEST),
    PAYMENT_ENVIRONMENT_MISMATCH(1602, "Payment environment does not match the target obligation", HttpStatus.BAD_REQUEST),
    PAYMENT_STATE_CONFLICT(1603, "Current payment state does not allow this operation", HttpStatus.CONFLICT),
    INSUFFICIENT_PREPAID_BALANCE(1604, "Available prepaid rent is insufficient", HttpStatus.CONFLICT),
    INVALID_WEBHOOK_SIGNATURE(1605, "Payment webhook signature is invalid", HttpStatus.BAD_REQUEST),

    // 1700-1799: maintenance.
    FURNITURE_NOT_FOUND(1700, "Furniture not found", HttpStatus.NOT_FOUND),
    REPORT_REQUEST_NOT_FOUND(1701, "Maintenance request not found", HttpStatus.NOT_FOUND),
    REPORT_RECORD_NOT_FOUND(1702, "Maintenance record not found", HttpStatus.NOT_FOUND),
    MAINTENANCE_STATE_CONFLICT(1703, "Current maintenance state does not allow this operation", HttpStatus.CONFLICT),

    // 1800-1899: notification; 1900-1999: reporting; 2000-2099: community; 2100-2199: audit.
    NOTIFICATION_NOT_FOUND(1800, "Notification not found", HttpStatus.NOT_FOUND),
    INVALID_REPORT_PERIOD(1900, "Report period is invalid", HttpStatus.BAD_REQUEST),
    THREAD_NOT_FOUND(2000, "Thread not found", HttpStatus.NOT_FOUND),
    THREAD_STATE_CONFLICT(2001, "Current thread state does not allow this operation", HttpStatus.CONFLICT),
    AUDIT_LOG_NOT_FOUND(2100, "Audit log not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
