package com.sba.project.modules.property.dto.response;

import com.sba.project.modules.property.enums.RoomSlotStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class RoomSlotResponse { UUID id; UUID roomId; String code; String name; RoomSlotStatus status; }
