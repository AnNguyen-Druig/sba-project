package com.sba.project.modules.leasing.service.impl;

import com.sba.project.modules.leasing.service.MoveInPaymentPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** TEMPORARY fail-closed placeholder until Student 5's move-in invoice is merged: nobody has paid. */
@Component
public class PendingPaymentMoveInPort implements MoveInPaymentPort {
    @Override
    public boolean isMoveInInvoiceSettled(UUID contractTenantId) {
        return false;
    }
}
