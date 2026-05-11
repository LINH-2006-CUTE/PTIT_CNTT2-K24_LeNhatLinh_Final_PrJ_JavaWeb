package com.btvn.projectfinal.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Một dòng lịch sử tư vấn đã hoàn thành (truy vấn JOIN cho giảng viên).
 */
public interface CompletedConsultationProjection {

    String getStudentName();

    LocalDate getAppointmentDate();

    LocalTime getStartTime();

    LocalTime getEndTime();

    String getEvaluationContent();
}
