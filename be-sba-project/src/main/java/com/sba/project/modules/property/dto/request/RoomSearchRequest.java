package com.sba.project.modules.property.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class RoomSearchRequest {
    private UUID branchId;
    private UUID roomTypeId;
    private String address;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minCapacity;
    private String status;
}
