package com.sba.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Keeps the response contract consumed by the authentication frontend. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthApiResponse<T> {

    /** Whether the authentication operation succeeded. */
    private boolean success;
    /** Human-readable operation result. */
    private String message;
    /** Optional response payload. */
    private T data;
    /** Time when the response was created. */
    private LocalDateTime timestamp;

    /** Creates an authentication response for the requested outcome. */
    public static <T> AuthApiResponse<T> success(String message, T data) {
        return AuthApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Creates an authentication response for the requested outcome. */
    public static <T> AuthApiResponse<T> success(String message) {
        return AuthApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /** Creates an authentication response for the requested outcome. */
    public static <T> AuthApiResponse<T> error(String message) {
        return AuthApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
