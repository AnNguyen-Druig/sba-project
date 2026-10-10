package com.sba.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeRequest {

    @NotBlank(message = "Tên loại phòng không được để trống")
    @Size(max = 255, message = "Tên loại phòng tối đa 255 ký tự")
    private String typeName;

    private String description;

    @NotNull(message = "Sức chứa mặc định không được để trống")
    @Positive(message = "Sức chứa mặc định phải lớn hơn 0")
    private Integer defaultCapacity;

    private String features;
}
