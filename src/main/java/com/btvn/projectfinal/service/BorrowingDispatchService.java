package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.BorrowingRecordStatus;
import com.btvn.projectfinal.model.entity.BorrowingDetail;
import com.btvn.projectfinal.model.entity.BorrowingRecord;
import com.btvn.projectfinal.model.entity.Equipment;
import com.btvn.projectfinal.repository.BorrowingRecordRepository;
import com.btvn.projectfinal.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
@Service
@RequiredArgsConstructor
public class BorrowingDispatchService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional(readOnly = true)
    public List<BorrowingRecord> listAwaitingDispatch() {
        return borrowingRecordRepository.findPendingForDispatch(List.of(
                BorrowingRecordStatus.CHO_CAP_PHAT,
                BorrowingRecordStatus.WAITING_APPROVAL));
    }

    /**
     * Xác nhận xuất kho (một transaction).
     * <p><b>Idempotency:</b> nếu phiếu đã {@code Đã cấp phát} → thoát ngay, không trừ kho lần 2
     * (chống double-click / gửi trùng request).
     * <p><b>Check-then-act:</b> khóa phiếu mượn trước; gom nhu cầu theo từng {@code equipment_id};
     * khóa từng dòng {@link Equipment} theo thứ tự {@code id} tăng dần (tránh deadlock);
     * kiểm tra đủ tồn cho <em>tất cả</em> món; chỉ khi đủ mới trừ — nếu thiếu một món thì ném lỗi
     * và rollback toàn bộ (không trừ lẻ).
     */
    /**
     * @return {@code true} nếu vừa trừ kho và đổi trạng thái; {@code false} nếu phiếu đã cấp phát trước đó (idempotent).
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDispatch(Long borrowingRecordId) {

        BorrowingRecord record = borrowingRecordRepository.findByIdForUpdate(borrowingRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));

        if (BorrowingRecordStatus.DA_CAP_PHAT.equals(record.getStatus())) {
            return false;
        }

        if (!isAwaitingDispatch(record.getStatus())) {
            throw new IllegalStateException(
                    "Phiếu không ở trạng thái chờ cấp phát (hiện tại: " + record.getStatus() + ").");
        }

        List<BorrowingDetail> lines = record.getDetails();
        if (lines == null || lines.isEmpty()) {
            throw new IllegalStateException("Phiếu không có chi tiết thiết bị.");
        }

        Map<Long, Integer> needPerEquipment = new LinkedHashMap<>();
        for (BorrowingDetail line : lines) {
            Long eqId = line.getEquipment().getId();
            needPerEquipment.merge(eqId, line.getQuantity(), Integer::sum);
        }

        TreeSet<Long> sortedIds = new TreeSet<>(needPerEquipment.keySet());
        Map<Long, Equipment> locked = new LinkedHashMap<>();
        for (Long eqId : sortedIds) {
            Equipment e = equipmentRepository.findByIdForUpdate(eqId)
                    .orElseThrow(() -> new IllegalStateException("Thiết bị id=" + eqId + " không tồn tại."));
            locked.put(eqId, e);
        }

        List<String> shortOnStock = new ArrayList<>();
        for (Long eqId : sortedIds) {
            Equipment e = locked.get(eqId);
            int need = needPerEquipment.get(eqId);
            if (e.getQuantity() < need) {
                shortOnStock.add(e.getName() + " (cần " + need + ", tồn " + e.getQuantity() + ")");
            }
        }

        if (!shortOnStock.isEmpty()) {
            throw new IllegalStateException(
                    "Không đủ tồn kho: " + String.join("; ", shortOnStock));
        }

        for (Long eqId : sortedIds) {
            Equipment e = locked.get(eqId);
            int need = needPerEquipment.get(eqId);
            e.setQuantity(e.getQuantity() - need);
        }

        record.setStatus(BorrowingRecordStatus.DA_CAP_PHAT);
        borrowingRecordRepository.save(record);
        return true;
    }

    private static boolean isAwaitingDispatch(String status) {
        return BorrowingRecordStatus.CHO_CAP_PHAT.equals(status)
                || BorrowingRecordStatus.WAITING_APPROVAL.equals(status);
    }
}
