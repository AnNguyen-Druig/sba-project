package com.sba.project.service;
import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public interface MeterAndChargeService {
    RoomServiceChargeResponse createMeterReading(CreateMeterReadingRequest request, UUID createdBy);
    RoomServiceChargeResponse createServiceCharge(CreateServiceChargeRequest request, UUID createdBy);
    RoomServiceChargeResponse finalizeCharge(UUID chargeId, UUID finalizedBy);
    List<RoomServiceChargeResponse> getChargesByRoomAndPeriod(UUID roomId, LocalDate from, LocalDate to);
    List<RoomServiceChargeResponse> getUnfinalizedCharges(UUID roomId);
    PageResponse<RoomServiceChargeResponse> getChargesByRoom(UUID roomId, Pageable pageable);
}
