package com.sba.project.modules.maintenance.dto.response;

import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public record HandoverResponse(
        UUID contractTenantId,
        Instant handoverAt,
        UUID handoverBy,
        JsonNode snapshot,
        JsonNode evidenceRefs) {
}
