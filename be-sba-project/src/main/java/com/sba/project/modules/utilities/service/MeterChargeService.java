package com.sba.project.modules.utilities.service;

import com.sba.project.modules.utilities.dto.request.CreateRoomServiceChargeRequest;
import com.sba.project.modules.utilities.dto.request.FinalizeChargeRequest;
import com.sba.project.modules.utilities.dto.response.ChargeDetailResponse;
import com.sba.project.modules.utilities.dto.response.RoomServiceChargeResponse;
import com.sba.project.modules.utilities.enums.BillingMethod;
import com.sba.project.modules.utilities.enums.RoomServiceChargeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for meter reading and room service charge calculations [BE-04.2].
 */
public interface MeterChargeService {

    /**
     * [P1-74] Records a meter reading or fixed/per-room service charge segment.
     */
    RoomServiceChargeResponse createCharge(UUID createdBy, CreateRoomServiceChargeRequest request);

    /**
     * [P1-75] Finalizes a batch of charges so they become ready for invoicing.
     */
    List<RoomServiceChargeResponse> finalizeCharges(UUID finalizedBy, FinalizeChargeRequest request);

    /**
     * Retrieves charge by ID.
     */
    RoomServiceChargeResponse getCharge(UUID chargeId);

    /**
     * Lists charges for a room within a billing month, optionally filtered by status.
     */
    List<RoomServiceChargeResponse> listCharges(UUID roomId, LocalDate billingMonth, RoomServiceChargeStatus status);

    /**
     * [P1-78] Retrieves charge details and calculation breakdown for reconciliation.
     */
    ChargeDetailResponse getChargeDetail(UUID chargeId);

    /**
     * [P1-75][P1-76] Calculates total charge based on billing method, usage quantity, and unit price.
     */
    BigDecimal calculateCharge(BillingMethod method, BigDecimal quantity, BigDecimal unitPrice);
}
