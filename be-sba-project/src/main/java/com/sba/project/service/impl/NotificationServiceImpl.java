package com.sba.project.service.impl;
import com.sba.project.dto.response.*;
import com.sba.project.entity.Notification;
import com.sba.project.enums.NotificationType;
import com.sba.project.event.NotificationEvent;
import com.sba.project.exception.BusinessException;
import com.sba.project.exception.ResourceNotFoundException;
import com.sba.project.repository.NotificationRepository;
import com.sba.project.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    @Override
    @EventListener
    public void createNotification(NotificationEvent event) {
        Notification notification = Notification.builder()
                .recipientId(event.getRecipientId())
                .type(event.getType())
                .title(event.getTitle())
                .message(event.getMessage())
                .referenceId(event.getReferenceId())
                .referenceType(event.getReferenceType())
                .branchId(event.getBranchId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("Created notification for user {} type {}", event.getRecipientId(), event.getType());
    }
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(UUID recipientId, Boolean isRead, NotificationType type, Pageable pageable) {
        Page<Notification> page;
        if (type != null) {
            page = notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(recipientId, type, pageable);
        } else if (isRead != null) {
            page = notificationRepository.findByRecipientIdAndIsReadOrderByCreatedAtDesc(recipientId, isRead, pageable);
        } else {
            page = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
        }
        return buildPageResponse(page.map(this::mapToResponse));
    }
    @Override
    public NotificationResponse markAsRead(UUID notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo: " + notificationId));
        if (!notification.getRecipientId().equals(recipientId)) {
            throw new BusinessException("Bạn không có quyền thao tác trên thông báo này");
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);
        return mapToResponse(notification);
    }
    @Override
    public void markAllAsRead(UUID recipientId) {
        int updated = notificationRepository.markAllAsRead(recipientId);
        log.info("Marked {} notifications as read for user {}", updated, recipientId);
    }
    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }
    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.isRead())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .branchId(n.getBranchId())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
    private <T> PageResponse<T> buildPageResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
