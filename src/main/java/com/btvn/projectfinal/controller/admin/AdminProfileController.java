package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final UserProfileRepository profileRepository;

    //danh sách Giảng viên
    @GetMapping("/lecturers")
    public String listLecturers(Model model) {
        var lecturers = profileRepository.findAll()
                .stream()
                .filter(p -> "LECTURER".equals(p.getUser().getRole().name()))
                .toList();

        model.addAttribute("users", lecturers);
        model.addAttribute("title", "Danh sách Giảng viên");
        return "admin/profile-list";
    }

    //danh sách Sinh viên
    @GetMapping("/students")
    public String listStudents(Model model) {
        var students = profileRepository.findAll()
                .stream()
                .filter(p -> "STUDENT".equals(p.getUser().getRole().name()))
                .toList();

        model.addAttribute("users", students);
        model.addAttribute("title", "Danh sách Sinh viên");
        return "admin/profile-list";
    }
}