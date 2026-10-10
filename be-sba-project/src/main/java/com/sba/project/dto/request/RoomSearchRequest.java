package com.sba.project.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSearchRequest {

    private UUID branchId;
    private UUID roomTypeId;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @PositiveOrZero(message = "Giá tối thiểu không được âm")
    private BigDecimal minPrice;

    @PositiveOrZero(message = "Giá tối đa không được âm")
    private BigDecimal maxPrice;

    private Integer minCapacity;

    @Size(max = 255, message = "Trạng thái tối đa 255 ký tự")
    private String status;
}
