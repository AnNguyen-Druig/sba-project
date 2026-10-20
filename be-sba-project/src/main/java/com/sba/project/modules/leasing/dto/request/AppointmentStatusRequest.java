package com.sba.project.modules.leasing.dto.request;

import com.sba.project.modules.leasing.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentStatusRequest {
    @NotNull private AppointmentStatus status;
}
