package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.modules.maintenance.service.MaintenanceNotifier;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** TEMPORARY no-op until Student 4's notification service is merged; replace with an adapter over it. */
@Component
@Slf4j
public class PendingNotificationMaintenanceNotifier implements MaintenanceNotifier {
    @Override
    public void notifyUser(UUID recipientUserId, String type, String title, String message, UUID reportRequestId) {
        log.info("Notification pending integration: type={}, request={}", type, reportRequestId);
    }
}
