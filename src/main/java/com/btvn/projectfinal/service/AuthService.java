package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.RegisterDTO;
import com.btvn.projectfinal.model.entity.Department;
import com.btvn.projectfinal.model.entity.Lecturer;
import com.btvn.projectfinal.model.entity.User;
import com.btvn.projectfinal.model.entity.UserProfile;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.LecturerRepository;
import com.btvn.projectfinal.repository.UserProfileRepository;
import com.btvn.projectfinal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;

    // spring security check login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .accountLocked(!user.isEnabled())
                .build();
    }

    @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại!");
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setFullName(dto.getFullName());

        if (dto.getRole() == User.Role.STUDENT && dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(profile::setDepartment);
        }
        if (dto.getRole() == User.Role.LECTURER && dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(profile::setDepartment);
        }

        profileRepository.save(profile);

        if (dto.getRole() == User.Role.LECTURER) {
            createLecturerProfileIfAbsent(user, dto.getDepartmentId());
        }
    }

    private void createLecturerProfileIfAbsent(User user, Long departmentIdFromForm) {
        if (lecturerRepository.findByUser_Id(user.getId()).isPresent()) {
            return;
        }
        Department department = null;
        if (departmentIdFromForm != null) {
            department = departmentRepository.findById(departmentIdFromForm).orElse(null);
        }
        if (department == null) {
            department = departmentRepository.findByCode("CNTT")
                    .orElseGet(() -> departmentRepository.findAll().stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(
                                    "Chưa có khoa trong hệ thống. Vui lòng liên hệ quản trị.")));
        }
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(user);
        lecturer.setDepartment(department);
        lecturer.setTitle("Giảng viên");
        lecturer.setSpecialization("Chưa cập nhật");
        lecturerRepository.save(lecturer);
    }
}
