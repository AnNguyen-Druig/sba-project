package com.sba.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Size(max = 255, message = "Trạng thái tối đa 255 ký tự")
    private String status;

    private String note;
}
