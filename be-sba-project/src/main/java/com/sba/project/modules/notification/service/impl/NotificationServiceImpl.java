package com.sba.project.modules.notification.service.impl;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.notification.dto.request.BulkNotificationRequest;
import com.sba.project.modules.notification.dto.request.MarkNotificationReadRequest;
import com.sba.project.modules.notification.dto.request.SendNotificationRequest;
import com.sba.project.modules.notification.dto.response.NotificationResponse;
import com.sba.project.modules.notification.dto.response.UnreadCountResponse;
import com.sba.project.modules.notification.entity.Notification;
import com.sba.project.modules.notification.enums.NotificationChannel;
import com.sba.project.modules.notification.enums.NotificationDeliveryStatus;
import com.sba.project.modules.notification.mapper.NotificationMapper;
import com.sba.project.modules.notification.repository.NotificationRepository;
import com.sba.project.modules.notification.service.NotificationService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of NotificationService handling notification delivery,
 * deduplication, queries, and read tracking.
 * Implements [P1-123], [P1-124], [P1-125].
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    /**
     * Send a single notification.
     * Task: [P1-123]
     * - Generates eventKey to prevent duplicates
     * - For IN_APP: immediately sets deliveryStatus = SENT and sentAt
     * - For EMAIL/SMS: sets deliveryStatus = PENDING for Phase 3 background workers
     */
    @Override
    @Transactional
    public NotificationResponse sendNotification(SendNotificationRequest request) {
        NotificationChannel channel = request.getChannel() != null ? request.getChannel() : NotificationChannel.IN_APP;
        String eventKey = buildEventKey(request.getType(), request.getReferenceType(), request.getReferenceId());

        if (isDuplicate(request.getRecipientUserId(), eventKey, request.getType(), channel)) {
            log.info("Duplicate notification detected for user: {}, eventKey: {}, type: {}, channel: {}. Skipping creation.",
                    request.getRecipientUserId(), eventKey, request.getType(), channel);
            return notificationRepository.findFirstByRecipientUserIdAndEventKeyAndTypeAndChannel(
                    request.getRecipientUserId(), eventKey, request.getType(), channel)
                    .map(notificationMapper::toResponse)
                    .orElse(null);
        }

        Notification notification = notificationMapper.toEntity(request);
        notification.setEventKey(eventKey);
        notification.setChannel(channel);

        Instant now = Instant.now();
        if (channel == NotificationChannel.IN_APP) {
            notification.setDeliveryStatus(NotificationDeliveryStatus.SENT);
            notification.setSentAt(now);
        } else {
            notification.setDeliveryStatus(NotificationDeliveryStatus.PENDING);
            notification.setNextAttemptAt(now);
        }
        notification.setAttemptCount(0);

        Notification saved = notificationRepository.save(notification);
        log.debug("Created notification id={} for recipient={}", saved.getId(), saved.getRecipientUserId());
        return notificationMapper.toResponse(saved);
    }

    /**
     * Send bulk notifications to multiple recipients.
     * Task: [P1-124]
     * Checks duplicates before creating for each recipient.
     */
    @Override
    @Transactional
    public List<NotificationResponse> sendBulkNotification(BulkNotificationRequest request) {
        List<NotificationResponse> responses = new ArrayList<>();
        if (request.getRecipientUserIds() == null || request.getRecipientUserIds().isEmpty()) {
            return responses;
        }

        for (UUID recipientId : request.getRecipientUserIds()) {
            SendNotificationRequest singleRequest = SendNotificationRequest.builder()
                    .recipientUserId(recipientId)
                    .type(request.getType())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .referenceType(request.getReferenceType())
                    .referenceId(request.getReferenceId())
                    .channel(request.getChannel())
                    .build();

            NotificationResponse response = sendNotification(singleRequest);
            if (response != null) {
                responses.add(response);
            }
        }

        log.info("Processed bulk notification dispatch for {} recipients. {} notifications sent or retrieved.",
                request.getRecipientUserIds().size(), responses.size());
        return responses;
    }

    /**
     * Get paginated notifications for current user with optional read/unread filter.
     * Task: [P1-125]
     */
    @Override
    public PagedResponse<NotificationResponse> getMyNotifications(UUID userId, Boolean readFilter, Pageable pageable) {
        Page<Notification> page;
        if (readFilter == null) {
            page = notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable);
        } else if (Boolean.TRUE.equals(readFilter)) {
            page = notificationRepository.findByRecipientUserIdAndReadAtIsNotNullOrderByCreatedAtDesc(userId, pageable);
        } else {
            page = notificationRepository.findByRecipientUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable);
        }

        List<NotificationResponse> content = notificationMapper.toResponseList(page.getContent());
        return PagedResponse.of(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    /**
     * Get unread notification count for user.
     * Task: [P1-125]
     */
    @Override
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByRecipientUserIdAndReadAtIsNull(userId);
        return new UnreadCountResponse(count);
    }

    /**
     * Mark selected notifications as read for user.
     * Task: [P1-125]
     */
    @Override
    @Transactional
    public List<NotificationResponse> markAsRead(UUID userId, MarkNotificationReadRequest request) {
        if (request.getNotificationIds() == null || request.getNotificationIds().isEmpty()) {
            return List.of();
        }

        List<Notification> notifications = notificationRepository.findByIdInAndRecipientUserId(
                request.getNotificationIds(), userId);

        Instant now = Instant.now();
        List<Notification> toUpdate = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getReadAt() == null) {
                notification.setReadAt(now);
                toUpdate.add(notification);
            }
        }

        if (!toUpdate.isEmpty()) {
            notificationRepository.saveAll(toUpdate);
        }

        return notificationMapper.toResponseList(notifications);
    }

    /**
     * Mark all unread notifications as read for user.
     * Task: [P1-125]
     */
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        int updatedCount = notificationRepository.markAllAsReadByRecipientUserId(userId, Instant.now());
        log.debug("Marked {} notifications as read for user {}", updatedCount, userId);
    }

    /**
     * Check if a duplicate notification exists.
     * Task: [P1-124]
     */
    @Override
    public boolean isDuplicate(UUID recipientUserId, String eventKey, String type, NotificationChannel channel) {
        return notificationRepository.existsByRecipientUserIdAndEventKeyAndTypeAndChannel(
                recipientUserId, eventKey, type, channel);
    }

    /**
     * Get single notification by ID belonging to user.
     * Task: [P1-125]
     */
    @Override
    public NotificationResponse getNotificationById(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndRecipientUserId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        return notificationMapper.toResponse(notification);
    }

    /**
     * Builds deduplication event key.
     * Format:
     * - type:referenceType:referenceId
     * - type:referenceType:UUID (if referenceId is missing)
     * - type:UUID (if referenceType is missing)
     */
    private String buildEventKey(String type, String referenceType, UUID referenceId) {
        if (referenceType != null && !referenceType.isBlank()) {
            if (referenceId != null) {
                return type + ":" + referenceType + ":" + referenceId;
            }
            return type + ":" + referenceType + ":" + UUID.randomUUID();
        }
        return type + ":" + UUID.randomUUID();
    }
}
