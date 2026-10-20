package com.sba.project.modules.utilities.mapper;

import com.sba.project.modules.utilities.dto.request.CreateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateRoomServiceRequest;
import com.sba.project.modules.utilities.dto.response.RoomServiceResponse;
import com.sba.project.modules.utilities.entity.RoomService;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for {@link RoomService} entity and DTOs [RULE 01].
 */
@Mapper(componentModel = "spring")
public interface RoomServiceMapper {

    @Mapping(target = "serviceName", ignore = true)
    RoomServiceResponse toResponse(RoomService entity);

    default RoomServiceResponse toResponse(RoomService entity, String serviceName) {
        RoomServiceResponse response = toResponse(entity);
        if (response != null) {
            response.setServiceName(serviceName);
        }
        return response;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "status", ignore = true)
    RoomService toEntity(CreateRoomServiceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "serviceId", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    void updateEntity(@MappingTarget RoomService entity, UpdateRoomServiceRequest request);
}
