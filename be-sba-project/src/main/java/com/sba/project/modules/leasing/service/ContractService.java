package com.sba.project.modules.leasing.service;

import com.sba.project.modules.leasing.dto.request.BookingConversionRequest;
import com.sba.project.modules.leasing.dto.response.ContractResponse;
import java.util.UUID;

/** BE-03.4/03.5 Contract (aggregate root of its ContractTenant participants). */
public interface ContractService {
    /**
     * P1-57: Owner/Manager starts a draft Contract from a CONFIRMED Booking, the customer becoming its
     * representative. Idempotent: repeating returns the existing draft. The Booking keeps its hold and
     * becomes CONVERTED only when the Contract is activated.
     */
    ContractResponse createDraftFromBooking(UUID bookingId, BookingConversionRequest request);
}
