package com.sba.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Tạo phí dịch vụ cho dịch vụ không dựa theo chỉ số (BY_PERSON, BY_ROOM, FIXED, PER_USE)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceChargeRequest {

    @NotNull(message = "Room Service ID không được để trống")
    private UUID roomServiceId;

    @NotNull(message = "Room ID không được để trống")
    private UUID roomId;

    @NotNull(message = "Ngày bắt đầu kỳ không được để trống")
    private LocalDate billingPeriodStart;

    @NotNull(message = "Ngày kết thúc kỳ không được để trống")
    private LocalDate billingPeriodEnd;

    @NotNull(message = "Số lượng không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Số lượng không được âm")
    private BigDecimal quantity;
}
