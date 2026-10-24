package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.modules.maintenance.service.MaintenanceNotifier;
import com.sba.project.modules.notification.dto.request.SendNotificationRequest;
import com.sba.project.modules.notification.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter over Student 4's in-app notification service (P1-119). */
@Component
@RequiredArgsConstructor
public class NotificationMaintenanceNotifier implements MaintenanceNotifier {
    static final String REFERENCE_TYPE = "MAINTENANCE_REQUEST";

    private final NotificationService notificationService;

    @Override
    public void notifyUser(UUID recipientUserId, String type, String title, String message, UUID reportRequestId) {
        notificationService.sendNotification(SendNotificationRequest.builder()
                .recipientUserId(recipientUserId).type(type).title(title).message(message)
                .referenceType(REFERENCE_TYPE).referenceId(reportRequestId).build());
    }
}
