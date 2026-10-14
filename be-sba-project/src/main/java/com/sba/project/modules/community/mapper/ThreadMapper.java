package com.sba.project.modules.community.mapper;

import com.sba.project.modules.community.dto.response.ThreadResponse;
import com.sba.project.modules.community.entity.Thread;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ThreadMapper {
    ThreadResponse toResponse(Thread thread);
}
