package com.sba.project.modules.maintenance.specifications;

import com.sba.project.modules.maintenance.entity.Furniture;
import com.sba.project.modules.maintenance.enums.FurnitureStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/** P1-109 filters: room, status and asset type. */
public final class FurnitureSpecifications {
    private FurnitureSpecifications() {
    }

    public static Specification<Furniture> search(UUID roomId, FurnitureStatus status, String furnitureType) {
        List<Specification<Furniture>> filters = new ArrayList<>();
        filters.add(inRoom(roomId));
        if (status != null) filters.add(hasStatus(status));
        if (furnitureType != null && !furnitureType.isBlank()) filters.add(hasType(furnitureType));
        return Specification.allOf(filters);
    }

    public static Specification<Furniture> inRoom(UUID roomId) {
        return (root, query, cb) -> cb.equal(root.get("roomId"), roomId);
    }

    public static Specification<Furniture> hasStatus(FurnitureStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Furniture> hasType(String furnitureType) {
        String normalized = furnitureType.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("furnitureType")), normalized);
    }
}
