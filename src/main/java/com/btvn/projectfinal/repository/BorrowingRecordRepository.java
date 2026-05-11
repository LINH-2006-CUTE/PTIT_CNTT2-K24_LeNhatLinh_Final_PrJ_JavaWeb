package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.BorrowingRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    @EntityGraph(attributePaths = {
            "student",
            "student.profile",
            "details",
            "details.equipment",
            "mentoringSession"
    })
    @Query("SELECT br FROM BorrowingRecord br WHERE br.status IN :statuses ORDER BY br.createdAt ASC")
    List<BorrowingRecord> findPendingForDispatch(@Param("statuses") List<String> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT br FROM BorrowingRecord br WHERE br.id = :id")
    Optional<BorrowingRecord> findByIdForUpdate(@Param("id") Long id);

    /** Phiếu mượn của sinh viên (xem trạng thái, không tự tạo phiếu trên UI). */
    @EntityGraph(attributePaths = {"details", "details.equipment", "mentoringSession"})
    List<BorrowingRecord> findByStudent_IdOrderByCreatedAtDesc(Long studentId);

    List<BorrowingRecord> findByMentoringSession_Id(Integer mentoringSessionId);
}
