package com.btvn.projectfinal.controller.lecturer;

import com.btvn.projectfinal.model.MentoringSessionStatus;
import com.btvn.projectfinal.model.dto.CompletedConsultationProjection;
import com.btvn.projectfinal.model.dto.EvaluationRequestDTO;
import com.btvn.projectfinal.model.entity.Appointment;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.repository.BookingRepository;
import com.btvn.projectfinal.repository.EquipmentRepository;
import com.btvn.projectfinal.repository.LecturerRepository;
import com.btvn.projectfinal.repository.UserRepository;
import com.btvn.projectfinal.service.LecturerConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
import java.util.Optional;

@Controller
@RequestMapping("/lecturer/consultations")
@RequiredArgsConstructor
public class LecturerConsultationController {

    private static final String FLASH_NO_LECTURER_PROFILE =
            "Tài khoản của bạn chưa gắn hồ sơ giảng viên trong hệ thống. "
                    + "Hãy đăng xuất, chạy lại ứng dụng (để đồng bộ dữ liệu) hoặc liên hệ quản trị. "
                    + "Nếu bạn vừa đăng ký, hãy đăng nhập lại sau khi admin đã cấu hình khoa.";

    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final BookingRepository bookingRepository;
    private final EquipmentRepository equipmentRepository;
    private final LecturerConsultationService lecturerConsultationService;

    @GetMapping("/pending")
    public String pendingList(
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Lecturer> lecturer = findCurrentLecturer(principal);
        if (lecturer.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", FLASH_NO_LECTURER_PROFILE);
            return "redirect:/lecturer/dashboardLecture";
        }
        model.addAttribute("sessions", lecturerConsultationService.listPendingSessions(lecturer.get().getId()));
        return "lecturer/consultations-pending";
    }

    @GetMapping("/history")
    public String history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Lecturer> lecturer = findCurrentLecturer(principal);
        if (lecturer.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", FLASH_NO_LECTURER_PROFILE);
            return "redirect:/lecturer/dashboardLecture";
        }
        Page<CompletedConsultationProjection> historyPage =
                lecturerConsultationService.listCompletedHistory(
                        lecturer.get().getId(), page, Math.min(Math.max(size, 1), 50));
        model.addAttribute("historyPage", historyPage);
        return "lecturer/consultations-history";
    }

    @GetMapping("/{sessionId}/evaluate")
    public String evaluationForm(
            @PathVariable Integer sessionId,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Lecturer> lecturer = findCurrentLecturer(principal);
        if (lecturer.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", FLASH_NO_LECTURER_PROFILE);
            return "redirect:/lecturer/dashboardLecture";
        }

        return bookingRepository.findByIdAndLecturer_Id(sessionId, lecturer.get().getId())
                .filter(s -> MentoringSessionStatus.PENDING.equals(s.getStatus()))
                .map(session -> {
                    EvaluationRequestDTO dto = new EvaluationRequestDTO();
                    dto.setSessionId(sessionId);
                    model.addAttribute("session", session);
                    model.addAttribute("evaluationRequest", dto);
                    model.addAttribute("equipments", equipmentRepository.findAll());
                    return "lecturer/consultation-evaluate";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage",
                            "Không tìm thấy buổi tư vấn hợp lệ, không thuộc quyền của bạn, hoặc ca không còn trạng thái Pending.");
                    return "redirect:/lecturer/consultations/pending";
                });
    }

    @PostMapping("/evaluate")
    public String submitEvaluation(
            @Valid @ModelAttribute("evaluationRequest") EvaluationRequestDTO evaluationRequest,
            BindingResult bindingResult,
            Model model,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Optional<Lecturer> lecturer = findCurrentLecturer(principal);
        if (lecturer.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", FLASH_NO_LECTURER_PROFILE);
            return "redirect:/lecturer/dashboardLecture";
        }

        if (bindingResult.hasErrors()) {
            Appointment session = bookingRepository
                    .findByIdAndLecturer_Id(evaluationRequest.getSessionId(), lecturer.get().getId())
                    .orElse(null);
            model.addAttribute("session", session);
            model.addAttribute("equipments", equipmentRepository.findAll());
            return "lecturer/consultation-evaluate";
        }

        try {
            lecturerConsultationService.completeEvaluation(lecturer.get().getId(), evaluationRequest);
            redirectAttributes.addFlashAttribute("message", "Đã lưu đánh giá thành công.");
            return "redirect:/lecturer/consultations/history";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            Appointment session = bookingRepository
                    .findByIdAndLecturer_Id(evaluationRequest.getSessionId(), lecturer.get().getId())
                    .orElse(null);
            model.addAttribute("session", session);
            model.addAttribute("equipments", equipmentRepository.findAll());
            return "lecturer/consultation-evaluate";
        }
    }

    private Optional<Lecturer> findCurrentLecturer(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .flatMap(u -> lecturerRepository.findByUser_Id(u.getId()));
    }
}
