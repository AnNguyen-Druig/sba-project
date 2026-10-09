package com.sba.project.dto.response;

import com.sba.project.enums.ServiceRegistrationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractServiceResponse {
    private UUID id;
    private UUID contractId;
    private RoomServiceResponse roomService;
    private ServiceRegistrationStatus registrationStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID requestedBy;
    private UUID processedBy;
    private LocalDateTime processedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
