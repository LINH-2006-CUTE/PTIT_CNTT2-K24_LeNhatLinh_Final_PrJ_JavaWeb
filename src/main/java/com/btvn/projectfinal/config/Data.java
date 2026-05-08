package com.btvn.projectfinal.config;

import com.btvn.projectfinal.model.entity.*;
import com.btvn.projectfinal.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class Data implements ApplicationRunner {

    private final DepartmentRepository departmentRepository;
    private final LabRoomTypeRepository labRoomTypeRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final LecturerRepository lecturerRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedDepartments();
        seedLabRoomTypes();
        seedAdminAccount();
        seedLecturers();
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) return;
        List<Department> departments = List.of(
                createDept("Công nghệ Thông tin",   "CNTT"),
                createDept("Kinh tế & Quản trị",    "KTQT"),
                createDept("Hệ thống Thông tin",   "HTTT"),
                createDept("An toàn Thông tin",    "ATTT")
        );
        departmentRepository.saveAll(departments);
        log.info("Seeded {} departments", departments.size());
    }

    private void seedLabRoomTypes() {
        if (labRoomTypeRepository.count() > 0) return;
        List<LabRoomType> types = List.of(
                createLabType("Phòng Lập trình",          "Máy tính cá nhân, IDE sẵn sàng"),
                createLabType("Phòng Mạng máy tính",      "Router, Switch, cáp mạng"),
                createLabType("Phòng Đa phương tiện",     "Thiết bị quay phim, dựng phim")
        );
        labRoomTypeRepository.saveAll(types);
        log.info("Seeded {} lab room types", types.size());
    }

    private void seedLecturers() {
        if (userRepository.existsByUsername("giangvien01")) {
            return;
        }
        Department cntt = departmentRepository.findByCode("CNTT")
                .orElseThrow(() -> new RuntimeException("Chưa có khoa CNTT"));
// tạo user cho gv

        User gv = new User();

        gv.setUsername("giangvien01");
        gv.setPassword(passwordEncoder.encode("123456"));
        gv.setRole(User.Role.LECTURER);
        userRepository.save(gv);
// tạo profile cho gv
        UserProfile profile = new UserProfile();
        profile.setUser(gv);
        profile.setFullName("Thầy Nguyễn Văn A");

        profileRepository.save(profile);

        Lecturer lecturer = new Lecturer();
        lecturer.setUser(gv);
        lecturer.setDepartment(cntt);
        lecturer.setTitle("Thạc sĩ");
        lecturer.setSpecialization("Lập trình Java");
        lecturerRepository.save(lecturer);
        log.info("Seeded lecturer: Thầy Nguyễn Văn A thuộc khoa CNTT");
    }

    private void seedAdminAccount() {
        if (userRepository.existsByUsername("admin")) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);

        UserProfile profile = new UserProfile();
        profile.setUser(admin);
        profile.setFullName("Quản trị viên hệ thống");
        profileRepository.save(profile);

        log.info("Seeded admin account (username: admin / password: Admin@123)");
    }

    private Department createDept(String name, String code) {
        Department d = new Department();
        d.setName(name); d.setCode(code);
        return d;
    }

    private LabRoomType createLabType(String name, String desc) {
        LabRoomType t = new LabRoomType();
        t.setName(name); t.setDescription(desc);
        return t;
    }
}
