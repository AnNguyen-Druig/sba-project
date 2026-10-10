package com.sba.project.mapper;

import com.sba.project.dto.request.RoomSlotRequest;
import com.sba.project.dto.response.RoomSlotResponse;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomSlot;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link RoomSlot} entity and its DTOs.
 */
@Component
public class RoomSlotMapper {

    /**
     * Converts a {@link RoomSlotRequest} to a new {@link RoomSlot} entity.
     * The caller is responsible for loading the Room entity by roomId before calling this method.
     *
     * @param request the incoming request DTO
     * @param room    resolved Room entity
     * @return a new RoomSlot entity (PK not yet assigned)
     */
    public RoomSlot toEntity(RoomSlotRequest request, Room room) {
        return RoomSlot.builder()
                .room(room)
                .slotCode(request.getSlotCode())
                .slotName(request.getSlotName())
                .status(request.getStatus())
                .description(request.getDescription())
                .build();
    }

    /**
     * Updates an existing {@link RoomSlot} entity in-place from a {@link RoomSlotRequest}.
     *
     * @param entity  the entity to update
     * @param request the incoming request DTO
     * @param room    resolved Room entity
     */
    public void updateEntity(RoomSlot entity, RoomSlotRequest request, Room room) {
        entity.setRoom(room);
        entity.setSlotCode(request.getSlotCode());
        entity.setSlotName(request.getSlotName());
        entity.setStatus(request.getStatus());
        entity.setDescription(request.getDescription());
    }

    /**
     * Converts a {@link RoomSlot} entity to a {@link RoomSlotResponse}.
     *
     * @param entity the entity to convert
     * @return a response DTO
     */
    public RoomSlotResponse toResponse(RoomSlot entity) {
        return RoomSlotResponse.builder()
                .roomSlotId(entity.getRoomSlotId())
                .roomId(entity.getRoom().getRoomId())
                .slotCode(entity.getSlotCode())
                .slotName(entity.getSlotName())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .build();
    }
}
