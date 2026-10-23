package com.sba.project.modules.leasing.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.leasing.dto.request.BookingCreateRequest;
import com.sba.project.modules.leasing.dto.request.BookingDecisionRequest;
import com.sba.project.modules.leasing.dto.request.BookingSearchRequest;
import com.sba.project.modules.leasing.dto.response.BookingPaymentInstructionResponse;
import com.sba.project.modules.leasing.dto.response.BookingResponse;
import com.sba.project.modules.leasing.dto.response.StatusHistoryResponse;
import com.sba.project.modules.leasing.enums.BookingStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** BE-03.1 Booking requests. */
public interface BookingService {
    /** P1-49/P1-50: any logged-in user (not necessarily a Tenant) holds a Room or Slot. */
    BookingResponse create(BookingCreateRequest request);

    /** Requester or Owner/Manager of the Booking's Branch. */
    BookingResponse getById(UUID bookingId);

    /** P1-51: the caller's own Bookings. */
    PagedResponse<BookingResponse> myBookings(BookingStatus status, Pageable pageable);

    /** P1-51: Owner/Manager Bookings of one Branch in scope. */
    PagedResponse<BookingResponse> search(BookingSearchRequest criteria, Pageable pageable);

    /** P1-51: Owner/Manager rejects a live hold; the hold deposit is fully refundable. */
    BookingResponse reject(UUID bookingId, BookingDecisionRequest request);

    /**
     * P1-56: requester or Owner/Manager cancels a live hold with a reason; the response carries the
     * hold-deposit refund decision (the refund itself is done by the payment module).
     */
    BookingResponse cancel(UUID bookingId, BookingDecisionRequest request);

    /** P1-58: status transitions recorded by the database audit trigger, oldest first. */
    List<StatusHistoryResponse> getHistory(UUID bookingId);

    /** P1-52: payment instruction and reference for the requester while the Booking awaits payment. */
    BookingPaymentInstructionResponse getPaymentInstruction(UUID bookingId);

    /**
     * P1-52: lets the payment module match transfer content to a Booking. Empty when the reference is
     * malformed, unknown or ambiguous — such money goes to manual review.
     */
    Optional<UUID> findBookingIdByPaymentReference(String reference);

    /**
     * P1-53: called by the payment module after it VERIFIED money for the Booking. Confirms the hold
     * only when verified money covers it and was received by the deadline; idempotent once CONFIRMED.
     * Not exposed over HTTP: nobody confirms a hold by changing its status by hand.
     */
    BookingResponse confirmHold(UUID bookingId);

    /** P1-55: expiry job entry point; returns how many lapsed holds were settled. */
    int settleLapsedHolds();
}
