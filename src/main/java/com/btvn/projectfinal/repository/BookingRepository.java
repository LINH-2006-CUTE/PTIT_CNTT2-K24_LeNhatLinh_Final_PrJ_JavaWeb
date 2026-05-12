package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.dto.CompletedConsultationProjection;
import com.btvn.projectfinal.model.dto.ConsultationHistoryProjection;
import com.btvn.projectfinal.model.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByStudent_IdOrderByAppointmentDateDescStartTimeDesc(Long studentId);

    /**
     * Id các buổi tư vấn của giảng viên (JPQL rõ ràng — tránh derived query {@code findByLecturer_Id} không khớp).
     */
    @Query("SELECT a.id FROM Appointment a WHERE a.lecturer.id = :lecturerId")
    List<Integer> findSessionIdsByLecturerId(@Param("lecturerId") Long lecturerId);

    Optional<Appointment> findByIdAndStudent_Id(Integer id, Long studentId);

    @EntityGraph(attributePaths = {"student", "student.profile", "lecturer", "lecturer.user"})
    Optional<Appointment> findByIdAndLecturer_Id(Integer id, Long lecturerId);

    /**
     * Ca chờ giảng viên xử lý (Pending + trạng thái cũ tiếng Việt nếu DB chưa migrate).
     */
    @EntityGraph(attributePaths = {"student", "student.profile"})
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.lecturer.id = :lecturerId AND a.status IN :statuses
            ORDER BY a.appointmentDate ASC, a.startTime ASC
            """)
    List<Appointment> findPendingSessionsForLecturer(
            @Param("lecturerId") Long lecturerId,
            @Param("statuses") List<String> statuses);

    /**
     * Lịch sử các ca đã hoàn thành của một giảng viên (có đánh giá).
     */
    @Query(value = """
            SELECT
                COALESCE(sup.full_name, su.username) AS studentName,
                ms.appointment_date AS appointmentDate,
                ms.start_time AS startTime,
                ms.end_time AS endTime,
                ae.evaluation_content AS evaluationContent
            FROM mentoring_sessions ms
            INNER JOIN users su ON su.id = ms.student_id
            LEFT JOIN user_profiles sup ON sup.user_id = su.id
            INNER JOIN academic_evaluations ae ON ae.mentoring_session_id = ms.id
            WHERE ms.lecturer_id = :lecturerId AND ms.status = 'Completed'
            ORDER BY ms.appointment_date DESC, ms.start_time DESC
            """,
            countQuery = """
                    SELECT COUNT(ms.id)
                    FROM mentoring_sessions ms
                    INNER JOIN academic_evaluations ae ON ae.mentoring_session_id = ms.id
                    WHERE ms.lecturer_id = :lecturerId AND ms.status = 'Completed'
                    """,
            nativeQuery = true)
    Page<CompletedConsultationProjection> findCompletedConsultationHistory(
            @Param("lecturerId") Long lecturerId,
            Pageable pageable);

    /**
     * Kiểm tra trùng khung giờ (cùng ngày + cùng start/end) khi ca vẫn {@code Pending}.
     * Dùng JPQL {@code COUNT} thay vì native {@code EXISTS}: driver MySQL thường trả EXISTS dạng số (Long),
     * Spring Data lại ép kiểu {@code boolean} → gây lỗi ClassCastException.
     */
    @Query("""
            SELECT COUNT(a) FROM Appointment a
            WHERE a.lecturer.id = :lecturerId
              AND a.appointmentDate = :appointmentDate
              AND a.startTime = :startTime
              AND a.endTime = :endTime
              AND a.status IN :activeStatuses
            """)
    long countActiveBookingAtSameSlot(
            @Param("lecturerId") Long lecturerId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") List<String> activeStatuses);

    /**
     * Tra cứu lịch sử tư vấn của một sinh viên (JOIN + GROUP_CONCAT trong DB, không gộp dữ liệu bằng vòng lặp Java).
     * Thứ tự JOIN: lọc theo {@code ms.student_id} trước (giảm bảng tạm), rồi mới LEFT JOIN các bảng phụ.
     */
    @Query(value = """
            SELECT
                ms.appointment_date AS consultationDate,
                MAX(COALESCE(up.full_name, u.username)) AS lecturerName,
                MAX(ae.evaluation_content) AS evaluationContent,
                GROUP_CONCAT(DISTINCT e.name ORDER BY e.name SEPARATOR ', ') AS borrowedEquipmentList
            FROM mentoring_sessions ms
            INNER JOIN lecturers l ON l.id = ms.lecturer_id
            INNER JOIN users u ON u.id = l.user_id
            LEFT JOIN user_profiles up ON up.user_id = u.id
            LEFT JOIN academic_evaluations ae ON ae.mentoring_session_id = ms.id
            LEFT JOIN borrowing_records br ON br.mentoring_session_id = ms.id
            LEFT JOIN borrowing_details bd ON bd.borrowing_record_id = br.id
            LEFT JOIN equipments e ON e.id = bd.equipment_id
            WHERE ms.student_id = :studentId
            GROUP BY ms.id
            ORDER BY ms.appointment_date DESC, ms.start_time DESC
            """, nativeQuery = true)
    List<ConsultationHistoryProjection> findConsultationHistoryByStudentId(@Param("studentId") Long studentId);
}
