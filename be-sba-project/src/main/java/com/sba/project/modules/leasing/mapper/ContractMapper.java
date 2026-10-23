package com.sba.project.modules.leasing.mapper;

import com.sba.project.modules.leasing.dto.response.ContractResponse;
import com.sba.project.modules.leasing.dto.response.ContractTenantResponse;
import com.sba.project.modules.leasing.entity.Contract;
import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.service.policy.LeasingDates;
import java.util.List;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/** {@code end_date} is exclusive in the schema; the API exposes the inclusive {@code lastDay}. */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = LeasingDates.class)
public interface ContractMapper {
    @Mapping(target = "lastDay", expression = "java(LeasingDates.toLastDay(contract.getEndDate()))")
    @Mapping(target = "members", source = "members")
    ContractResponse toResponse(Contract contract, List<ContractTenantResponse> members);

    @Mapping(target = "lastDay", expression = "java(LeasingDates.toLastDay(member.getEndDate()))")
    ContractTenantResponse toMemberResponse(ContractTenant member);

    List<ContractTenantResponse> toMemberResponses(List<ContractTenant> members);
}
