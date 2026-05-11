package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.entity.BorrowingRecord;
import com.btvn.projectfinal.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentBorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;

    @Transactional(readOnly = true)
    public List<BorrowingRecord> listForStudent(Long studentUserId) {
        return borrowingRecordRepository.findByStudent_IdOrderByCreatedAtDesc(studentUserId);
    }
}
