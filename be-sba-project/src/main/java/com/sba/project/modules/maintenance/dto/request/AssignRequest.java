package com.sba.project.modules.maintenance.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** P1-115: the single main person responsible for a request. */
public record AssignRequest(@NotNull UUID assigneeUserId) {
}
