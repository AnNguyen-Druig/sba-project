package com.sba.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {

    @NotNull(message = "Branch ID không được để trống")
    private UUID branchId;

    @NotNull(message = "Manager ID không được để trống")
    private UUID managerId;

    @NotNull(message = "Loại phòng không được để trống")
    private UUID roomTypeId;

    @NotBlank(message = "Mã phòng không được để trống")
    @Size(max = 255, message = "Mã phòng tối đa 255 ký tự")
    private String roomCode;

    @NotNull(message = "Giá tham chiếu không được để trống")
    @PositiveOrZero(message = "Giá tham chiếu không được âm")
    private BigDecimal referencePrice;

    @NotNull(message = "Sức chứa không được để trống")
    @Positive(message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 255, message = "Trạng thái tối đa 255 ký tự")
    private String status;

    private String description;
}
