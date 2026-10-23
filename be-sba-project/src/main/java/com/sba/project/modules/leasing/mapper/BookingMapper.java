package com.sba.project.modules.leasing.mapper;

import com.sba.project.modules.leasing.dto.response.BookingResponse;
import com.sba.project.modules.leasing.entity.Booking;
import com.sba.project.modules.leasing.enums.HoldRefundOutcome;
import com.sba.project.modules.leasing.service.policy.LeasingDates;
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
public interface BookingMapper {
    @Mapping(target = "lastDay", expression = "java(LeasingDates.toLastDay(booking.getEndDate()))")
    @Mapping(target = "holdRefundOutcome", ignore = true)
    BookingResponse toResponse(Booking booking);

    @Mapping(target = "lastDay", expression = "java(LeasingDates.toLastDay(booking.getEndDate()))")
    @Mapping(target = "holdRefundOutcome", source = "outcome")
    BookingResponse toResponse(Booking booking, HoldRefundOutcome outcome);
}
