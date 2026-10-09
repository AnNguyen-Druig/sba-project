package com.sba.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Nhập chỉ số điện/nước
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeterReadingRequest {

    @NotNull(message = "Room Service ID không được để trống")
    private UUID roomServiceId;

    @NotNull(message = "Room ID không được để trống")
    private UUID roomId;

    @NotNull(message = "Ngày bắt đầu kỳ không được để trống")
    private LocalDate billingPeriodStart;

    @NotNull(message = "Ngày kết thúc kỳ không được để trống")
    private LocalDate billingPeriodEnd;

    @NotNull(message = "Chỉ số đầu kỳ không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số đầu kỳ không được âm")
    private BigDecimal previousReading;

    @NotNull(message = "Chỉ số cuối kỳ không được để trống")
    @DecimalMin(value = "0.0", message = "Chỉ số cuối kỳ không được âm")
    private BigDecimal currentReading;
}
