package com.btvn.projectfinal.controller;

import com.btvn.projectfinal.model.dto.ProfileDTO;
import com.btvn.projectfinal.model.entity.UserProfile;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final DepartmentRepository departmentRepository;

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        UserProfile profile = profileService.getByUsername(authentication.getName());

        ProfileDTO dto = new ProfileDTO();
        dto.setFullName(profile.getFullName());
        dto.setPhone(profile.getPhone());
        dto.setAddress(profile.getAddress());
        dto.setGender(profile.getGender());
//        dto.setStudentId(profile.getStudentId());
//        dto.setAcademicRank(profile.getAcademicRank());
        if (profile.getDepartment() != null) {
            dto.setDepartmentId(profile.getDepartment().getId());
        }

        model.addAttribute("profileDTO", dto);
        model.addAttribute("profile", profile);
        model.addAttribute("departments", departmentRepository.findAll());
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(
            Authentication authentication,
            @ModelAttribute("profileDTO") ProfileDTO dto,
            RedirectAttributes redirectAttributes) {

        profileService.update(authentication.getName(), dto);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile";
    }
}