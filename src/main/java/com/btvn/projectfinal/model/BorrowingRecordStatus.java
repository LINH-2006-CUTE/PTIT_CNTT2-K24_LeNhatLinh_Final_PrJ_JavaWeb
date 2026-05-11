package com.btvn.projectfinal.model;

/**
 * Trạng thái phiếu mượn: giảng viên chỉ định thiết bị → chờ kho → admin xác nhận xuất.
 */
public final class BorrowingRecordStatus {

    /** @deprecated Dùng {@link #CHO_CAP_PHAT}; giữ để tương thích dữ liệu cũ nếu có. */
    @Deprecated
    public static final String WAITING_APPROVAL = "Waiting_Approval";

    /** Sau khi GV ghi nhận thiết bị, chờ Admin xác nhận xuất kho (CORE-08). */
    public static final String CHO_CAP_PHAT = "Chờ cấp phát";

    /** Admin đã trừ tồn kho thành công. */
    public static final String DA_CAP_PHAT = "Đã cấp phát";

    private BorrowingRecordStatus() {
    }
}
