package com.sba.project.modules.maintenance.service;

import com.sba.project.modules.maintenance.dto.request.HandoverRequest;
import com.sba.project.modules.maintenance.dto.response.HandoverResponse;
import java.util.UUID;

/** BE-06.6 furniture handover for a rental member (P2-19), stored on contract_tenants. */
public interface HandoverService {
    /** Owner/Manager of the member's room records the handover once. */
    HandoverResponse recordHandover(UUID contractTenantId, HandoverRequest request);

    /** The member or the room's Owner/Manager. */
    HandoverResponse getHandover(UUID contractTenantId);
}
