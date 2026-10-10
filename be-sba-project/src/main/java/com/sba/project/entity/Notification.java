package com.sba.project.entity;
import com.sba.project.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_recipient", columnList = "recipientId"),
    @Index(name = "idx_notification_read", columnList = "recipientId, isRead")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID recipientId; 

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

    private UUID referenceId;

    private String referenceType; 

    private UUID branchId;
    private LocalDateTime readAt;
}
