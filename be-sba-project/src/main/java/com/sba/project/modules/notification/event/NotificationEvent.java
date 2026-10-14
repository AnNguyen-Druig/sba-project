package com.sba.project.modules.notification.event;

import java.util.UUID;
import lombok.Builder;

/**
 * Spring Application Event published by other domain modules to trigger notifications.
 * Can be published via ApplicationEventPublisher.
 */
@Builder
public record NotificationEvent(
        UUID recipientUserId,
        String type,
        String title,
        String message,
        String referenceType,
        UUID referenceId,
        UUID branchId
) {
}
