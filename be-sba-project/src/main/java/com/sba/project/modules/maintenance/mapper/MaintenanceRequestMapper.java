package com.sba.project.modules.maintenance.mapper;

import com.sba.project.modules.maintenance.dto.response.MaintenanceRecordResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestDetailResponse;
import com.sba.project.modules.maintenance.dto.response.MaintenanceRequestSummaryResponse;
import com.sba.project.modules.maintenance.entity.ReportRecord;
import com.sba.project.modules.maintenance.entity.ReportRequest;
import com.sba.project.modules.maintenance.entity.ReportRequestFurniture;
import com.sba.project.modules.maintenance.entity.ReportRequestService;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MaintenanceRequestMapper {
    MaintenanceRequestSummaryResponse toSummary(ReportRequest request);

    @Mapping(target = "furnitureItems", source = "furnitureItems")
    @Mapping(target = "serviceItems", source = "serviceItems")
    @Mapping(target = "records", source = "records")
    MaintenanceRequestDetailResponse toDetail(ReportRequest request, List<ReportRequestFurniture> furnitureItems,
            List<ReportRequestService> serviceItems, List<ReportRecord> records);

    MaintenanceRequestDetailResponse.FurnitureItemResponse toFurnitureItem(ReportRequestFurniture item);

    MaintenanceRequestDetailResponse.ServiceItemResponse toServiceItem(ReportRequestService item);

    MaintenanceRecordResponse toRecord(ReportRecord record);
}
