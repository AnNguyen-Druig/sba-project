package com.sba.project.modules.property.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class RoomTypeRequest {
    @NotNull private UUID branchId;
    private String code;
    private String typeName;
    @NotNull private Integer defaultCapacity;
    private String description;
    private Boolean active;
}
