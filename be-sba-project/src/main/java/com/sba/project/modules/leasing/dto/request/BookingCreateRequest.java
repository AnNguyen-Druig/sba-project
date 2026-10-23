package com.sba.project.modules.leasing.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * P1-49. {@code lastDay} is the last stay day, inclusive (BR-G01). Rental mode, hold amount, deadlines
 * and status are decided by the server, never sent by the client.
 *
 * @param roomSlotId    required for SLOT rooms, must be null for whole-room rooms
 * @param appointmentId optional viewing appointment of the same requester and Room
 */
public record BookingCreateRequest(
        @NotNull UUID roomId,
        UUID roomSlotId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate lastDay,
        UUID appointmentId) {
}
