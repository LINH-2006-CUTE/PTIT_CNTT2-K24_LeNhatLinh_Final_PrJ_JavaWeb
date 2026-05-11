package com.btvn.projectfinal.controller.student;

import com.btvn.projectfinal.model.dto.BookingDTO;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.repository.BookingRepository;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.LecturerRepository;
import com.btvn.projectfinal.repository.UserRepository;
import com.btvn.projectfinal.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/student/appointments")
@RequiredArgsConstructor
public class StudentAppointmentController {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final BookingRepository bookingRepository;
    private final AppointmentService appointmentService;

    @GetMapping("/new")
    public String showBookingPage(
            @RequestParam(required = false) Long departmentId,
            Model model,
            Principal principal) {

        User student = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        model.addAttribute("student", student);
        model.addAttribute("departments", departmentRepository.findAll());

        if (!model.containsAttribute("booking")) {
            BookingDTO booking = new BookingDTO();
            booking.setDepartmentId(departmentId);
            model.addAttribute("booking", booking);
        }

        if (departmentId != null) {
            List<Lecturer> lecturers = lecturerRepository.findByDepartmentId(departmentId);
            model.addAttribute("lecturers", lecturers);
            model.addAttribute("selectedDeptId", departmentId);
        }
        return "student/booking";
    }

    @PostMapping("/save")
    public String saveAppointment(
            @Valid @ModelAttribute("booking") BookingDTO booking,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Long departmentId = booking.getDepartmentId();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    bindingResult.getFieldErrors().stream()
                            .findFirst()
                            .map(err -> err.getDefaultMessage())
                            .orElse("Dữ liệu không hợp lệ"));
            redirectAttributes.addFlashAttribute("booking", booking);
            String base = "redirect:/student/appointments/new";
            return departmentId != null ? base + "?departmentId=" + departmentId : base;
        }

        try {
            User student = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy sinh viên"));

            appointmentService.bookSession(student.getId(), booking);

            redirectAttributes.addFlashAttribute("message", "Đặt lịch thành công!");
            return "redirect:/student/appointments/my-appointments";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("booking", booking);
            String base = "redirect:/student/appointments/new";
            return departmentId != null ? base + "?departmentId=" + departmentId : base;
        }
    }

    @GetMapping("/my-appointments")
    public String listAppointments(Model model, Principal principal) {
        User student = userRepository.findByUsername(principal.getName()).get();
        model.addAttribute("appointments",
                bookingRepository.findByStudent_IdOrderByAppointmentDateDescStartTimeDesc(student.getId()));
        model.addAttribute("now", LocalDateTime.now());
        return "student/list-appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancelAppointment(
            @PathVariable Integer id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            User student = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy sinh viên"));
            appointmentService.cancelSession(student.getId(), id);
            redirectAttributes.addFlashAttribute("message", "Đã hủy lịch thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/appointments/my-appointments";
    }
}
