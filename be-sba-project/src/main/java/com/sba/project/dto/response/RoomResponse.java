package com.sba.project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {

    private UUID roomId;
    private UUID branchId;
    private UUID managerId;
    private UUID roomTypeId;
    private String roomCode;
    private BigDecimal referencePrice;
    private Integer capacity;
    private String status;
    private String description;
}
