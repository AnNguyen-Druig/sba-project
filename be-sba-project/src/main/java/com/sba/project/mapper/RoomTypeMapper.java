package com.sba.project.mapper;

import com.sba.project.dto.request.RoomTypeRequest;
import com.sba.project.dto.response.RoomTypeResponse;
import com.sba.project.entity.RoomType;
import org.springframework.stereotype.Component;

/**
 * Mapper for {@link RoomType} entity and its DTOs.
 */
@Component
public class RoomTypeMapper {

    /**
     * Converts a {@link RoomTypeRequest} to a new {@link RoomType} entity.
     * The PK is not set — it will be assigned by JPA on persist.
     *
     * @param request the incoming request DTO
     * @return a new RoomType entity
     */
    public RoomType toEntity(RoomTypeRequest request) {
        return RoomType.builder()
                .typeName(request.getTypeName())
                .description(request.getDescription())
                .defaultCapacity(request.getDefaultCapacity())
                .features(request.getFeatures())
                .build();
    }

    /**
     * Updates an existing {@link RoomType} entity in-place from a {@link RoomTypeRequest}.
     *
     * @param entity  the entity to update
     * @param request the incoming request DTO
     */
    public void updateEntity(RoomType entity, RoomTypeRequest request) {
        entity.setTypeName(request.getTypeName());
        entity.setDescription(request.getDescription());
        entity.setDefaultCapacity(request.getDefaultCapacity());
        entity.setFeatures(request.getFeatures());
    }

    /**
     * Converts a {@link RoomType} entity to a {@link RoomTypeResponse}.
     *
     * @param entity the entity to convert
     * @return a response DTO
     */
    public RoomTypeResponse toResponse(RoomType entity) {
        return RoomTypeResponse.builder()
                .roomTypeId(entity.getRoomTypeId())
                .typeName(entity.getTypeName())
                .description(entity.getDescription())
                .defaultCapacity(entity.getDefaultCapacity())
                .features(entity.getFeatures())
                .build();
    }
}
