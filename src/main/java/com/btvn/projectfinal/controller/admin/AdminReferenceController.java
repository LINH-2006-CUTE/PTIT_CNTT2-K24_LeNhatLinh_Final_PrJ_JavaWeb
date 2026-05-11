package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.LabRoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reference")
@RequiredArgsConstructor
public class AdminReferenceController {

    private final DepartmentRepository departmentRepository;
    private final LabRoomTypeRepository labRoomTypeRepository;

    @GetMapping
    public String viewReferenceData(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("labRoomTypes", labRoomTypeRepository.findAll());
        return "admin/reference/view";
    }
}
