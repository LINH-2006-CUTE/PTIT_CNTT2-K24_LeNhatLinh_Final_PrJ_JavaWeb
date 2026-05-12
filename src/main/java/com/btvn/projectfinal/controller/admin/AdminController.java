package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("topLecturers", adminDashboardService.loadTop5LecturersByMentoringSessions());
        model.addAttribute("lowStockEquipments", adminDashboardService.loadLowStockEquipments());
        model.addAttribute("lowStockThreshold", adminDashboardService.getLowStockThreshold());
        return "admin/dashboard";
    }
}