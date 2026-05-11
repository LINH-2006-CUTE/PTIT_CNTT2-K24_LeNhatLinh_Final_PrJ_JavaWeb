package com.btvn.projectfinal.controller.admin;

import com.btvn.projectfinal.model.dto.EquipmentDTO;
import com.btvn.projectfinal.model.entity.Equipment;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.LabRoomTypeRepository;
import com.btvn.projectfinal.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentService equipmentService;
    private final DepartmentRepository departmentRepository;
    private final LabRoomTypeRepository labRoomTypeRepository;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Equipment> equipmentPage = equipmentService.findAll(keyword, pageable);

        model.addAttribute("equipmentPage", equipmentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        return "admin/equipment/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("equipmentDTO", new EquipmentDTO());
        model.addAttribute("statuses", Equipment.EquipmentStatus.values());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("labRoomTypes", labRoomTypeRepository.findAll());
        return "admin/equipment/form";
    }

    // edit
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Equipment equipment = equipmentService.getEditableEquipment(id);

        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setDescription(equipment.getDescription());
        dto.setQuantity(equipment.getQuantity());
        dto.setUnit(equipment.getUnit());
        dto.setStatus(equipment.getStatus());
        if (equipment.getDepartment() != null) {
            dto.setDepartmentId(equipment.getDepartment().getId());
        }
        if (equipment.getLabRoomType() != null) {
            dto.setLabRoomTypeId(equipment.getLabRoomType().getId());
        }

        model.addAttribute("equipmentDTO", dto);
        model.addAttribute("statuses", Equipment.EquipmentStatus.values());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("labRoomTypes", labRoomTypeRepository.findAll());
        return "admin/equipment/form";
    }

    @PostMapping("/save")
    public String save(
            @Valid @ModelAttribute("equipmentDTO") EquipmentDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", Equipment.EquipmentStatus.values());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("labRoomTypes", labRoomTypeRepository.findAll());
            return "admin/equipment/form";
        }

        try {
            if (dto.getId() != null) {
                equipmentService.update(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thiết bị thành công!");
            } else {
                equipmentService.save(dto);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm thiết bị thành công!");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return dto.getId() != null
                    ? "redirect:/admin/equipment/edit/" + dto.getId()
                    : "redirect:/admin/equipment/add";
        }

        return "redirect:/admin/equipment";
    }


    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipmentService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thiết bị thành công!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (NoSuchElementException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/equipment";
    }
}
