package com.btvn.projectfinal.model;

public final class MentoringSessionStatus {
    public static final String PENDING = "Pending";
    public static final String COMPLETED = "Completed";
    public static final String CANCELLED = "Cancelled";

    private MentoringSessionStatus() {
    }
    public static boolean isBlockingSlot(String status) {
        return PENDING.equals(status);
    }
}
