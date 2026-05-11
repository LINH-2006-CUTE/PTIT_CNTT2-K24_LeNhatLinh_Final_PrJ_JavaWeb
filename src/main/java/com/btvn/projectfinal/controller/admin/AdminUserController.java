package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.model.dto.AdminUserEditDTO;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.model.entity.UserProfile;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.UserRepository;
import com.btvn.projectfinal.service.AdminAccountService;
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

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminAccountService adminAccountService;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @RequestParam String from,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!"students".equals(from) && !"lecturers".equals(from)) {
            from = "students";
        }

        try {
            User user = adminAccountService.requireManagedUser(id);
            UserProfile profile = adminAccountService.requireProfile(user);

            AdminUserEditDTO dto = new AdminUserEditDTO();
            dto.setFullName(profile.getFullName());
            dto.setPhone(profile.getPhone());
            dto.setAddress(profile.getAddress());
            dto.setGender(profile.getGender());
            if (profile.getDepartment() != null) {
                dto.setDepartmentId(profile.getDepartment().getId());
            }
            dto.setEnabled(user.isEnabled());

            model.addAttribute("editUser", user);
            model.addAttribute("adminUserEditDTO", dto);
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("from", from);
            model.addAttribute("currentAdminId", currentAdminUserId(principal));
            return "admin/user-edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/profile/" + from;
        }
    }

    @PostMapping("/{id}/edit")
    public String saveEdit(
            @PathVariable Long id,
            @RequestParam String from,
            @RequestParam(value = "enabled", required = false) Boolean enabledFlag,
            @Valid @ModelAttribute("adminUserEditDTO") AdminUserEditDTO dto,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!"students".equals(from) && !"lecturers".equals(from)) {
            from = "students";
        }

        dto.setEnabled(Boolean.TRUE.equals(enabledFlag));

        if (bindingResult.hasErrors()) {
            User user = userRepository.findById(id).orElseThrow();
            model.addAttribute("editUser", user);
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("from", from);
            model.addAttribute("currentAdminId", currentAdminUserId(principal));
            return "admin/user-edit";
        }

        try {
            adminAccountService.updateAccount(id, dto, currentAdminUserId(principal));
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tài khoản.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/" + id + "/edit?from=" + from;
        }
        return "redirect:/admin/profile/" + from;
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam String from,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!"students".equals(from) && !"lecturers".equals(from)) {
            from = "students";
        }

        try {
            adminAccountService.deleteAccount(id, currentAdminUserId(principal));
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản và dữ liệu liên quan.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/profile/" + from;
    }

    private Long currentAdminUserId(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("Không xác định được admin."));
    }
}
