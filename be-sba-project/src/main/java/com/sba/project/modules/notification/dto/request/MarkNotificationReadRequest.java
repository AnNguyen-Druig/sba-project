package com.sba.project.modules.notification.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload to mark one or more notifications as read.
 * Task: [P1-125]
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkNotificationReadRequest {

    @NotEmpty(message = "Notification IDs list must not be empty")
    private List<UUID> notificationIds;
}
