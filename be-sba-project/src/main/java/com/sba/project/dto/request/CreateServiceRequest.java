package com.sba.project.dto.request;
import com.sba.project.enums.BillingMethod;
import com.sba.project.enums.ServiceType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateServiceRequest {

    @NotBlank(message = "Tên dịch vụ không được để trống")

    @Size(max = 255, message = "Tên dịch vụ tối đa 255 ký tự")

    private String name;

    @NotNull(message = "Loại dịch vụ không được để trống")

    private ServiceType serviceType;

    @NotBlank(message = "Đơn vị không được để trống")

    private String unit;

    @NotNull(message = "Phương thức tính phí không được để trống")

    private BillingMethod billingMethod;

    @NotNull(message = "Đơn giá không được để trống")

    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá không được âm")

    private BigDecimal defaultUnitPrice;

    private String description;

    @NotNull(message = "Branch ID không được để trống")

    private UUID branchId;
}
