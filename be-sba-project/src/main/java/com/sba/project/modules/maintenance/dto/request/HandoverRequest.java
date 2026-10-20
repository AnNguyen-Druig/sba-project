package com.sba.project.modules.maintenance.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/** P2-19: furniture handed over/taken back for one rental member; items must belong to the member's room. */
public record HandoverRequest(
        @NotEmpty @Valid List<Item> items,
        @Valid List<EvidenceRequest> evidence) {

    public record Item(@NotNull UUID furnitureId, @NotBlank String condition, String note) {
    }
}
