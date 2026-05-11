package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.BorrowingRecordStatus;
import com.btvn.projectfinal.model.MentoringSessionStatus;
import com.btvn.projectfinal.model.dto.CompletedConsultationProjection;
import com.btvn.projectfinal.model.dto.EvaluationRequestDTO;
import com.btvn.projectfinal.model.entity.*;
import com.btvn.projectfinal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerConsultationService {

    private final BookingRepository bookingRepository;
    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final EquipmentRepository equipmentRepository;

    /** Danh sách chờ: Pending, thời gian sớm nhất trước. */
    @Transactional(readOnly = true)
    public List<Appointment> listPendingSessions(Long lecturerId) {
        return bookingRepository.findByLecturer_IdAndStatusOrderByAppointmentDateAscStartTimeAsc(
                lecturerId, MentoringSessionStatus.PENDING);
    }

    /** Lịch sử đã hoàn thành + phân trang. */
    @Transactional(readOnly = true)
    public Page<CompletedConsultationProjection> listCompletedHistory(Long lecturerId, int page, int size) {
        /* Native query đã có ORDER BY; không truyền Sort vào Pageable để tránh xung đột với SQL. */
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findCompletedConsultationHistory(lecturerId, pageable);
    }

    /**
     * Hoàn tất đánh giá buổi cố vấn (một transaction duy nhất).
     * <p>B1–B4 cùng commit hoặc cùng rollback nếu có lỗi (ví dụ ID thiết bị sai ở B4).
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeEvaluation(Long lecturerId, EvaluationRequestDTO request) {

        Appointment session = bookingRepository
                .findByIdAndLecturer_Id(request.getSessionId(), lecturerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy buổi tư vấn hoặc buổi này không thuộc về bạn."));

        if (!MentoringSessionStatus.PENDING.equals(session.getStatus())) {
            throw new IllegalStateException("Buổi tư vấn không ở trạng thái Pending (đã xử lý hoặc đã hủy).");
        }

        if (academicEvaluationRepository.existsByMentoringSession_Id(session.getId())) {
            throw new IllegalStateException("Buổi tư vấn đã được đánh giá trước đó.");
        }

        session.setStatus(MentoringSessionStatus.COMPLETED);
        bookingRepository.save(session);

        AcademicEvaluation evaluation = new AcademicEvaluation();
        evaluation.setMentoringSession(session);
        evaluation.setEvaluationContent(request.getComment().trim());
        academicEvaluationRepository.save(evaluation);

        List<Long> equipmentIds = normalizeEquipmentIds(request.getListEquipmentIds());

        if (equipmentIds.isEmpty()) {
            return;
        }

        List<Equipment> equipments = equipmentRepository.findAllById(equipmentIds);
        if (equipments.size() != equipmentIds.size()) {
            throw new IllegalArgumentException("Có mã thiết bị không tồn tại trong hệ thống.");
        }

        BorrowingRecord record = new BorrowingRecord();
        record.setStudent(session.getStudent());
        record.setMentoringSession(session);
        record.setStatus(BorrowingRecordStatus.WAITING_APPROVAL);
        borrowingRecordRepository.save(record);

        for (Equipment equipment : equipments) {
            BorrowingDetail line = new BorrowingDetail();
            line.setBorrowingRecord(record);
            line.setEquipment(equipment);
            line.setQuantity(1);
            borrowingDetailRepository.save(line);
        }
    }

    private static List<Long> normalizeEquipmentIds(List<Long> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        return raw.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
