package com.btvn.projectfinal.model.dto;

import java.time.LocalDate;

public interface ConsultationHistoryProjection {

    LocalDate getConsultationDate();

    String getLecturerName();

    String getEvaluationContent();

    String getBorrowedEquipmentList();
}
