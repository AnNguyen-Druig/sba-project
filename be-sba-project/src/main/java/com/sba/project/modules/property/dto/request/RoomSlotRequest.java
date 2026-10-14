package com.sba.project.modules.property.dto.request;

import com.sba.project.modules.property.enums.RoomSlotStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class RoomSlotRequest {
    @NotNull private UUID roomId;
    private String slotCode;
    private String slotName;
    private RoomSlotStatus status;
}
