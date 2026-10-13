package com.sba.project.modules.notification.dto.request;

import com.sba.project.modules.notification.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload to send the same notification to multiple recipients in bulk.
 * Task: [P1-124]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkNotificationRequest {

    @NotEmpty(message = "Recipient user IDs list must not be empty")
    private List<UUID> recipientUserIds;

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
