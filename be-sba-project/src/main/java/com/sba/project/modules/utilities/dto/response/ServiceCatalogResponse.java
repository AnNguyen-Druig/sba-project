package com.sba.project.modules.utilities.dto.response;

import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.ServiceType;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO representing a service catalog item [P1-68].
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCatalogResponse {

    private UUID id;
    private UUID branchId;
    private String code;
    private String name;
    private String unit;
    private ServiceType serviceType;
    private BillingMethod billingMethod;
    private BigDecimal referencePrice;
    private Boolean mandatory;
    private Boolean active;
}
