package com.sba.project.dto.response;
import com.sba.project.enums.BillingMethod;
import com.sba.project.enums.ServiceStatus;
import com.sba.project.enums.ServiceType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private UUID id;

    private String name;

    private ServiceType serviceType;

    private String unit;

    private BillingMethod billingMethod;

    private BigDecimal defaultUnitPrice;

    private ServiceStatus status;

    private String description;

    private boolean isDefault;

    private UUID branchId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
