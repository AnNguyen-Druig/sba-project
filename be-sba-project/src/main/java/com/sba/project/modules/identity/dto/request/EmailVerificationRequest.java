package com.sba.project.modules.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(@NotBlank String token) {
}
