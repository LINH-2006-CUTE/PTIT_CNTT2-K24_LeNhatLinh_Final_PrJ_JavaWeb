package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.MentoringSessionStatus;
import com.btvn.projectfinal.model.dto.BookingDTO;
import com.btvn.projectfinal.model.entity.Appointment;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.repository.BookingRepository;
import com.btvn.projectfinal.repository.LecturerRepository;
import com.btvn.projectfinal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final BookingRepository bookingRepository;
    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;

    /**
     * Đặt lịch cố vấn (CORE-05).
     * <p>
     * Tránh “điểm mù” khi 2 sinh viên bấm đặt cùng lúc: dùng transaction + {@code SELECT … FOR UPDATE}
     * trên dòng {@code lecturers} (đã có trong {@link LecturerRepository#findByIdForUpdate(Long)})
     * để các request cùng giảng viên xếp hàng; sau đó COUNT kiểm tra trùng slot trước khi INSERT.
     * Có thể nâng cấp thêm unique index (lecturer_id, appointment_date, start_time, end_time) + {@link Isolation#REPEATABLE_READ}.
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.REPEATABLE_READ)
    public Appointment bookSession(Long studentId, BookingDTO dto) {

        LocalDate appointmentDate = dto.getAppointmentDate();
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();
        Long lecturerId = dto.getLecturerId();
        Long departmentId = dto.getDepartmentId();

        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ: giờ bắt đầu phải trước giờ kết thúc.");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (appointmentDate.isBefore(today)) {
            throw new IllegalArgumentException("Ngày đặt lịch phải lớn hơn hoặc bằng ngày hiện tại. Không được đặt lịch trong quá khứ.");
        }

        if (appointmentDate.isEqual(today) && startTime.isBefore(now)) {
            throw new IllegalArgumentException("Không được đặt lịch trong quá khứ.");
        }

        Lecturer lecturer = lecturerRepository.findByIdForUpdate(lecturerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giảng viên."));

        if (lecturer.getDepartment() == null
                || !lecturer.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Giảng viên không thuộc khoa đã chọn.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));

        if (bookingRepository.countActiveBookingAtSameSlot(
                lecturerId, appointmentDate, startTime, endTime) > 0) {
            throw new IllegalStateException("Giảng viên đã có lịch");
        }

        Appointment appointment = new Appointment();
        appointment.setStudent(student);
        appointment.setLecturer(lecturer);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(MentoringSessionStatus.PENDING);

        return bookingRepository.save(appointment);
    }

    /**
     * Hủy lịch (CORE-09): chỉ được hủy trước giờ hẹn ít nhất 24 giờ; trạng thái → Cancelled.
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelSession(Long studentId, Integer appointmentId) {
        Appointment appointment = bookingRepository
                .findByIdAndStudent_Id(appointmentId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn của bạn."));

        if (MentoringSessionStatus.CANCELLED.equals(appointment.getStatus())) {
            throw new IllegalStateException("Lịch này đã được hủy trước đó.");
        }

        LocalDateTime appointmentStart = LocalDateTime.of(
                appointment.getAppointmentDate(), appointment.getStartTime());
        LocalDateTime now = LocalDateTime.now();

        if (!appointmentStart.isAfter(now)) {
            throw new IllegalStateException("Không thể hủy lịch đã diễn ra hoặc đã qua.");
        }

        LocalDateTime earliestAllowedCancel = now.plusHours(24);

        if (appointmentStart.isBefore(earliestAllowedCancel)) {
            throw new IllegalStateException("Không thể hủy lịch sát giờ hẹn");
        }

        appointment.setStatus(MentoringSessionStatus.CANCELLED);
        bookingRepository.save(appointment);
    }
}
