package com.sba.project.modules.leasing.service;

import java.util.UUID;

/**
 * Integration contract implemented by Student 5 (billing/payment): whether the member's move-in
 * invoice (first-month rent, first-month fixed services, missing contract deposit) is verified as
 * fully paid. One of the BR-P14 conditions for a ContractTenant to start renting.
 */
public interface MoveInPaymentPort {
    boolean isMoveInInvoiceSettled(UUID contractTenantId);
}
