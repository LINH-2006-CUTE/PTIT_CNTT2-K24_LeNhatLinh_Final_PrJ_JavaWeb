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
        /* Chỉ phiếu đúng luồng SRS: GV chỉ định xong → "Chờ cấp phát" → Admin xác nhận. */
        return borrowingRecordRepository.findPendingForDispatch(
                List.of(BorrowingRecordStatus.CHO_CAP_PHAT));
    }

    /**
     * Xác nhận xuất kho (CORE-08) — một transaction {@code @Transactional}.
     * <p><b>Idempotency (chống bấm đúp):</b> nếu phiếu đã ở trạng thái {@code Đã cấp phát}
     * thì trả về {@code false} ngay, <em>không</em> trừ kho thêm lần nữa.
     * <p><b>Check-then-act (toàn vẹn tồn kho):</b>
     * <ol>
     *   <li>{@code SELECT ... FOR UPDATE} trên phiếu mượn → các request khác chờ, tránh xử lý song song cùng một phiếu.</li>
     *   <li>Gom số lượng cần theo từng {@code equipment_id} (nhiều dòng chi tiết cùng thiết bị).</li>
     *   <li>Khóa từng dòng {@link Equipment} theo {@code id} tăng dần → giảm deadlock khi nhiều phiếu khác nhau.</li>
     *   <li>Chỉ sau khi <em>tất cả</em> món đều đủ tồn mới trừ kho + đổi trạng thái phiếu; nếu thiếu một món
     *       thì ném {@link IllegalStateException} → Spring rollback toàn bộ (không trừ lẻ từng món).</li>
     * </ol>
     *
     * @return {@code true} nếu vừa trừ kho và đổi trạng thái; {@code false} nếu phiếu đã cấp phát trước đó (idempotent).
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDispatch(Long borrowingRecordId) {

        BorrowingRecord record = borrowingRecordRepository.findByIdForUpdate(borrowingRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));

        if (BorrowingRecordStatus.DA_CAP_PHAT.equals(record.getStatus())) {
            return false;
        }

        if (!BorrowingRecordStatus.CHO_CAP_PHAT.equals(record.getStatus())) {
            throw new IllegalStateException(
                    "Phiếu phải ở trạng thái \"" + BorrowingRecordStatus.CHO_CAP_PHAT
                            + "\" mới được xác nhận xuất kho (hiện tại: " + record.getStatus() + ").");
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
}
