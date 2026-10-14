package com.sba.project.modules.property.dto.request;

import com.sba.project.modules.property.enums.RentalMode;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class RoomRequest {
    @NotNull private UUID branchId;
    @NotNull private UUID roomTypeId;
    private String roomCode;
    private RentalMode rentalMode;
    @NotNull private Integer capacity;
    private BigDecimal areaSqm;
    private Integer floorNumber;
    @NotNull private BigDecimal referencePrice;
    private String description;
    private String[] amenities;
    private JsonNode imageRefs;
    private RoomOperationalStatus operationalStatus;
    private Boolean isPublic;
}
