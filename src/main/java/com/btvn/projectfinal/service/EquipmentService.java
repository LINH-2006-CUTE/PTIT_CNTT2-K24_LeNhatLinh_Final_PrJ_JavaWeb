package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.EquipmentDTO;
import com.btvn.projectfinal.model.entity.Department;
import com.btvn.projectfinal.model.entity.Equipment;
import com.btvn.projectfinal.model.entity.LabRoomType;
import com.btvn.projectfinal.repository.BorrowingDetailRepository;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.EquipmentRepository;
import com.btvn.projectfinal.repository.LabRoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final DepartmentRepository departmentRepository;
    private final LabRoomTypeRepository labRoomTypeRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;

    public Page<Equipment> findAll(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return equipmentRepository.findByNameContainingIgnoreCaseWithRefs(keyword.trim(), pageable);
        }
        return equipmentRepository.findAllWithDepartmentAndLab(pageable);
    }

    public Optional<Equipment> findById(Long id) {
        return equipmentRepository.findById(id);
    }

    /** Tải thiết bị kèm khoa / loại phòng (tránh lỗi lazy khi mở form sửa). */
    @Transactional(readOnly = true)
    public Equipment getEditableEquipment(Long id) {
        Equipment e = equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thiết bị!"));
        if (e.getDepartment() != null) {
            e.getDepartment().getId();
        }
        if (e.getLabRoomType() != null) {
            e.getLabRoomType().getId();
        }
        return e;
    }

    @Transactional
    public void save(EquipmentDTO dto) {
        String name = dto.getName().trim();
        assertNameUnique(name, null);

        Equipment equipment = new Equipment();
        mapDtoToEntity(dto, equipment, name);
        equipmentRepository.save(equipment);
    }

    @Transactional
    public void update(Long id, EquipmentDTO dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thiết bị!"));

        String name = dto.getName().trim();
        assertNameUnique(name, id);

        mapDtoToEntity(dto, equipment, name);
        equipmentRepository.save(equipment);
    }

    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy thiết bị!");
        }
        if (borrowingDetailRepository.existsByEquipment_Id(id)) {
            throw new IllegalStateException(
                    "Không thể xóa: thiết bị đã nằm trong ít nhất một phiếu mượn (liên kết dữ liệu).");
        }
        equipmentRepository.deleteById(id);
    }

    private void assertNameUnique(String trimmedName, Long excludeId) {
        boolean dup = excludeId == null
                ? equipmentRepository.existsByNameIgnoreCase(trimmedName)
                : equipmentRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, excludeId);
        if (dup) {
            throw new IllegalArgumentException("Tên thiết bị đã tồn tại (không được trùng).");
        }
    }

    private void mapDtoToEntity(EquipmentDTO dto, Equipment entity, String trimmedName) {
        entity.setName(trimmedName);
        entity.setDescription(dto.getDescription());
        entity.setQuantity(dto.getQuantity());
        String u = dto.getUnit();
        entity.setUnit((u == null || u.isBlank()) ? "cái" : u.trim());
        entity.setStatus(dto.getStatus() != null
                ? dto.getStatus()
                : Equipment.EquipmentStatus.AVAILABLE);

        if (dto.getDepartmentId() != null) {
            Department d = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Khoa không hợp lệ."));
            entity.setDepartment(d);
        } else {
            entity.setDepartment(null);
        }

        if (dto.getLabRoomTypeId() != null) {
            LabRoomType t = labRoomTypeRepository.findById(dto.getLabRoomTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Loại phòng lab không hợp lệ."));
            entity.setLabRoomType(t);
        } else {
            entity.setLabRoomType(null);
        }
    }
}
