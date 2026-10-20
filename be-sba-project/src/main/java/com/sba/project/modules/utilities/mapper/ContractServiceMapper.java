package com.sba.project.modules.utilities.mapper;

import com.sba.project.modules.utilities.dto.request.ContractServiceRegistrationRequest;
import com.sba.project.modules.utilities.dto.response.ContractServiceResponse;
import com.sba.project.modules.utilities.entity.ContractService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link ContractService} entity and DTOs [RULE 01].
 */
@Mapper(componentModel = "spring")
public interface ContractServiceMapper {

    @Mapping(target = "serviceName", ignore = true)
    ContractServiceResponse toResponse(ContractService entity);

    default ContractServiceResponse toResponse(ContractService entity, String serviceName) {
        ContractServiceResponse response = toResponse(entity);
        if (response != null) {
            response.setServiceName(serviceName);
        }
        return response;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roomId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    ContractService toEntity(ContractServiceRegistrationRequest request);
}
