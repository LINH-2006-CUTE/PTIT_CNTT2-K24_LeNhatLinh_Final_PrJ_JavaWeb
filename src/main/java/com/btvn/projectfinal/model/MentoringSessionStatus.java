package com.btvn.projectfinal.model;

import java.util.List;

public final class MentoringSessionStatus {

    public static final String PENDING = "Pending";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";
    public static final String LEGACY_CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String LEGACY_DA_XAC_NHAN = "Đã xác nhận";

    private MentoringSessionStatus() {
    }

    /** Ca vẫn chiếm khung giờ GV (không cho đặt trùng). */
    public static boolean isBlockingSlot(String status) {
        return PENDING.equals(status)
                || LEGACY_CHO_XAC_NHAN.equals(status)
                || LEGACY_DA_XAC_NHAN.equals(status);
    }

    /** Ca hiển thị trong màn “Chờ xử lý” của giảng viên (chưa đánh giá xong). */
    public static boolean isAwaitingLecturerEvaluation(String status) {
        return PENDING.equals(status)
                || LEGACY_CHO_XAC_NHAN.equals(status)
                || LEGACY_DA_XAC_NHAN.equals(status);
    }

    public static List<String> statusesBlockingSlot() {
        return List.of(PENDING, LEGACY_CHO_XAC_NHAN, LEGACY_DA_XAC_NHAN);
    }

    public static List<String> statusesAwaitingLecturer() {
        return List.of(PENDING, LEGACY_CHO_XAC_NHAN, LEGACY_DA_XAC_NHAN);
    }
}
