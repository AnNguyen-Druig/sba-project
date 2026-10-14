package com.sba.project.modules.maintenance.dto.response;

import java.util.UUID;

/** P2-29: how many repair requests reported this furniture. */
public record FurnitureFailureResponse(UUID furnitureId, long requestCount) {
}
