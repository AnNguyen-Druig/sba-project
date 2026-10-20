package com.sba.project.modules.maintenance.service;

import java.util.UUID;

/**
 * Integration contract owned by Student 4 (notification). Implementations publish an in-app
 * notification. Student 4 de-duplicates by type + referenceType + referenceId, so callers use
 * one stable type per event (P1-126).
 */
public interface MaintenanceNotifier {
    String REFERENCE_TYPE = "REPORT_REQUEST";

    void notifyUser(UUID recipientUserId, String type, String title, String message, UUID reportRequestId);
}
