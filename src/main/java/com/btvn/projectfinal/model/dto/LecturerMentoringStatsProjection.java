package com.btvn.projectfinal.model.dto;

/**
 * Một dòng kết quả thống kê: giảng viên + tổng số buổi tư vấn (đếm trực tiếp trong SQL).
 */
public interface LecturerMentoringStatsProjection {

    Long getLecturerId();

    String getLecturerName();

    Long getTotalSessions();
}
