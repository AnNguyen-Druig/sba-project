package com.sba.project.modules.property.dto.response;

import com.sba.project.modules.property.enums.BranchStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchAssignmentResponse {
    private UUID id;
    private UUID ownerId;
    private UUID managerId;
    private String name;
    private String address;
    private BranchStatus status;
    private String managerName;
    private String zaloUrl;
    private Boolean zaloEnabled;
}
