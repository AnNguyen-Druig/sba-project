package com.sba.project.modules.property.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class PublicRoomResponse {
    RoomResponse room;
    UUID branchId;
    String branchName;
    String address;
    String roomTypeName;
}
