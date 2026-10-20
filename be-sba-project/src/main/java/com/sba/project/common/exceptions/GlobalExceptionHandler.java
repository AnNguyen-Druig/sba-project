package com.sba.project.common.exceptions;

import com.sba.project.common.base.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** MVC error contract. Security filter errors need their own entry point/access-denied handler. */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        // Only catalog messages are public; custom exception details stay internal.
        return error(ex.getErrorCode() == null ? ErrorCode.INTERNAL_ERROR : ex.getErrorCode());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return error(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return error(ErrorCode.UNAUTHENTICATED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        return error(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        // Do not expose or log SQL messages: they may contain personal data or secrets.
        log.warn("Database constraint rejected an operation ({})", ex.getClass().getSimpleName());
        return error(ErrorCode.DATA_CONFLICT);
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleConcurrency(ConcurrencyFailureException ex) {
        return error(ErrorCode.CONCURRENT_MODIFICATION);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleDatabaseUnavailable(DataAccessResourceFailureException ex) {
        log.error("Database access unavailable ({})", ex.getClass().getSimpleName());
        return error(ErrorCode.SERVICE_UNAVAILABLE);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationError> violations = ex.getBindingResult().getAllErrors().stream()
                .map(e -> new ValidationError(e instanceof org.springframework.validation.FieldError f
                        ? f.getField() : e.getObjectName(), validationCode(e).getCode(), validationCode(e).getMessage()))
                .toList();
        ErrorCode code = ex.getBindingResult().getAllErrors().stream()
                .findFirst().map(this::validationCode).orElse(ErrorCode.INVALID_REQUEST);
        ApiResponse<List<ValidationError>> body = ApiResponse.<List<ValidationError>>error(code);
        body.setData(violations);
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Retain Spring's HTTP status and headers (Allow, Accept, Retry-After, etc.).
        ErrorCode code = ex instanceof HttpMessageNotReadableException
                ? ErrorCode.MALFORMED_REQUEST : codeForStatus(status.value());
        if (status.is5xxServerError()) {
            log.error("Request processing failed ({})", ex.getClass().getName());
        }
        ApiResponse<Void> response = ApiResponse.error(code);
        response.setStatus(status.value());
        return super.handleExceptionInternal(ex, response, headers, status, request);
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException ex) {
        log.debug("Client disconnected before the response completed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (org.springframework.web.util.DisconnectedClientHelper.isClientDisconnectedException(ex)) {
            log.debug("Client disconnected before the response completed");
            return null; // No synthetic success response on a broken connection.
        }
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        log.error("Unhandled request {} {} ({}; root cause: {})",
                request.getMethod(), request.getRequestURI(),
                ex.getClass().getName(), rootCause.getClass().getName());
        log.debug("Unhandled request exception details", ex);
        return error(ErrorCode.INTERNAL_ERROR);
    }

    private ErrorCode validationCode(ObjectError error) {
        String key = error.getDefaultMessage();
        if (key != null) {
            try {
                ErrorCode code = ErrorCode.valueOf(key);
                // A validation annotation must never turn a malformed request into 401/404/500.
                if (code.getHttpStatusCode().value() == 400) return code;
            } catch (IllegalArgumentException ignored) {
                // Standard Bean Validation messages and unknown keys use a safe fallback.
            }
        }
        return ErrorCode.INVALID_REQUEST;
    }

    private ErrorCode codeForStatus(int status) {
        return switch (status) {
            case 400 -> ErrorCode.INVALID_REQUEST;
            case 401 -> ErrorCode.UNAUTHENTICATED;
            case 403 -> ErrorCode.UNAUTHORIZED;
            case 404 -> ErrorCode.RESOURCE_NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> ErrorCode.MEDIA_TYPE_NOT_ACCEPTABLE;
            case 409 -> ErrorCode.DATA_CONFLICT;
            case 413 -> ErrorCode.UPLOAD_TOO_LARGE;
            case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            case 503 -> ErrorCode.SERVICE_UNAVAILABLE;
            default -> status >= 500 ? ErrorCode.INTERNAL_ERROR : ErrorCode.REQUEST_FAILED;
        };
    }

    private ResponseEntity<ApiResponse<Void>> error(ErrorCode code) {
        return ResponseEntity.status(code.getHttpStatusCode()).body(ApiResponse.error(code));
    }

    public record ValidationError(String field, int code, String message) { }
}
