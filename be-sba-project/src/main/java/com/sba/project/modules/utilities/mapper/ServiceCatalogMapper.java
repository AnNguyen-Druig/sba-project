package com.sba.project.modules.utilities.mapper;

import com.sba.project.modules.utilities.dto.request.CreateServiceRequest;
import com.sba.project.modules.utilities.dto.request.UpdateServiceRequest;
import com.sba.project.modules.utilities.dto.response.ServiceCatalogResponse;
import com.sba.project.modules.utilities.entity.ServiceCatalog;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for {@link ServiceCatalog} entity and DTOs [RULE 01].
 */
@Mapper(componentModel = "spring")
public interface ServiceCatalogMapper {

    ServiceCatalogResponse toResponse(ServiceCatalog entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "active", constant = "true")
    ServiceCatalog toEntity(CreateServiceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "branchId", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "serviceType", ignore = true)
    @Mapping(target = "mandatory", ignore = true)
    void updateEntity(@MappingTarget ServiceCatalog entity, UpdateServiceRequest request);
}
