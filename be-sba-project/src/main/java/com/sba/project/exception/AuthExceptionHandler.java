package com.sba.project.exception;

import com.sba.project.dto.response.AuthApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Preserves authentication errors without replacing other controller contracts. */
@org.springframework.core.annotation.Order(0)
@RestControllerAdvice(assignableTypes = com.sba.project.controller.AuthController.class)
@Slf4j
public class AuthExceptionHandler {

    /** Converts an authentication failure into its HTTP response. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest()
                .body(AuthApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .timestamp(java.time.LocalDateTime.now())
                        .build());
    }

    /** Converts an authentication failure into its HTTP response. */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<AuthApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(AuthApiResponse.error(ex.getMessage()));
    }

    /** Converts an authentication failure into its HTTP response. */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(AuthApiResponse.error("Invalid email or password"));
    }

    /** Converts an authentication failure into its HTTP response. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.internalServerError().body(AuthApiResponse.error("An unexpected error occurred"));
    }
}
