package com.sba.project.controller;
import com.sba.project.dto.response.*;
import com.sba.project.enums.NotificationType;
import com.sba.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getNotifications(userId, isRead, type, pageable));
    }

    @PatchMapping("/{notificationId}/read")

    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId,
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, userId));
    }

    @PatchMapping("/read-all")

    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-Id") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> countUnread(@RequestHeader("X-User-Id") UUID userId) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
