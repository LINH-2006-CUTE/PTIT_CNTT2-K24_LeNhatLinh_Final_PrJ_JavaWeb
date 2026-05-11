package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminAccountService adminAccountService;

    @GetMapping("/lecturers")
    public String listLecturers(Model model) {
        model.addAttribute("users", adminAccountService.listProfilesByRole(User.Role.LECTURER));
        model.addAttribute("title", "Danh sách Giảng viên");
        model.addAttribute("listFrom", "lecturers");
        return "admin/profile-list";
    }

    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("users", adminAccountService.listProfilesByRole(User.Role.STUDENT));
        model.addAttribute("title", "Danh sách Sinh viên");
        model.addAttribute("listFrom", "students");
        return "admin/profile-list";
    }
}