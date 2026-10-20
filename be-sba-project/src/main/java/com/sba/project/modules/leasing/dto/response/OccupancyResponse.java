package com.sba.project.modules.leasing.dto.response;

import java.time.LocalDate;
import java.util.UUID;

/** Actual residence interval [startDate, endDate) of a member (room_slot_occupancies). */
public record OccupancyResponse(
        UUID id,
        UUID roomId,
        UUID roomSlotId,
        UUID contractTenantId,
        LocalDate startDate,
        LocalDate endDate) {
}
