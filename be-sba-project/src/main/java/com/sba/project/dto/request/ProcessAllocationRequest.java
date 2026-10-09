package com.sba.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessAllocationRequest {

    @NotNull(message = "Trạng thái xử lý không được để trống")
    private Boolean approved; // true = approve, false = reject

    private String rejectionReason;
}
