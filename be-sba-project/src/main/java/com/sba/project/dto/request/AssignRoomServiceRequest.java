package com.sba.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoomServiceRequest {

    @NotNull(message = "Room ID không được để trống")
    private UUID roomId;

    @NotNull(message = "Service ID không được để trống")
    private UUID serviceId;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá không được âm")
    private BigDecimal unitPrice;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
