package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.entity.Appointment;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.repository.AppointmentRepository;
import com.btvn.projectfinal.repository.LecturerRepository;
import com.btvn.projectfinal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final LecturerRepository lecturerRepository;
    private final UserRepository userRepository;

    /**
     * Đặt lịch cố vấn: khóa hàng {@code lecturers} theo id để serialize các transaction cùng giảng viên,
     * sau đó kiểm tra chồng lấn khung giờ trong DB.
     */
    @Transactional(rollbackFor = Exception.class)
    public Appointment bookSession(
            Long studentId,
            Long lecturerId,
            Long departmentId,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime) {

        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Khung giờ không hợp lệ: giờ bắt đầu phải trước giờ kết thúc.");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (appointmentDate.isBefore(today)) {
            throw new IllegalArgumentException("Ngày hẹn không hợp lệ (đã trôi qua).");
        }

        if (appointmentDate.isEqual(today) && startTime.isBefore(now)) {
            throw new IllegalArgumentException("Giờ bắt đầu không thể ở quá khứ.");
        }

        Lecturer lecturer = lecturerRepository.findByIdForUpdate(lecturerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giảng viên."));

        if (lecturer.getDepartment() == null
                || !lecturer.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Giảng viên không thuộc khoa đã chọn.");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));

        if (appointmentRepository.countOverlapping(
                lecturerId,
                appointmentDate,
                startTime,
                endTime,
                null) > 0) {
            throw new IllegalStateException("Giảng viên đã có lịch trùng khung giờ này.");
        }

        Appointment appointment = new Appointment();
        appointment.setStudent(student);
        appointment.setLecturer(lecturer);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);

        return appointmentRepository.save(appointment);
    }
}
