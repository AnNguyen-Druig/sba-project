package com.sba.project.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSlotResponse {

    private UUID roomSlotId;
    private UUID roomId;
    private String slotCode;
    private String slotName;
    private String status;
    private String description;
}
