package com.sba.project.modules.notification.dto.response;

import com.sba.project.modules.notification.enums.NotificationChannel;
import com.sba.project.modules.notification.enums.NotificationDeliveryStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response representation for a notification.
 * Task: [P1-125]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;

    private UUID recipientUserId;

    private String eventKey;

    private String type;

    private String title;

    private String message;

    private String referenceType;

    private UUID referenceId;

    private boolean read;

    private Instant readAt;

    private Instant createdAt;

    private NotificationChannel channel;

    private NotificationDeliveryStatus deliveryStatus;
}
