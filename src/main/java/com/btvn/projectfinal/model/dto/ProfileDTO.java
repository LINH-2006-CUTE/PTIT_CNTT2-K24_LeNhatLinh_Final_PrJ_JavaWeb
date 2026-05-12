package com.btvn.projectfinal.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileDTO {
    private String fullName;
    private String phone;
    private String address;
    private String gender;
    //  STUDENT
    private String studentId;
    private Long departmentId;
    //  LECTURER
    private String academicRank;
}
