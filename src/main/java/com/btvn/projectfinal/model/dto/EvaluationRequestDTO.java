package com.btvn.projectfinal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Giảng viên gửi sau buổi cố vấn: đánh giá + (tuỳ chọn) thiết bị cho sinh viên mượn.
 */
@Data
public class EvaluationRequestDTO {

    @NotNull(message = "Thiếu mã buổi tư vấn")
    private Integer sessionId;

    @NotBlank(message = "Vui lòng nhập nội dung đánh giá")
    private String comment;

    /** Để trống hoặc không chọn thiết bị → chỉ lưu đánh giá, không tạo phiếu mượn. */
    private List<Long> listEquipmentIds = new ArrayList<>();
}
