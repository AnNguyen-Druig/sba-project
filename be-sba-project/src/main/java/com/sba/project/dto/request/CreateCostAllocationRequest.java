package com.sba.project.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCostAllocationRequest {

    @NotNull(message = "Room Service Charge ID không được để trống")

    private UUID roomServiceChargeId;

    @NotNull(message = "Room ID không được để trống")

    private UUID roomId;

    private String reason;

    @NotEmpty(message = "Danh sách phân bổ không được rỗng")

    private List<AllocationItemRequest> items;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllocationItemRequest {

        @NotNull(message = "Tenant ID không được để trống")

        private UUID tenantId;

        @NotNull(message = "Số tiền không được để trống")

        @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền không được âm")

        private BigDecimal amount;

        private BigDecimal percentage;

        private String note;
    }
}
