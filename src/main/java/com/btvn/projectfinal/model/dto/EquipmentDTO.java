package com.btvn.projectfinal.model.dto;

import com.btvn.projectfinal.model.entity.Equipment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EquipmentDTO {
    private Long id;

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(max = 150, message = "Tên thiết bị không được vượt quá 150 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    /** Để trống → hệ thống dùng "cái". */
    @Size(max = 50, message = "Đơn vị không quá 50 ký tự")
    private String unit;

    private Equipment.EquipmentStatus status;

    /** Gán khoa (chọn từ dữ liệu nền — chỉ đọc danh sách). */
    private Long departmentId;

    /** Gán loại phòng lab (dữ liệu nền). */
    private Long labRoomTypeId;
}
