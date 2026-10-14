package com.sba.project.modules.notification.controller;

import com.sba.project.common.base.ApiResponse;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.common.security.AuthPrincipal;
import com.sba.project.modules.notification.dto.request.MarkNotificationReadRequest;
import com.sba.project.modules.notification.dto.response.NotificationResponse;
import com.sba.project.modules.notification.dto.response.UnreadCountResponse;
import com.sba.project.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user notification endpoints.
 * Implements [P1-125].
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Retrieve paginated notifications for the current authenticated user.
     * Task: [P1-125]
     *
     * @param read optional boolean filter (true = read, false = unread, null = all)
     * @param page page index (0-based, default: 0)
     * @param size page size (default: 20)
     * @return paginated notification responses wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID currentUserId = getCurrentUserId();
        int pageNumber = Math.max(0, page);
        int pageSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        PagedResponse<NotificationResponse> result = notificationService.getMyNotifications(currentUserId, read, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Retrieve unread notification count for the current authenticated user.
     * Task: [P1-125]
     *
     * @return unread notification count wrapped in ApiResponse
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        UUID currentUserId = getCurrentUserId();
        UnreadCountResponse result = notificationService.getUnreadCount(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Mark specified notifications as read for current user.
     * Task: [P1-125]
     *
     * @param request list of notification IDs
     * @return updated notification responses wrapped in ApiResponse
     */
    @PutMapping("/mark-read")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> markAsRead(
            @Valid @RequestBody MarkNotificationReadRequest request) {
        UUID currentUserId = getCurrentUserId();
        List<NotificationResponse> result = notificationService.markAsRead(currentUserId, request);
        return ResponseEntity.ok(ApiResponse.success("Notifications marked as read", result));
    }

    /**
     * Mark all unread notifications as read for current user.
     * Task: [P1-125]
     *
     * @return success response
     */
    @PutMapping("/mark-all-read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        UUID currentUserId = getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    /**
     * Retrieve single notification by ID for current user.
     * Task: [P1-125]
     *
     * @param notificationId notification ID
     * @return single notification wrapped in ApiResponse
     */
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable UUID notificationId) {
        UUID currentUserId = getCurrentUserId();
        NotificationResponse result = notificationService.getNotificationById(currentUserId, notificationId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Helper method to extract the authenticated user ID from SecurityContextHolder.
     * Throws UNAUTHENTICATED exception if no valid principal is present.
     */
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthPrincipal authPrincipal) {
            return UUID.fromString(authPrincipal.accountId());
        }
        if (principal instanceof String principalStr && !principalStr.equals("anonymousUser")) {
            return UUID.fromString(principalStr);
        }

        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
