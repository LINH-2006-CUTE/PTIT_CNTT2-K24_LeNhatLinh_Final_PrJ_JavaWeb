package com.btvn.projectfinal.controller.student;

import com.btvn.projectfinal.model.entity.Appointment;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.repository.AppointmentRepository;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.LecturerRepository;
import com.btvn.projectfinal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/student/appointments")
@RequiredArgsConstructor
public class StudentAppointmentController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final AppointmentRepository appointmentRepository;
    @GetMapping("/new")
    public String showBookingPage(
            @RequestParam(required = false) Integer departmentId,
            Model model,
            Principal principal) {
        User student = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        model.addAttribute("student", student);

        model.addAttribute("departments", departmentRepository.findAll());

        if (departmentId != null) {
            List<Lecturer> lecturers = lecturerRepository.findByDepartmentId(departmentId);
            model.addAttribute("lecturers", lecturers);

            model.addAttribute("selectedDeptId", departmentId);
        }
        return "student/booking";
    }


    @PostMapping("/save")
    public String saveAppointment(
            @RequestParam Long lecturerId,
            @RequestParam String appointmentDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User student = userRepository.findByUsername(principal.getName()).get();
            // tim gv theo id
            Lecturer lecturer = lecturerRepository.findById(lecturerId).get();

            Appointment app = new Appointment();
            app.setStudent(student);
            app.setLecturer(lecturer);
            app.setAppointmentDate(LocalDate.parse(appointmentDate));
            app.setStartTime(LocalTime.parse(startTime));
            app.setEndTime(LocalTime.parse(endTime));

            appointmentRepository.save(app);
            redirectAttributes.addFlashAttribute("message", "Đặt lịch thành công!");
            return "redirect:/student/appointments/my-appointments";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/student/appointments/new";
        }
    }
    @GetMapping("/my-appointments")
    public String listAppointments(Model model, Principal principal) {
        User student = userRepository.findByUsername(principal.getName()).get();
        List<Appointment> list = appointmentRepository.findByStudentId(student.getId());
        model.addAttribute("appointments", list);

        return "student/list-appointments";
    }
}