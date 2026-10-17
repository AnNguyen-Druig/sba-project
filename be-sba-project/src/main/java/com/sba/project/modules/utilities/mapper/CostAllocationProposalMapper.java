package com.sba.project.modules.utilities.mapper;

import com.sba.project.modules.utilities.dto.request.CreateCostAllocationProposalRequest;
import com.sba.project.modules.utilities.dto.response.CostAllocationProposalResponse;
import com.sba.project.modules.utilities.entity.CostAllocationProposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link CostAllocationProposal} entity and DTOs [RULE 01].
 */
@Mapper(componentModel = "spring")
public interface CostAllocationProposalMapper {

    CostAllocationProposalResponse toResponse(CostAllocationProposal entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "calculationSnapshot", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "proposedBy", ignore = true)
    @Mapping(target = "processedBy", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "roomServiceChargeReference", ignore = true)
    CostAllocationProposal toEntity(CreateCostAllocationProposalRequest request);
}
