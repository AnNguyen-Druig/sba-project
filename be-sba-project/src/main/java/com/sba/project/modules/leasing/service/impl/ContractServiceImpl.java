package com.sba.project.modules.leasing.service.impl;

import com.sba.project.common.audit.AuditActorContext;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.leasing.dto.request.BookingConversionRequest;
import com.sba.project.modules.leasing.dto.response.ContractResponse;
import com.sba.project.modules.leasing.entity.Booking;
import com.sba.project.modules.leasing.entity.Contract;
import com.sba.project.modules.leasing.entity.ContractTenant;
import com.sba.project.modules.leasing.enums.BookingStatus;
import com.sba.project.modules.leasing.enums.ContractStatus;
import com.sba.project.modules.leasing.enums.ContractTenantRole;
import com.sba.project.modules.leasing.enums.ContractTenantStatus;
import com.sba.project.modules.leasing.mapper.ContractMapper;
import com.sba.project.modules.leasing.repository.BookingRepository;
import com.sba.project.modules.leasing.repository.ContractRepository;
import com.sba.project.modules.leasing.repository.ContractTenantRepository;
import com.sba.project.modules.leasing.service.ContractService;
import com.sba.project.modules.leasing.service.TenantAccessPolicy;
import com.sba.project.modules.leasing.service.policy.LeasingDates;
import com.sba.project.modules.property.service.PropertyQueryService;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final ContractRepository contractRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final BookingRepository bookingRepository;
    private final PropertyQueryService propertyQueryService;
    private final ContractMapper contractMapper;
    private final TenantAccessPolicy accessPolicy;
    private final AuditActorContext auditContext;
    private final Clock clock;

    @Override
    @Transactional
    public ContractResponse createDraftFromBooking(UUID bookingId, BookingConversionRequest request) {
        UUID actorId = accessPolicy.currentUserId();
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (!accessPolicy.canManageBranch(actorId, booking.getBranchId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Only a hold whose deposit is verified can lead to a Contract (BR-P10a, BR-P12).
        if (booking.getStatus() != BookingStatus.CONFIRMED) throw new AppException(ErrorCode.LEASING_STATE_CONFLICT);

        Optional<ContractTenant> existing = findDraftFor(booking);
        if (existing.isPresent()) return toResponse(existing.get().getContractId());

        UUID ownerId = propertyQueryService.getRoom(booking.getRoomId()).ownerId();
        auditContext.bind(actorId);
        // The draft holds nothing itself: the Booking's hold keeps the period until activation (D2/D7).
        Contract contract = contractRepository.save(Contract.builder()
                .ownerId(ownerId)
                .branchId(booking.getBranchId())
                .roomId(booking.getRoomId())
                .code(newContractCode())
                .rentalMode(booking.getRentalMode())
                .paymentEnvironment(booking.getPaymentEnvironment())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .terms(request.terms().trim())
                .status(ContractStatus.DRAFT)
                .createdBy(actorId)
                .build());
        contractTenantRepository.save(ContractTenant.builder()
                .contractId(contract.getId())
                .tenantUserId(booking.getRequesterUserId())
                .ownerId(ownerId)
                .roomId(booking.getRoomId())
                .roomSlotId(booking.getRoomSlotId())
                .tenantSnapshot(draftSnapshot(booking.getRequesterUserId()))
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .role(ContractTenantRole.REPRESENTATIVE)
                .monthlyRent(request.monthlyRent())
                .firstMonthRent(request.firstMonthRent())
                .lastMonthRent(request.lastMonthRent())
                // Schema CHECK: prepaid requirements equal the first/last period rent.
                .prepaidFirstRequired(request.firstMonthRent())
                .prepaidLastRequired(request.lastMonthRent())
                .terms(request.memberTerms() == null ? "" : request.memberTerms().trim())
                .status(ContractTenantStatus.DRAFT)
                .build());
        return toResponse(contract.getId());
    }

    private Optional<ContractTenant> findDraftFor(Booking booking) {
        return contractTenantRepository
                .findDraftMembers(booking.getRequesterUserId(), booking.getRoomId(), booking.getStartDate()).stream()
                .filter(member -> Objects.equals(member.getRoomSlotId(), booking.getRoomSlotId()))
                .findFirst();
    }

    private ContractResponse toResponse(UUID contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
        return contractMapper.toResponse(contract, contractMapper.toMemberResponses(
                contractTenantRepository.findByContractIdOrderByCreatedAtAsc(contractId)));
    }

    /** Unique per Branch by schema; date + 8 random hex digits makes a clash practically impossible. */
    private String newContractCode() {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return "HD-" + CODE_DATE.format(LeasingDates.today(clock)) + "-" + random;
    }

    /**
     * Identity snapshot is filled when the Contract is signed (the schema requires full_name once the
     * member leaves DRAFT); the draft only records whose it is.
     */
    private static ObjectNode draftSnapshot(UUID userId) {
        ObjectNode snapshot = JsonNodeFactory.instance.objectNode();
        snapshot.put("userId", userId.toString());
        return snapshot;
    }
}
