package com.sba.project.mapper;

import com.sba.project.dto.request.RoomRequest;
import com.sba.project.dto.response.PublicRoomResponse;
import com.sba.project.dto.response.RoomResponse;
import com.sba.project.entity.Branch;
import com.sba.project.entity.Manager;
import com.sba.project.entity.Room;
import com.sba.project.entity.RoomType;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link Room} entity and its DTOs.
 * FK objects (Branch, Manager, RoomType) must be resolved before calling toEntity().
 */
@Component
public class RoomMapper {

    /**
     * Converts a {@link RoomRequest} to a new {@link Room} entity.
     * The caller is responsible for loading Branch, Manager and RoomType by their IDs
     * before invoking this method.
     *
     * @param request  the incoming request DTO
     * @param branch   resolved Branch entity
     * @param manager  resolved Manager entity
     * @param roomType resolved RoomType entity
     * @return a new Room entity (PK not yet assigned)
     */
    public Room toEntity(RoomRequest request, Branch branch, Manager manager, RoomType roomType) {
        return Room.builder()
                .branch(branch)
                .manager(manager)
                .roomType(roomType)
                .roomCode(request.getRoomCode())
                .referencePrice(request.getReferencePrice())
                .capacity(request.getCapacity())
                .status(request.getStatus())
                .description(request.getDescription())
                .build();
    }

    /**
     * Updates an existing {@link Room} entity in-place from a {@link RoomRequest}.
     *
     * @param entity   the entity to update
     * @param request  the incoming request DTO
     * @param branch   resolved Branch entity
     * @param manager  resolved Manager entity
     * @param roomType resolved RoomType entity
     */
    public void updateEntity(Room entity, RoomRequest request, Branch branch, Manager manager, RoomType roomType) {
        entity.setBranch(branch);
        entity.setManager(manager);
        entity.setRoomType(roomType);
        entity.setRoomCode(request.getRoomCode());
        entity.setReferencePrice(request.getReferencePrice());
        entity.setCapacity(request.getCapacity());
        entity.setStatus(request.getStatus());
        entity.setDescription(request.getDescription());
    }

    /**
     * Converts a {@link Room} entity to a {@link RoomResponse}.
     *
     * @param entity the entity to convert
     * @return a response DTO
     */
    public RoomResponse toResponse(Room entity) {
        return RoomResponse.builder()
                .roomId(entity.getRoomId())
                .branchId(entity.getBranch().getBranchId())
                .managerId(entity.getManager().getManagerId())
                .roomTypeId(entity.getRoomType().getRoomTypeId())
                .roomCode(entity.getRoomCode())
                .referencePrice(entity.getReferencePrice())
                .capacity(entity.getCapacity())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .build();
    }

    /**
     * Converts a {@link Room} entity to a {@link PublicRoomResponse} for the public search API.
     * Aggregates Branch and RoomType display fields without exposing sensitive information.
     *
     * @param entity the entity to convert (Branch and RoomType must be initialised)
     * @return a public-facing response DTO
     */
    public PublicRoomResponse toPublicResponse(Room entity) {
        return PublicRoomResponse.builder()
                .roomId(entity.getRoomId())
                .branchId(entity.getBranch().getBranchId())
                .branchName(entity.getBranch().getBranchName())
                .address(entity.getBranch().getAddress())
                .roomTypeId(entity.getRoomType().getRoomTypeId())
                .typeName(entity.getRoomType().getTypeName())
                .roomCode(entity.getRoomCode())
                .referencePrice(entity.getReferencePrice())
                .capacity(entity.getCapacity())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .build();
    }
}
