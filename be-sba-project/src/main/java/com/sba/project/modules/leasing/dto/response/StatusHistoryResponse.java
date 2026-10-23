package com.sba.project.modules.leasing.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * One status transition (P1-58, P2-20). {@code fromStatus} is null for the creation row. {@code reason}
 * is what the command bound to the transaction, e.g. {@code "CUSTOMER_CANCEL; refund=FULL_REFUND; ..."};
 * {@code actorUserId} is null for system jobs.
 */
public record StatusHistoryResponse(
        String fromStatus,
        String toStatus,
        UUID actorUserId,
        String reason,
        Instant occurredAt) {
}
