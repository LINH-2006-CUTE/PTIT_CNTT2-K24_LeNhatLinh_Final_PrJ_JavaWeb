package com.btvn.projectfinal.controller.student;

import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.repository.UserRepository;
import com.btvn.projectfinal.service.AcademicConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentAcademicProfileController {

    private final UserRepository userRepository;
    private final AcademicConsultationService academicConsultationService;

    @GetMapping("/academic-profile")
    public String academicProfile(Model model, Principal principal) {
        User student = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

        model.addAttribute("student", student);
        model.addAttribute("historyRows", academicConsultationService.getConsultationHistory(student.getId()));
        return "student/academic-profile";
    }
}
