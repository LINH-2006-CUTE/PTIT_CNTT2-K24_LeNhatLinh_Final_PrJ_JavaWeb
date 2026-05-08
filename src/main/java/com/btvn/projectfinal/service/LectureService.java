package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.LecturerDTO;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class LectureService {

    private final LecturerRepository lecturerRepository;

    public List<LecturerDTO> getLecturersByDept(Integer deptId) {
        return lecturerRepository.findByDepartmentId(deptId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LecturerDTO convertToDTO(Lecturer l) {
        String name = (l.getUser().getProfile() != null)
                ? l.getUser().getProfile().getFullName()
                : "Chưa cập nhật tên";

        return new LecturerDTO(
                l.getId(),
                name,
                l.getTitle(),
                l.getSpecialization()
        );
    }
}