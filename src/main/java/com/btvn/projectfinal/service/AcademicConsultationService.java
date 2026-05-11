package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.ConsultationHistoryProjection;
import com.btvn.projectfinal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicConsultationService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<ConsultationHistoryProjection> getConsultationHistory(Long studentId) {
        return bookingRepository.findConsultationHistoryByStudentId(studentId);
    }
}
