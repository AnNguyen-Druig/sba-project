package com.sba.project.modules.property.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class RoomTypeResponse { UUID id; UUID branchId; String code; String name; Integer defaultCapacity; String description; Boolean active; }
