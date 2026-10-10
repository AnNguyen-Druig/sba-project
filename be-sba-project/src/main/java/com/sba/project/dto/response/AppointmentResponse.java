package com.sba.project.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID appointmentId;
    private UUID tenantId;
    private UUID roomId;
    private LocalDateTime appointmentAt;
    private String status;
    private String note;
}
