package com.sba.project.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeResponse {

    private UUID roomTypeId;
    private String typeName;
    private String description;
    private Integer defaultCapacity;
    private String features;
}
