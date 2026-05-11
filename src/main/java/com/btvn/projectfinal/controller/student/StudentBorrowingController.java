package com.btvn.projectfinal.controller.student;

import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.repository.UserRepository;
import com.btvn.projectfinal.service.StudentBorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/**
 * Sinh viên chỉ <strong>xem</strong> phiếu mượn. Phiếu được hệ thống tạo khi giảng viên
 * hoàn tất đánh giá buổi cố vấn và chọn thiết bị — không có nút “Tạo phiếu” ở đây.
 */
@Controller
@RequestMapping("/student/borrowings")
@RequiredArgsConstructor
public class StudentBorrowingController {

    private final UserRepository userRepository;
    private final StudentBorrowingService studentBorrowingService;

    @GetMapping("/my-borrowings")
    public String myBorrowings(Model model, Principal principal) {
        User student = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản."));
        model.addAttribute("records", studentBorrowingService.listForStudent(student.getId()));
        return "student/my-borrowings";
    }
 
}
