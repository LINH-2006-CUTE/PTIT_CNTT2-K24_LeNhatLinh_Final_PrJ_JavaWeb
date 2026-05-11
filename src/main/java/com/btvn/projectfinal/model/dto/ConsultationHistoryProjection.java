package com.btvn.projectfinal.model.dto;

import java.time.LocalDate;

/**
 * Kết quả truy vấn JOIN (native query): một dòng = một buổi tư vấn + đánh giá + danh sách thiết bị (đã gộp).
 */
public interface ConsultationHistoryProjection {

    LocalDate getConsultationDate();

    String getLecturerName();

    String getEvaluationContent();

    /** Chuỗi tên thiết bị (GROUP_CONCAT), có thể {@code null} nếu không mượn gì. */
    String getBorrowedEquipmentList();
}
