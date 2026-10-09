package com.sba.project.service;

import com.sba.project.dto.response.*;
import com.sba.project.enums.NotificationType;
import com.sba.project.event.NotificationEvent;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Thông báo trong ứng dụng (BE-04.4)
 */
public interface NotificationService {

    // Tạo thông báo từ event
    void createNotification(NotificationEvent event);

    // Lấy danh sách thông báo
    PageResponse<NotificationResponse> getNotifications(UUID recipientId, Boolean isRead, NotificationType type, Pageable pageable);

    // Đánh dấu đã đọc
    NotificationResponse markAsRead(UUID notificationId, UUID recipientId);

    // Đánh dấu tất cả đã đọc
    void markAllAsRead(UUID recipientId);

    // Đếm chưa đọc
    long countUnread(UUID recipientId);
}
