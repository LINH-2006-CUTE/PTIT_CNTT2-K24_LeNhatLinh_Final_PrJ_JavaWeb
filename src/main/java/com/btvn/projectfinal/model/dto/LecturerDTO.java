package com.btvn.projectfinal.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerDTO {
    private Long id;
    private String fullName;
    private String title; // để bằng cấp
    private String specialization; // chuyên môn
}