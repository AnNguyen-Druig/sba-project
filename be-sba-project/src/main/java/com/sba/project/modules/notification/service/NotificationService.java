package com.sba.project.modules.notification.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.notification.dto.request.BulkNotificationRequest;
import com.sba.project.modules.notification.dto.request.MarkNotificationReadRequest;
import com.sba.project.modules.notification.dto.request.SendNotificationRequest;
import com.sba.project.modules.notification.dto.response.NotificationResponse;
import com.sba.project.modules.notification.dto.response.UnreadCountResponse;
import com.sba.project.modules.notification.enums.NotificationChannel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for notification operations.
 * Implements RULE 02 and RULE 04.
 */
public interface NotificationService {

    /**
     * Send a single notification to a recipient.
     * Task: [P1-123]
     *
     * @param request the send notification payload
     * @return created notification response
     */
    NotificationResponse sendNotification(SendNotificationRequest request);

    /**
     * Send bulk notifications to multiple recipients.
     * Task: [P1-124]
     *
     * @param request the bulk notification payload
     * @return list of created notification responses
     */
    List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request);

    /**
     * Retrieve notifications for the current authenticated user with optional read/unread filter.
     * Task: [P1-125]
     *
     * @param userId     recipient user ID
     * @param readFilter true for read, false for unread, null for all
     * @param pageable   pagination request
     * @return paged notification response
     */
    PagedResponse<NotificationResponse> getMyNotifications(UUID userId, Boolean readFilter, Pageable pageable);

    /**
     * Get unread notifications count for a user.
     * Task: [P1-125]
     *
     * @param userId recipient user ID
     * @return unread count response
     */
    UnreadCountResponse getUnreadCount(UUID userId);

    /**
     * Mark specific notifications as read for a user.
     * Task: [P1-125]
     *
     * @param userId  recipient user ID
     * @param request list of notification IDs to mark as read
     * @return list of updated notification responses
     */
    List<NotificationResponse> markAsRead(UUID userId, MarkNotificationReadRequest request);

    /**
     * Mark all unread notifications as read for a user.
     * Task: [P1-125]
     *
     * @param userId recipient user ID
     */
    void markAllAsRead(UUID userId);

    /**
     * Check if a duplicate notification exists for the given user, eventKey, type, and channel.
     * Task: [P1-124]
     *
     * @param recipientUserId recipient user ID
     * @param eventKey        deduplication event key
     * @param type            notification type
     * @param channel         delivery channel
     * @return true if duplicate exists, false otherwise
     */
    boolean isDuplicate(UUID recipientUserId, String eventKey, String type, NotificationChannel channel);

    /**
     * Retrieve a specific notification by ID for the user.
     * Task: [P1-125]
     *
     * @param userId         recipient user ID
     * @param notificationId notification ID
     * @return notification response
     */
    NotificationResponse getNotificationById(UUID userId, UUID notificationId);
}
