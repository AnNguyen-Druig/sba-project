package com.sba.project.modules.leasing.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.leasing.dto.request.BookingCreateRequest;
import com.sba.project.modules.leasing.dto.request.BookingDecisionRequest;
import com.sba.project.modules.leasing.dto.request.BookingSearchRequest;
import com.sba.project.modules.leasing.dto.response.BookingResponse;
import com.sba.project.modules.leasing.enums.BookingStatus;
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
}
