package com.sba.project.modules.maintenance.service.impl;

import com.sba.project.modules.maintenance.dto.request.EvidenceRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/** Builds the evidence_refs JSON entries (image references only, never binaries or secrets). */
final class EvidenceReferences {
    private EvidenceReferences() {
    }

    static ObjectNode entry(EvidenceRequest evidence, UUID addedBy) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("public_id", evidence.publicId().trim());
        node.put("resource_type", evidence.resourceType().trim());
        node.put("stage", evidence.stage().name());
        node.put("added_by", addedBy.toString());
        node.put("added_at", Instant.now().toString());
        return node;
    }

    static JsonNode array(List<EvidenceRequest> evidence, UUID addedBy) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        if (evidence != null) evidence.forEach(item -> array.add(entry(item, addedBy)));
        return array;
    }
}
