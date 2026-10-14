package com.sba.project.modules.maintenance.mapper;

import com.sba.project.modules.maintenance.dto.request.FurnitureCreateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureUpdateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureWarrantyRequest;
import com.sba.project.modules.maintenance.dto.response.FurnitureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureWarrantyResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.entity.FurnitureWarranty;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/** Entities use @SuperBuilder; constructors/setters are used so MapStruct does not depend on its generics. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FurnitureMapper {
    FurnitureResponse toResponse(Furniture furniture);

    @Mapping(target = "id", ignore = true)
    Furniture toEntity(FurnitureCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    void updateEntity(FurnitureUpdateRequest request, @MappingTarget Furniture furniture);

    FurnitureWarrantyResponse toWarrantyResponse(FurnitureWarranty warranty);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "furnitureId", ignore = true)
    FurnitureWarranty toWarrantyEntity(FurnitureWarrantyRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "furnitureId", ignore = true)
    void updateWarranty(FurnitureWarrantyRequest request, @MappingTarget FurnitureWarranty warranty);
}
