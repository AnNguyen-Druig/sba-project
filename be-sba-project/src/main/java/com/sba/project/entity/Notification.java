package com.sba.project.entity;

import com.sba.project.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Thông báo trong ứng dụng
 * [P1-123] Tạo thông báo cho các sự kiện nghiệp vụ
 * [P1-124] Gửi đúng người nhận theo vai trò và phạm vi Branch
 * [P1-125] Lấy danh sách, lọc đã đọc/chưa đọc, đánh dấu đã đọc
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient", columnList = "recipientId"),
    @Index(name = "idx_notification_read", columnList = "recipientId, isRead")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID recipientId; // Người nhận

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    // Optional reference to the related entity
    private UUID referenceId;
    private String referenceType; // e.g., "INVOICE", "CONTRACT", "BOOKING"

    // Branch scope for targeting
    private UUID branchId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
