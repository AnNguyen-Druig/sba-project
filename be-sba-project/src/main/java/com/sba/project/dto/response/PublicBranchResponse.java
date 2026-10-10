package com.sba.project.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicBranchResponse {

    private UUID branchId;
    private String branchCode;
    private String branchName;
    private String address;
    private String description;
}
