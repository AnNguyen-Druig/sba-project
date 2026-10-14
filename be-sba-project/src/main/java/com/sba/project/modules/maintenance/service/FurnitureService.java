package com.sba.project.modules.maintenance.service;

import com.sba.project.common.base.PagedResponse;
import com.sba.project.modules.maintenance.dto.request.FurnitureCreateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureUpdateRequest;
import com.sba.project.modules.maintenance.dto.request.FurnitureWarrantyRequest;
import com.sba.project.modules.maintenance.dto.response.FurnitureFailureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureResponse;
import com.sba.project.modules.maintenance.dto.response.FurnitureWarrantyResponse;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/** BE-06.3: Furniture/CSVC and warranties (P1-107..P1-110), Owner/Manager of the Room's Branch only. */
public interface FurnitureService {
    FurnitureResponse createFurniture(FurnitureCreateRequest request);

    FurnitureResponse getFurniture(UUID furnitureId);

    PagedResponse<FurnitureResponse> searchFurniture(UUID roomId, FurnitureStatus status, String furnitureType,
            Pageable pageable);

    FurnitureResponse updateFurniture(UUID furnitureId, FurnitureUpdateRequest request);

    List<FurnitureWarrantyResponse> getWarranties(UUID furnitureId);

    FurnitureWarrantyResponse addWarranty(UUID furnitureId, FurnitureWarrantyRequest request);

    FurnitureWarrantyResponse updateWarranty(UUID furnitureId, UUID warrantyId, FurnitureWarrantyRequest request);

    void deleteWarranty(UUID furnitureId, UUID warrantyId);

    /** P2-29: warranties of the room's furniture ending within the next {@code withinDays} days. */
    List<FurnitureWarrantyResponse> getExpiringWarranties(UUID roomId, int withinDays);

    /** P2-29: furniture reported in at least {@code minRequests} repair requests, most reported first. */
    List<FurnitureFailureResponse> getFrequentlyFailing(UUID roomId, int minRequests);
}
