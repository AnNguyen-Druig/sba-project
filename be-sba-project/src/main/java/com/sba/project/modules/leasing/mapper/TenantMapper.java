package com.sba.project.modules.leasing.mapper;

import com.sba.project.modules.leasing.dto.response.OccupancyResponse;
import com.sba.project.modules.leasing.entity.RoomSlotOccupancy;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TenantMapper {
    OccupancyResponse toOccupancy(RoomSlotOccupancy occupancy);
}
