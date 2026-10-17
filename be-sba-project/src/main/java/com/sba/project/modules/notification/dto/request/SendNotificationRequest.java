package com.sba.project.modules.notification.dto.request;

import com.sba.project.modules.notification.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload used by other modules or internal endpoints to dispatch a single notification.
 * Task: [P1-123]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotNull(message = "Recipient user ID is required")
    private UUID recipientUserId;

    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String referenceType;

    private UUID referenceId;

    @Builder.Default
    private NotificationChannel channel = NotificationChannel.IN_APP;
}
