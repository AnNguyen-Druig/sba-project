package com.sba.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomSlotRequest {

    @NotNull(message = "Room ID không được để trống")
    private UUID roomId;

    @NotBlank(message = "Mã slot không được để trống")
    @Size(max = 255, message = "Mã slot tối đa 255 ký tự")
    private String slotCode;

    @NotBlank(message = "Tên slot không được để trống")
    @Size(max = 255, message = "Tên slot tối đa 255 ký tự")
    private String slotName;

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 255, message = "Trạng thái tối đa 255 ký tự")
    private String status;

    private String description;
}
