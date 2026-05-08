//package com.btvn.projectfinal.controller.student;
//
//import com.btvn.projectfinal.model.entity.Lecturer;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import java.util.List;
//
//@GetMapping("/booking")
//public String showBookingForm(
//        @RequestParam(required = false) Integer departmentId,
//        Model model) {
//
//    // 1. Luôn gửi danh sách Khoa sang
//    model.addAttribute("departments", departmentRepository.findAll());
//
//    // 2. Nếu Linh vừa chọn Khoa (departmentId không null)
//    if (departmentId != null) {
//        // Tìm giảng viên theo khoa
//        List<Lecturer> lecturers = lecturerRepository.findByDepartmentId(departmentId);
//
//        model.addAttribute("lecturers", lecturers);
//        model.addAttribute("selectedDeptId", departmentId);
//    }
//
//    return "student/booking"; // Đường dẫn tới file HTML của bạn
//}