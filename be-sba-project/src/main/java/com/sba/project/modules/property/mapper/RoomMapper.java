package com.sba.project.modules.property.mapper;

import com.sba.project.modules.property.dto.request.RoomRequest;
import com.sba.project.modules.property.dto.response.PublicRoomResponse;
import com.sba.project.modules.property.dto.response.RoomResponse;
import com.sba.project.modules.property.entity.Branch;
import com.sba.project.modules.property.entity.Room;
import com.sba.project.modules.property.entity.RoomType;
import com.sba.project.modules.property.enums.RentalMode;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.JsonNodeFactory;

@Component
public class RoomMapper {
    public Room toEntity(RoomRequest request) {
        Room room = new Room();
        updateEntity(room, request);
        return room;
    }

    public void updateEntity(Room room, RoomRequest request) {
        room.setBranchId(request.getBranchId());
        room.setRoomTypeId(request.getRoomTypeId());
        room.setCode(request.getRoomCode());
        room.setRentalMode(request.getRentalMode() == null ? RentalMode.ROOM : request.getRentalMode());
        room.setCapacity(request.getCapacity());
        room.setAreaSqm(request.getAreaSqm());
        room.setFloorNumber(request.getFloorNumber());
        room.setReferencePrice(request.getReferencePrice());
        room.setDescription(request.getDescription());
        room.setAmenities(request.getAmenities() == null ? new String[0] : request.getAmenities());
        room.setImageRefs(request.getImageRefs() == null ? JsonNodeFactory.instance.arrayNode() : request.getImageRefs());
        room.setOperationalStatus(request.getOperationalStatus() == null ? RoomOperationalStatus.AVAILABLE : request.getOperationalStatus());
        room.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
    }

    public RoomResponse toResponse(Room room) {
        return RoomResponse.builder().id(room.getId()).branchId(room.getBranchId()).roomTypeId(room.getRoomTypeId())
                .code(room.getCode()).rentalMode(room.getRentalMode()).capacity(room.getCapacity()).areaSqm(room.getAreaSqm())
                .floorNumber(room.getFloorNumber()).referencePrice(room.getReferencePrice()).description(room.getDescription())
                .amenities(room.getAmenities()).imageRefs(room.getImageRefs()).operationalStatus(room.getOperationalStatus())
                .isPublic(room.getIsPublic()).createdAt(room.getCreatedAt()).updatedAt(room.getUpdatedAt()).version(room.getVersion()).build();
    }

    public PublicRoomResponse toPublicResponse(Room room, Branch branch, RoomType roomType) {
        return PublicRoomResponse.builder().room(toResponse(room)).branchId(branch.getId()).branchName(branch.getName())
                .address(branch.getAddress()).roomTypeName(roomType.getName()).build();
    }
}
