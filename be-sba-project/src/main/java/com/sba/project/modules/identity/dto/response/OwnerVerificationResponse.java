package com.sba.project.modules.identity.dto.response;

import com.sba.project.modules.identity.enums.OwnerVerificationStatus;
import java.time.Instant;
import java.util.UUID;

/** ADMIN-only view. Carries the document reference, never credentials or contact details. */
public record OwnerVerificationResponse(
        UUID userId,
        String fullName,
        OwnerVerificationStatus status,
        String documentPublicId,
        String documentResourceType,
        UUID reviewedBy,
        Instant reviewedAt,
        String reviewReason) {
}
