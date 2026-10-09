package com.sba.project.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterContractServiceRequest {

    @NotNull(message = "Contract ID không được để trống")

    private UUID contractId;

    @NotNull(message = "Room Service ID không được để trống")

    private UUID roomServiceId;

    @NotNull(message = "Ngày bắt đầu không được để trống")

    private LocalDate startDate;

    private LocalDate endDate;
}
