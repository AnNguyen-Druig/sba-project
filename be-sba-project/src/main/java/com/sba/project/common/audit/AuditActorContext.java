package com.sba.project.common.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Binds the authenticated actor to the current transaction (rental.actor_id) so database audit and
 * guard triggers attribute writes. Call inside the transaction; never pass a client-supplied id.
 */
@Component
public class AuditActorContext {
    @PersistenceContext
    private EntityManager entityManager;

    public void bind(UUID actorId) {
        entityManager.createNativeQuery("select set_config('rental.actor_id', :actor, true)")
                .setParameter("actor", actorId.toString()).getSingleResult();
    }

    /** Reason recorded by audit_change() for every audited write in the current transaction. */
    public void bindReason(String reason) {
        entityManager.createNativeQuery("select set_config('rental.reason', :reason, true)")
                .setParameter("reason", reason == null ? "" : reason).getSingleResult();
    }
}
