package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.dto.LecturerMentoringStatsProjection;
import com.btvn.projectfinal.model.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Thống kê cho Admin: toàn bộ gộp nhóm / sắp xếp / giới hạn TOP 5 xử lý ở DB (không lặp trong Java để cộng dồn).
 */
@Repository
public interface AdminDashboardRepository extends JpaRepository<Appointment, Integer> {

    /**
     * Top 5 giảng viên có nhiều buổi tư vấn ({@code mentoring_sessions}) nhất.
     * <p>SQL: {@code JOIN} + {@code GROUP BY} + {@code COUNT} + {@code ORDER BY} + {@code LIMIT 5}.
     */
    @Query(value = """
            SELECT
                l.id AS lecturerId,
                COALESCE(p.full_name, u.username) AS lecturerName,
                COUNT(ms.id) AS totalSessions
            FROM mentoring_sessions ms
            INNER JOIN lecturers l ON l.id = ms.lecturer_id
            INNER JOIN users u ON u.id = l.user_id
            LEFT JOIN user_profiles p ON p.user_id = u.id
            GROUP BY l.id, u.username, p.full_name
            ORDER BY COUNT(ms.id) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<LecturerMentoringStatsProjection> findTop5LecturersByMentoringSessionCount();
}
