package com.sba.project.modules.community.dto.response;

import com.sba.project.modules.community.enums.ThreadStatus;
import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/** Moderation details are intentionally not exposed. */
public record ThreadResponse(
        UUID id,
        UUID authorUserId,
        String title,
        String description,
        JsonNode criteria,
        ThreadStatus status,
        Instant expiresAt,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt) {
}
