package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.ProfileDTO;
import com.btvn.projectfinal.model.entity.UserProfile;
import com.btvn.projectfinal.repository.DepartmentRepository;
import com.btvn.projectfinal.repository.UserProfileRepository;
import com.btvn.projectfinal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final DepartmentRepository departmentRepository;

    public UserProfile getByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> profileRepository.findByUserId(user.getId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ!"));
    }

    @Transactional
    public void update(String username, ProfileDTO dto) {
        UserProfile profile = getByUsername(username);

        profile.setFullName(dto.getFullName());
        profile.setPhone(dto.getPhone());
        profile.setAddress(dto.getAddress());
        profile.setGender(dto.getGender());

        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId())
                    .ifPresent(profile::setDepartment);
        } else {
            profile.setDepartment(null);
        }

        profileRepository.save(profile);
    }

}
