package com.sba.project.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRescheduleRequest {

    @NotNull(message = "Thời gian hẹn mới không được để trống")
    @Future(message = "Thời gian hẹn mới phải là thời điểm trong tương lai")
    private LocalDateTime appointmentAt;
}
