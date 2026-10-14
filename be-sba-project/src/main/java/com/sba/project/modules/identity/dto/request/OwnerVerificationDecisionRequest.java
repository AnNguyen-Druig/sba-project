package com.sba.project.modules.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

/** The schema requires a reason for both APPROVED and REJECTED decisions. */
public record OwnerVerificationDecisionRequest(@NotBlank(message = "REVIEW_REASON_REQUIRED") String reason) {
}
