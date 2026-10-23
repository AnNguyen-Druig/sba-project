package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.dto.request.BookingCreateRequest;
import com.sba.project.modules.leasing.dto.request.BookingDecisionRequest;
import com.sba.project.modules.leasing.dto.request.BookingSearchRequest;
import com.sba.project.modules.leasing.dto.response.BookingPaymentInstructionResponse;
import com.sba.project.modules.leasing.dto.response.BookingResponse;
import com.sba.project.modules.leasing.entity.Appointment;
import com.sba.project.modules.leasing.entity.Booking;
import com.sba.project.modules.leasing.entity.ResourceAllocation;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.enums.HoldReleaseCause;
import com.sba.project.modules.leasing.enums.HoldRefundOutcome;
import com.sba.project.modules.leasing.enums.PaymentEnvironment;
import com.sba.project.modules.leasing.enums.RentalMode;
import com.sba.project.modules.leasing.enums.ResourceAllocationStatus;
import com.sba.project.modules.leasing.mapper.BookingMapper;
import com.sba.project.modules.leasing.repository.AppointmentRepository;
import com.sba.project.modules.leasing.repository.BookingRepository;
import com.sba.project.modules.leasing.repository.ResourceAllocationRepository;
import com.sba.project.modules.leasing.service.BookingFundsPort;
import com.sba.project.modules.leasing.service.BookingService;
import com.sba.project.modules.leasing.service.TenantAccessPolicy;
import com.sba.project.modules.leasing.service.policy.LeasingDates;
import com.sba.project.modules.leasing.specifications.BookingSpecifications;
import com.sba.project.modules.property.dto.response.BranchPolicyView;
import com.sba.project.modules.property.dto.response.LeasingRoomView;
import com.sba.project.modules.property.dto.response.LeasingSlotView;
import com.sba.project.modules.property.enums.BranchStatus;
import com.sba.project.modules.property.enums.RoomOperationalStatus;
import com.sba.project.modules.property.enums.RoomSlotStatus;
import com.sba.project.modules.property.service.PropertyQueryService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
    private static final String REFERENCE_PREFIX = "BK";
    private static final int REFERENCE_HEX_LENGTH = 12;
    private static final int EXPIRY_BATCH = 200;
    private static final DateTimeFormatter DEADLINE_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(LeasingDates.ZONE);

    private final BookingRepository bookingRepository;
    private final ResourceAllocationRepository allocationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PropertyQueryService propertyQueryService;
    private final ResourceAvailabilityGuard availabilityGuard;
    private final BookingHoldLifecycle holdLifecycle;
    private final BookingFundsPort fundsPort;
    private final BookingMapper bookingMapper;
    private final TenantAccessPolicy accessPolicy;
    private final AuditActorContext auditContext;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final PaymentEnvironment paymentEnvironment;

    BookingServiceImpl(BookingRepository bookingRepository, ResourceAllocationRepository allocationRepository,
            AppointmentRepository appointmentRepository, PropertyQueryService propertyQueryService,
            ResourceAvailabilityGuard availabilityGuard, BookingHoldLifecycle holdLifecycle,
            BookingFundsPort fundsPort, BookingMapper bookingMapper, TenantAccessPolicy accessPolicy,
            AuditActorContext auditContext, TransactionTemplate transactionTemplate, Clock clock,
            // REAL vs SANDBOX money is a server setting; never chosen by the client.
            @Value("${app.leasing.payment-environment:SANDBOX}") PaymentEnvironment paymentEnvironment) {
        this.bookingRepository = bookingRepository;
        this.allocationRepository = allocationRepository;
        this.appointmentRepository = appointmentRepository;
        this.propertyQueryService = propertyQueryService;
        this.availabilityGuard = availabilityGuard;
        this.holdLifecycle = holdLifecycle;
        this.fundsPort = fundsPort;
        this.bookingMapper = bookingMapper;
        this.transactionTemplate = transactionTemplate;
        this.accessPolicy = accessPolicy;
        this.auditContext = auditContext;
        this.clock = clock;
        this.paymentEnvironment = paymentEnvironment;
    }

    @Override
    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        UUID actorId = accessPolicy.currentUserId();
        LeasingRoomView room = propertyQueryService.getRoom(request.roomId());
        if (room.branchStatus() != BranchStatus.ACTIVE || room.operationalStatus() != RoomOperationalStatus.AVAILABLE) {
            throw new AppException(ErrorCode.RESOURCE_UNAVAILABLE);
        }
        // The Room decides ROOM vs SLOT; the client only names the Slot (BR-P10).
        RentalMode mode = RentalMode.valueOf(room.rentalMode().name());
        requireSlotMatchesMode(mode, room.roomId(), request.roomSlotId());
        LocalDate endExclusive = requireValidPeriod(request.startDate(), request.lastDay());
        requireOwnAppointment(actorId, room.roomId(), request.appointmentId());
        BranchPolicyView policy = propertyQueryService.currentPolicy(room.branchId());

        auditContext.bind(actorId);
        // Check + hold run under the Room lock in this transaction (BR-P09, BR-P10b).
        availabilityGuard.lockAndAssertFree(room.roomId(), request.roomSlotId(), request.startDate(), endExclusive);

        Instant now = clock.instant();
        Instant temporaryHoldUntil = now.plus(Duration.ofMinutes(policy.temporaryHoldMinutes()));
        Booking booking = bookingRepository.save(Booking.builder()
                .branchId(room.branchId())
                .roomId(room.roomId())
                .roomSlotId(request.roomSlotId())
                .requesterUserId(actorId)
                .appointmentId(request.appointmentId())
                .policyId(policy.id())
                .rentalMode(mode)
                .startDate(request.startDate())
                .endDate(endExclusive)
                .holdAmount(policy.bookingHoldAmount())
                .policySnapshot(snapshot(policy))
                .paymentDueAt(now.plus(Duration.ofMinutes(policy.paymentWindowMinutes())))
                .temporaryHoldUntil(temporaryHoldUntil)
                .paymentEnvironment(paymentEnvironment)
                .status(BookingStatus.AWAITING_PAYMENT)
                .build());
        allocationRepository.save(ResourceAllocation.builder()
                .roomId(booking.getRoomId())
                .roomSlotId(booking.getRoomSlotId())
                .bookingId(booking.getId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .expiresAt(temporaryHoldUntil)
                .status(ResourceAllocationStatus.HELD)
                .build());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(UUID bookingId) {
        UUID actorId = accessPolicy.currentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!actorId.equals(booking.getRequesterUserId())) requireBranchScope(actorId, booking.getBranchId());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> myBookings(BookingStatus status, Pageable pageable) {
        UUID actorId = accessPolicy.currentUserId();
        return page(BookingSpecifications.forRequester(actorId, status), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> search(BookingSearchRequest criteria, Pageable pageable) {
        UUID actorId = accessPolicy.currentUserId();
        if (criteria.branchId() == null) throw new AppException(ErrorCode.INVALID_REQUEST);
        requireBranchScope(actorId, criteria.branchId());
        return page(BookingSpecifications.forBranch(criteria.branchId(), criteria.roomId(), criteria.status(),
                criteria.from(), criteria.to()), pageable);
    }

    @Override
    @Transactional
    public BookingResponse reject(UUID bookingId, BookingDecisionRequest request) {
        UUID actorId = accessPolicy.currentUserId();
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        requireBranchScope(actorId, booking.getBranchId());
        if (!BookingHoldLifecycle.LIVE_HOLD.contains(booking.getStatus())) {
            throw new AppException(ErrorCode.LEASING_STATE_CONFLICT);
        }
        auditContext.bind(actorId);
        HoldRefundOutcome outcome = holdLifecycle.release(booking, BookingStatus.REJECTED,
                HoldReleaseCause.LANDLORD_REJECT, request.reason().trim());
        return bookingMapper.toResponse(booking, outcome);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingPaymentInstructionResponse getPaymentInstruction(UUID bookingId) {
        UUID actorId = accessPolicy.currentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!actorId.equals(booking.getRequesterUserId())) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (booking.getStatus() == BookingStatus.EXPIRED || (booking.getStatus() == BookingStatus.AWAITING_PAYMENT
                && clock.instant().isAfter(booking.getPaymentDueAt()))) {
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            throw new AppException(ErrorCode.LEASING_STATE_CONFLICT);
        }
        BigDecimal verified = fundsPort.verifiedHold(bookingId).verifiedAmount();
        BigDecimal amountDue = booking.getHoldAmount().subtract(verified).max(BigDecimal.ZERO);
        String reference = paymentReference(bookingId);
        String instructions = "Chuyển khoản %s đ với nội dung \"%s\" trước %s (giờ Việt Nam). Giữ chỗ chỉ được xác nhận sau khi khoản tiền được xác minh."
                .formatted(amountDue.toPlainString(), reference, DEADLINE_FORMAT.format(booking.getPaymentDueAt()));
        return new BookingPaymentInstructionResponse(bookingId, booking.getHoldAmount(), verified, amountDue,
                reference, booking.getPaymentDueAt(), booking.getPaymentEnvironment(),
                propertyQueryService.getPaymentQrUrl(booking.getBranchId()), instructions);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findBookingIdByPaymentReference(String reference) {
        if (reference == null) return Optional.empty();
        String normalized = reference.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches(REFERENCE_PREFIX + "[0-9A-F]{" + REFERENCE_HEX_LENGTH + "}")) return Optional.empty();
        List<UUID> matches = bookingRepository.findIdsByHexPrefix(
                normalized.substring(REFERENCE_PREFIX.length()).toLowerCase(Locale.ROOT) + "%");
        // An ambiguous reference must go to manual review, never to a guessed Booking.
        return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }

    @Override
    @Transactional
    public BookingResponse confirmHold(UUID bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (booking.getStatus() == BookingStatus.CONFIRMED) return bookingMapper.toResponse(booking);
        if (booking.getStatus() == BookingStatus.EXPIRED) throw new AppException(ErrorCode.BOOKING_EXPIRED);
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT) {
            throw new AppException(ErrorCode.LEASING_STATE_CONFLICT);
        }
        BookingFundsPort.VerifiedHold funds = fundsPort.verifiedHold(bookingId);
        if (!BookingHoldLifecycle.isFullyPaidOnTime(booking, funds)) {
            // Money completed after the deadline is a late payment for manual review (BR-P10c).
            boolean late = funds.fullyPaidAt() != null && funds.fullyPaidAt().isAfter(booking.getPaymentDueAt());
            throw new AppException(late ? ErrorCode.BOOKING_EXPIRED : ErrorCode.BOOKING_HOLD_FUNDS_INSUFFICIENT);
        }
        holdLifecycle.confirm(booking);
        return bookingMapper.toResponse(booking);
    }

    /**
     * Each lapsed hold is settled in its own transaction, taking the Room lock before the row lock
     * (same order as the availability guard) so one failure or contention does not block the batch.
     */
    @Override
    public int settleLapsedHolds() {
        int settled = 0;
        for (UUID bookingId : bookingRepository.findLapsedHoldIds(clock.instant(), PageRequest.of(0, EXPIRY_BATCH))) {
            try {
                if (Boolean.TRUE.equals(transactionTemplate.execute(status -> settleOne(bookingId)))) settled++;
            } catch (RuntimeException ex) {
                log.warn("Could not settle lapsed Booking hold {} ({})", bookingId, ex.getClass().getSimpleName());
            }
        }
        return settled;
    }

    private boolean settleOne(UUID bookingId) {
        UUID roomId = bookingRepository.findRoomIdById(bookingId).orElse(null);
        if (roomId == null) return false;
        allocationRepository.lockRoom(roomId.toString());
        Booking booking = bookingRepository.findByIdForUpdate(bookingId).orElse(null);
        // Re-check under lock: it may have been confirmed, cancelled or settled meanwhile.
        if (booking == null || !BookingHoldLifecycle.isLapsed(booking, clock.instant())) return false;
        holdLifecycle.settleLapsed(booking);
        return true;
    }

    /** Fixed per Booking: "BK" + first 12 hex digits of its id (transfer-content friendly). */
    private static String paymentReference(UUID bookingId) {
        return REFERENCE_PREFIX + bookingId.toString().replace("-", "").substring(0, REFERENCE_HEX_LENGTH)
                .toUpperCase(Locale.ROOT);
    }

    private void requireSlotMatchesMode(RentalMode mode, UUID roomId, UUID slotId) {
        if (mode == RentalMode.ROOM) {
            if (slotId != null) throw new AppException(ErrorCode.INVALID_REQUEST);
            return;
        }
        if (slotId == null) throw new AppException(ErrorCode.INVALID_REQUEST);
        LeasingSlotView slot = propertyQueryService.getSlot(slotId);
        if (!roomId.equals(slot.roomId())) throw new AppException(ErrorCode.ROOM_SLOT_NOT_FOUND);
        if (slot.status() != RoomSlotStatus.AVAILABLE) throw new AppException(ErrorCode.RESOURCE_UNAVAILABLE);
    }

    /** Any start day of the month is allowed (BR-R05), but not in the past (Vietnam date, BR-P03a). */
    private LocalDate requireValidPeriod(LocalDate startDate, LocalDate lastDay) {
        if (startDate.isBefore(LeasingDates.today(clock)) || lastDay.isBefore(startDate)) {
            throw new AppException(ErrorCode.INVALID_RENTAL_PERIOD);
        }
        return LeasingDates.toEndExclusive(lastDay);
    }

    /** An Appointment is optional; when given it must be the requester's own viewing of this Room. */
    private void requireOwnAppointment(UUID actorId, UUID roomId, UUID appointmentId) {
        if (appointmentId == null) return;
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        if (!actorId.equals(appointment.getRequesterUserId()) || !roomId.equals(appointment.getRoomId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void requireBranchScope(UUID actorId, UUID branchId) {
        if (!accessPolicy.canManageBranch(actorId, branchId)) throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    /** Policy terms frozen at request time; later policy versions do not change this Booking. */
    private static ObjectNode snapshot(BranchPolicyView policy) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("policyId", policy.id().toString());
        node.put("version", policy.version());
        node.put("effectiveFrom", policy.effectiveFrom().toString());
        node.put("bookingHoldAmount", policy.bookingHoldAmount());
        node.put("paymentWindowMinutes", policy.paymentWindowMinutes());
        node.put("temporaryHoldMinutes", policy.temporaryHoldMinutes());
        node.put("confirmedHoldMinutes", policy.confirmedHoldMinutes());
        node.put("cancellationTerms", policy.cancellationTerms());
        return node;
    }

    private PagedResponse<BookingResponse> page(Specification<Booking> spec, Pageable pageable) {
        Page<Booking> page = bookingRepository.findAll(spec,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST));
        // PagedResponse numbers pages from 1; Spring Data pages from 0.
        return PagedResponse.of(page.getContent().stream().map(bookingMapper::toResponse).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }
}
