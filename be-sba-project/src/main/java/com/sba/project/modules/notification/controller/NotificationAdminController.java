package com.sba.project.modules.notification.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.modules.notification.dto.request.BulkNotificationRequest;
import com.sba.project.modules.notification.dto.request.SendNotificationRequest;
import com.sba.project.modules.notification.dto.response.NotificationResponse;
import com.sba.project.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing internal endpoints for inter-module / administrative notification dispatch.
 * Implements [P1-123], [P1-124].
 *
 * TODO: Protect internal endpoints with internal API key / mutual TLS / internal service role check for backend-to-backend communication only.
 */
@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationService notificationService;

    /**
     * Send a single notification.
     * Task: [P1-123]
     *
     * @param request send notification payload
     * @return created notification response wrapped in ApiResponse
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @Valid @RequestBody SendNotificationRequest request) {
        NotificationResponse result = notificationService.sendNotification(request);
        ApiResponse<NotificationResponse> response = ApiResponse.success("Notification sent successfully", result);
        response.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Send bulk notifications to multiple recipients.
     * Task: [P1-124]
     *
     * @param request bulk notification payload
     * @return list of created notification responses wrapped in ApiResponse
     */
    @PostMapping("/send-bulk")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> sendBulkNotification(
            @Valid @RequestBody BulkNotificationRequest request) {
        List<NotificationResponse> result = notificationService.sendBulkNotification(request);
        ApiResponse<List<NotificationResponse>> response = ApiResponse.success("Bulk notifications processed successfully", result);
        response.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
