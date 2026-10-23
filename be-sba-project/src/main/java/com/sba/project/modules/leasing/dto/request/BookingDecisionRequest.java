package com.sba.project.modules.leasing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Reason is mandatory for reject/cancel; it is stored and shown in the status history. */
public record BookingDecisionRequest(@NotBlank @Size(max = 1000) String reason) {
}
