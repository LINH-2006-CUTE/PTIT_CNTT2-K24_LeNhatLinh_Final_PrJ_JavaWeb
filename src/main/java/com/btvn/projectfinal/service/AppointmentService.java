package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.entity.Appointment;
import com.btvn.projectfinal.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Appointment addAppointment(Appointment appointment) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (appointment.getAppointmentDate().isBefore(today)) {
            throw new RuntimeException("Ngày hẹn không hợp lệ (đã trôi qua)!");
        }

        if (appointment.getAppointmentDate().isEqual(today)
                && appointment.getStartTime().isBefore(now)) {
            throw new RuntimeException("Giờ bắt đầu không thể ở quá khứ!");
        }

//        boolean isConflict = appointmentRepository.existsConflict(
//                appointment.getLecturer().getId(),
//                appointment.getAppointmentDate(),
//                appointment.getStartTime(),
//                appointment.getEndTime()
//        );
//        if (isConflict) {
////            throw new RuntimeException("Giảng viên đã có lịch trong khung giờ này!");
////        }
////

        return appointmentRepository.save(appointment);
    }
}
