package com.btvn.projectfinal.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserEditDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    @Size(max = 10)
    private String gender;

    private Long departmentId;

    @Size(max = 72)
    private String newPassword;

    private boolean enabled = true;
}
