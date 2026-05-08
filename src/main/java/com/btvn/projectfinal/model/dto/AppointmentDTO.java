package com.btvn.projectfinal.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentDTO {
    @NotNull(message = "Ngày hẹn không được để trống")
    @FutureOrPresent(message = "Ngày hẹn phải là ngày hiện tại hoặc tương lai")
    private LocalDate appointmentDate;

    private LocalTime startTime;
}