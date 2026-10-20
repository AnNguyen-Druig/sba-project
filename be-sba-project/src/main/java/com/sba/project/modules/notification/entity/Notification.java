package com.sba.project.modules.notification.entity;

import com.sba.project.modules.notification.enums.NotificationChannel;
import com.sba.project.modules.notification.enums.NotificationDeliveryStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "notifications", schema = "rental")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "event_key", nullable = false, columnDefinition = "text")
    private String eventKey;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false, columnDefinition = "text")
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "reference_type", nullable = true, columnDefinition = "text")
    private String referenceType;

    @Column(name = "reference_id", nullable = true)
    private UUID referenceId;

    @Column(name = "read_at", nullable = true, columnDefinition = "timestamptz")
    private Instant readAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz", updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "channel", nullable = false, length = 10)
    private NotificationChannel channel = NotificationChannel.IN_APP;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "delivery_status", nullable = false, length = 12)
    private NotificationDeliveryStatus deliveryStatus = NotificationDeliveryStatus.PENDING;

    @Builder.Default
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "next_attempt_at", nullable = true, columnDefinition = "timestamptz")
    private Instant nextAttemptAt;

    @Column(name = "sent_at", nullable = true, columnDefinition = "timestamptz")
    private Instant sentAt;

    @Column(name = "provider_message_id", nullable = true, columnDefinition = "text")
    private String providerMessageId;

    @Column(name = "last_error", nullable = true, columnDefinition = "text")
    private String lastError;

    @PrePersist
    protected void initializeInsertTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
    }
}
