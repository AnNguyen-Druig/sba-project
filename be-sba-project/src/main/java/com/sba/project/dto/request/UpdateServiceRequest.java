package com.sba.project.dto.request;
import com.sba.project.enums.BillingMethod;
import com.sba.project.enums.ServiceStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceRequest {

    @Size(max = 255, message = "Tên dịch vụ tối đa 255 ký tự")

    private String name;

    private String unit;

    private BillingMethod billingMethod;

    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá không được âm")

    private BigDecimal defaultUnitPrice;

    private String description;

    private ServiceStatus status;
}
