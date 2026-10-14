package com.sba.project.modules.identity.mapper;

import com.sba.project.modules.identity.dto.response.OwnerVerificationResponse;
import com.sba.project.modules.identity.entity.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OwnerVerificationMapper {
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "status", source = "ownerVerificationStatus")
    @Mapping(target = "documentPublicId", source = "ownerDocumentPublicId")
    @Mapping(target = "documentResourceType", source = "ownerDocumentResourceType")
    @Mapping(target = "reviewedBy", source = "ownerReviewedBy")
    @Mapping(target = "reviewedAt", source = "ownerReviewedAt")
    @Mapping(target = "reviewReason", source = "ownerReviewReason")
    OwnerVerificationResponse toResponse(User user);
}
