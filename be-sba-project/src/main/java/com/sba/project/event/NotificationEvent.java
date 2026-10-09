package com.sba.project.event;

import com.sba.project.enums.NotificationType;
import lombok.*;

import java.util.UUID;

/**
 * Event phát ra từ các module khác để Student 4 tạo notification.
 * Các Student khác chỉ cần publish event này qua ApplicationEventPublisher.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID recipientId;
    private NotificationType type;
    private String title;
    private String message;
    private UUID referenceId;
    private String referenceType;
    private UUID branchId;
}
