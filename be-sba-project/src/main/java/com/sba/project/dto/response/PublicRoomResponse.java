package com.sba.project.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicRoomResponse {

    private UUID roomId;
    private UUID branchId;
    private String branchName;
    private String address;
    private UUID roomTypeId;
    private String typeName;
    private String roomCode;
    private BigDecimal referencePrice;
    private Integer capacity;
    private String status;
    private String description;
}
