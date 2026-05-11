package com.btvn.projectfinal.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dữ liệu form đặt lịch: Khoa → Giảng viên → Ngày → Khung giờ (bắt đầu / kết thúc).
 */
@Data
public class BookingDTO {

    @NotNull(message = "Vui lòng chọn khoa/ngành")
    private Long departmentId;

    @NotNull(message = "Vui lòng chọn giảng viên")
    private Long lecturerId;

    @NotNull(message = "Vui lòng chọn ngày")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate appointmentDate;

    @NotNull(message = "Vui lòng chọn giờ bắt đầu")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotNull(message = "Vui lòng chọn giờ kết thúc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime endTime;
}
