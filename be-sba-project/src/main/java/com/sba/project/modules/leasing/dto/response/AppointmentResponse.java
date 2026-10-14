package com.sba.project.modules.leasing.dto.response;

import com.sba.project.modules.leasing.enums.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class AppointmentResponse {
    UUID id; UUID roomId; UUID requesterUserId; UUID assignedTo; Instant startAt; Instant endAt;
    AppointmentStatus status; String note; Instant createdAt; Instant updatedAt; Long version;
}
