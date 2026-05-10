package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByStudentIdOrderByAppointmentDateDescStartTimeDesc(Long studentId);

    /**
     * Hai khoảng thời gian giao nhau khi: bắt đầu A &lt; kết thúc B và bắt đầu B &lt; kết thúc A.
     * Bỏ qua bản ghi hiện tại khi cập nhật (excludeId).
     */
    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.lecturer.id = :lecturerId
              AND a.appointmentDate = :date
              AND a.startTime < :endTime
              AND a.endTime > :startTime
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    long countOverlapping(
            @Param("lecturerId") Long lecturerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Integer excludeId);
}