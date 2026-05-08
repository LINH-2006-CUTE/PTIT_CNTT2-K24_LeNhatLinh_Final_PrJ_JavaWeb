package com.btvn.projectfinal.controller.lecturer;

import com.btvn.projectfinal.model.dto.LecturerDTO;
import com.btvn.projectfinal.service.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class LectureController {
    private final LectureService lectureService;

    @GetMapping("/lecturers")
    public List<LecturerDTO> getLecturers(@RequestParam Integer deptId) {
        return lectureService.getLecturersByDept(deptId);
    }
}