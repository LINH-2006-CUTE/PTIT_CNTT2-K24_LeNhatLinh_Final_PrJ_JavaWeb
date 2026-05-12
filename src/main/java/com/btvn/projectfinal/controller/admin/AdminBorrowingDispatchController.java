package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.service.BorrowingDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/borrowing")
@RequiredArgsConstructor
public class AdminBorrowingDispatchController {

    private final BorrowingDispatchService borrowingDispatchService;

    @GetMapping("/pending")
    public String pendingList(Model model) {
        model.addAttribute("records", borrowingDispatchService.listAwaitingDispatch());
        return "admin/borrowing/pending";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            boolean applied = borrowingDispatchService.confirmDispatch(id);
            redirectAttributes.addFlashAttribute("successMessage", applied
                    ? "Đã xác nhận xuất kho và cập nhật tồn."
                    : "Phiếu đã được cấp phát trước đó — không trừ kho lần hai (chống bấm đúp / gửi trùng).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/borrowing/pending";
    }
}
