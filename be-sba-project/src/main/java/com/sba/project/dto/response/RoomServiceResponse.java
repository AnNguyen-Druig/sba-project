package com.sba.project.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomServiceResponse {

    private UUID id;

    private UUID roomId;

    private ServiceResponse service;

    private BigDecimal unitPrice;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private boolean active;

    private LocalDateTime createdAt;
}
