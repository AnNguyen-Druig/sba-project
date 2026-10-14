package com.sba.project.modules.utilities.mapper;

import com.sba.project.modules.utilities.dto.response.ChargeDetailResponse;
import com.sba.project.modules.utilities.dto.response.RoomServiceChargeResponse;
import com.sba.project.modules.utilities.entity.RoomServiceCharge;
import com.sba.project.modules.utilities.enums.BillingMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link RoomServiceCharge} entity and DTOs [RULE 01].
 */
@Mapper(componentModel = "spring")
public interface RoomServiceChargeMapper {

    @Mapping(target = "serviceName", ignore = true)
    RoomServiceChargeResponse toResponse(RoomServiceCharge entity);

    default RoomServiceChargeResponse toResponse(RoomServiceCharge entity, String serviceName) {
        RoomServiceChargeResponse response = toResponse(entity);
        if (response != null) {
            response.setServiceName(serviceName);
        }
        return response;
    }

    @Mapping(target = "chargeId", source = "id")
    @Mapping(target = "serviceName", ignore = true)
    @Mapping(target = "billingMethod", ignore = true)
    ChargeDetailResponse toChargeDetailResponse(RoomServiceCharge entity);

    default ChargeDetailResponse toChargeDetailResponse(RoomServiceCharge entity, String serviceName, BillingMethod billingMethod) {
        ChargeDetailResponse response = toChargeDetailResponse(entity);
        if (response != null) {
            response.setServiceName(serviceName);
            response.setBillingMethod(billingMethod);
        }
        return response;
    }
}
