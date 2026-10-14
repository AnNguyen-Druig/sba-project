package com.sba.project.modules.maintenance.dto.request;

import com.sba.project.modules.maintenance.enums.EvidenceStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** P1-113: reference to an uploaded (Cloudinary) image; the binary never passes through the backend. */
public record EvidenceRequest(
        @NotBlank String publicId,
        @NotBlank @Size(max = 20) String resourceType,
        @NotNull EvidenceStage stage) {
}
