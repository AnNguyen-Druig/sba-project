package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.common.exceptions.AppException;
import com.sba.project.common.exceptions.ErrorCode;
import com.sba.project.modules.maintenance.dto.request.FurnitureCreateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureUpdateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureWarrantyRequest;
import com.sba.project.modules.maintenance.dto.response.FurnitureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureWarrantyResponse;
import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.entity.FurnitureWarranty;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import com.sba.project.modules.maintenance.enums.ReportRequestStatus;
import com.sba.project.modules.maintenance.mapper.FurnitureMapper;
import com.sba.project.modules.maintenance.repository.FurnitureRepository;
import com.sba.project.modules.maintenance.repository.FurnitureWarrantyRepository;
import com.sba.project.modules.maintenance.repository.ReportRequestFurnitureRepository;
import com.sba.project.modules.maintenance.service.FurnitureService;
import com.sba.project.modules.maintenance.service.MaintenanceAccessPolicy;
import com.sba.project.modules.maintenance.specifications.FurnitureSpecifications;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FurnitureServiceImpl implements FurnitureService {
    /** Unfinished repair states; the same set the schema indexes as open (ix_repair_sla). */
    static final Set<ReportRequestStatus> OPEN_REPAIR_STATUSES =
            EnumSet.of(ReportRequestStatus.OPEN, ReportRequestStatus.ACCEPTED, ReportRequestStatus.IN_PROGRESS);
    private static final Set<String> SORTABLE_PROPERTIES =
            Set.of("assetCode", "name", "furnitureType", "status", "purchaseDate", "assetValue");
    private static final Sort DEFAULT_SORT = Sort.by("assetCode");

    private final FurnitureRepository furnitureRepository;
    private final FurnitureWarrantyRepository warrantyRepository;
    private final ReportRequestFurnitureRepository reportRequestFurnitureRepository;
    private final FurnitureMapper furnitureMapper;
    private final MaintenanceAccessPolicy accessPolicy;

    @Override
    @Transactional
    public FurnitureResponse createFurniture(FurnitureCreateRequest request) {
        requireRoomManager(request.roomId());
        if (furnitureRepository.existsByRoomIdAndAssetCode(request.roomId(), request.assetCode())) {
            throw new AppException(ErrorCode.FURNITURE_ASSET_CODE_EXISTS);
        }
        Furniture furniture = furnitureRepository.save(furnitureMapper.toEntity(request));
        return furnitureMapper.toResponse(furniture);
    }

    @Override
    @Transactional(readOnly = true)
    public FurnitureResponse getFurniture(UUID furnitureId) {
        return furnitureMapper.toResponse(loadManagedFurniture(furnitureId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<FurnitureResponse> searchFurniture(UUID roomId, FurnitureStatus status, String furnitureType,
            Pageable pageable) {
        requireRoomManager(roomId);
        Page<Furniture> page = furnitureRepository.findAll(
                FurnitureSpecifications.search(roomId, status, furnitureType), withAllowedSort(pageable));
        List<FurnitureResponse> content = page.getContent().stream().map(furnitureMapper::toResponse).toList();
        // PagedResponse numbers pages from 1; Spring Data pages from 0.
        return PagedResponse.of(content, page.getNumber() + 1, page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional
    public FurnitureResponse updateFurniture(UUID furnitureId, FurnitureUpdateRequest request) {
        Furniture furniture = loadManagedFurniture(furnitureId);
        if (request.status() == FurnitureStatus.RETIRED && furniture.getStatus() != FurnitureStatus.RETIRED
                && reportRequestFurnitureRepository.existsByFurnitureIdAndRequestStatusIn(
                        furnitureId, OPEN_REPAIR_STATUSES)) {
            throw new AppException(ErrorCode.FURNITURE_IN_OPEN_REPAIR);
        }
        if (furnitureRepository.existsByRoomIdAndAssetCodeAndIdNot(
                furniture.getRoomId(), request.assetCode(), furnitureId)) {
            throw new AppException(ErrorCode.FURNITURE_ASSET_CODE_EXISTS);
        }
        furnitureMapper.updateEntity(request, furniture);
        return furnitureMapper.toResponse(furnitureRepository.save(furniture));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FurnitureWarrantyResponse> getWarranties(UUID furnitureId) {
        loadManagedFurniture(furnitureId);
        return warrantyRepository.findByFurnitureIdOrderByStartDateDesc(furnitureId).stream()
                .map(furnitureMapper::toWarrantyResponse).toList();
    }

    @Override
    @Transactional
    public FurnitureWarrantyResponse addWarranty(UUID furnitureId, FurnitureWarrantyRequest request) {
        loadManagedFurniture(furnitureId);
        requireValidPeriod(request);
        FurnitureWarranty warranty = furnitureMapper.toWarrantyEntity(request);
        warranty.setFurnitureId(furnitureId);
        return furnitureMapper.toWarrantyResponse(warrantyRepository.save(warranty));
    }

    @Override
    @Transactional
    public FurnitureWarrantyResponse updateWarranty(UUID furnitureId, UUID warrantyId,
            FurnitureWarrantyRequest request) {
        loadManagedFurniture(furnitureId);
        requireValidPeriod(request);
        FurnitureWarranty warranty = loadWarranty(furnitureId, warrantyId);
        furnitureMapper.updateWarranty(request, warranty);
        return furnitureMapper.toWarrantyResponse(warrantyRepository.save(warranty));
    }

    @Override
    @Transactional
    public void deleteWarranty(UUID furnitureId, UUID warrantyId) {
        loadManagedFurniture(furnitureId);
        warrantyRepository.delete(loadWarranty(furnitureId, warrantyId));
    }

    private Furniture loadManagedFurniture(UUID furnitureId) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));
        requireRoomManager(furniture.getRoomId());
        return furniture;
    }

    private FurnitureWarranty loadWarranty(UUID furnitureId, UUID warrantyId) {
        return warrantyRepository.findByIdAndFurnitureId(warrantyId, furnitureId)
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_WARRANTY_NOT_FOUND));
    }

    private void requireRoomManager(UUID roomId) {
        UUID actorId = accessPolicy.currentUserId();
        if (!accessPolicy.canManageRoom(actorId, roomId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    /** Mirrors CHECK(end_date >= start_date) on furniture_warranties. */
    private void requireValidPeriod(FurnitureWarrantyRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new AppException(ErrorCode.INVALID_WARRANTY_PERIOD);
        }
    }

    private Pageable withAllowedSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        return pageable;
    }
}
