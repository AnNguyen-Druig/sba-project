package com.sba.project.service;

import com.sba.project.dto.request.*;
import com.sba.project.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Chỉ số điện/nước và tính phí dịch vụ (BE-04.2)
 */
public interface MeterAndChargeService {

    // Nhập chỉ số điện/nước
    RoomServiceChargeResponse createMeterReading(CreateMeterReadingRequest request, UUID createdBy);

    // Tạo phí dịch vụ (không theo chỉ số)
    RoomServiceChargeResponse createServiceCharge(CreateServiceChargeRequest request, UUID createdBy);

    // Chốt phí dịch vụ
    RoomServiceChargeResponse finalizeCharge(UUID chargeId, UUID finalizedBy);

    // Xem phí theo phòng và kỳ
    List<RoomServiceChargeResponse> getChargesByRoomAndPeriod(UUID roomId, LocalDate from, LocalDate to);

    // Xem phí chưa chốt
    List<RoomServiceChargeResponse> getUnfinalizedCharges(UUID roomId);

    // Phân trang xem phí
    PageResponse<RoomServiceChargeResponse> getChargesByRoom(UUID roomId, Pageable pageable);
}
