package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.Lecturer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    List<Lecturer> findByDepartmentId(Long departmentId);

    Optional<Lecturer> findByUser_Id(Long userId);

    /**
     * Khóa theo giảng viên để tránh hai request đặt lịch cùng lúc ghi trùng khung giờ (MySQL InnoDB row lock).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lecturer l WHERE l.id = :id")
    Optional<Lecturer> findByIdForUpdate(@Param("id") Long id);
}