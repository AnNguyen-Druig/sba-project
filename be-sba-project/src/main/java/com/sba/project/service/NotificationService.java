package com.sba.project.service;
import com.sba.project.dto.response.*;
import com.sba.project.enums.NotificationType;
import com.sba.project.event.NotificationEvent;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
public interface NotificationService {
    void createNotification(NotificationEvent event);
    PageResponse<NotificationResponse> getNotifications(UUID recipientId, Boolean isRead, NotificationType type, Pageable pageable);
    NotificationResponse markAsRead(UUID notificationId, UUID recipientId);
    void markAllAsRead(UUID recipientId);
    long countUnread(UUID recipientId);
}
