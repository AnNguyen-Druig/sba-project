package com.sba.project.dto.response;
import com.sba.project.enums.AllocationStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostAllocationResponse {

    private UUID id;

    private UUID roomServiceChargeId;

    private UUID roomId;

    private AllocationStatus status;

    private String reason;

    private UUID proposedBy;

    private UUID processedBy;

    private LocalDateTime processedAt;

    private String rejectionReason;

    private List<AllocationItemResponse> items;

    private LocalDateTime createdAt;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationItemResponse {

        private UUID id;

        private UUID tenantId;

        private BigDecimal amount;

        private BigDecimal percentage;

        private String note;
    }
}
