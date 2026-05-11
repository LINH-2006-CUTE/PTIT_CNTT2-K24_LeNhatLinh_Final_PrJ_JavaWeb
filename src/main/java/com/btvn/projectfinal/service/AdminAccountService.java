package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.AdminUserEditDTO;
import com.btvn.projectfinal.model.entity.*;
import com.btvn.projectfinal.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class AdminAccountService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;
    private final BookingRepository bookingRepository;
    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowingDetailRepository borrowingDetailRepository;
    private final PasswordEncoder passwordEncoder;

    /** Tải hồ sơ kèm {@link User} trong một transaction (tránh lỗi lazy trên Thymeleaf). */
    @Transactional(readOnly = true)
    public List<UserProfile> listProfilesByRole(User.Role role) {
        return userProfileRepository.findAll().stream()
                .filter(p -> {
                    User u = p.getUser();
                    return u != null && role.equals(u.getRole());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public User requireManagedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalStateException("Không được thao tác trên tài khoản quản trị tại đây.");
        }
        if (user.getRole() != User.Role.STUDENT && user.getRole() != User.Role.LECTURER) {
            throw new IllegalStateException("Chỉ quản lý sinh viên hoặc giảng viên.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserProfile requireProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Tài khoản chưa có hồ sơ (user_profiles)."));
    }

    @Transactional
    public void updateAccount(Long userId, AdminUserEditDTO dto, Long currentAdminUserId) {
        User user = requireManagedUser(userId);
        if (user.getId().equals(currentAdminUserId)) {
            throw new IllegalStateException("Không thể tự sửa chính mình qua màn hình này (dùng Hồ sơ cá nhân).");
        }

        user.setEnabled(dto.isEnabled());
        String rawPw = dto.getNewPassword();
        if (StringUtils.hasText(rawPw)) {
            String pw = rawPw.trim();
            if (pw.length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới ít nhất 6 ký tự.");
            }
            user.setPassword(passwordEncoder.encode(pw));
        }
        userRepository.save(user);

        UserProfile profile = requireProfile(user);
        profile.setFullName(dto.getFullName().trim());
        profile.setPhone(dto.getPhone());
        profile.setAddress(dto.getAddress());
        profile.setGender(dto.getGender());
        if (dto.getDepartmentId() != null) {
            departmentRepository.findById(dto.getDepartmentId()).ifPresent(profile::setDepartment);
        } else {
            profile.setDepartment(null);
        }
        userProfileRepository.save(profile);
    }

    @Transactional
    public void deleteAccount(Long userId, Long currentAdminUserId) {
        User user = requireManagedUser(userId);
        if (user.getId().equals(currentAdminUserId)) {
            throw new IllegalStateException("Không thể xóa chính tài khoản đang đăng nhập.");
        }

        if (user.getRole() == User.Role.STUDENT) {
            deleteStudentAndRelatedData(user);
        } else {
            deleteLecturerAndRelatedData(user);
        }
    }

    private void deleteStudentAndRelatedData(User studentUser) {
        Long uid = studentUser.getId();
        List<Appointment> sessions = new ArrayList<>(
                bookingRepository.findByStudent_IdOrderByAppointmentDateDescStartTimeDesc(uid));
        for (Appointment a : sessions) {
            cleanupMentoringSession(a.getId());
        }
        List<BorrowingRecord> orphanBorrow = borrowingRecordRepository.findByStudent_IdOrderByCreatedAtDesc(uid);
        for (BorrowingRecord br : orphanBorrow) {
            borrowingDetailRepository.deleteByBorrowingRecord_Id(br.getId());
            borrowingRecordRepository.delete(br);
        }
        deleteProfileAndUser(studentUser);
    }

    private void deleteLecturerAndRelatedData(User lecturerUser) {
        Lecturer lec = lecturerRepository.findByUser_Id(lecturerUser.getId()).orElse(null);
        if (lec != null) {
            Long lecturerPk = lec.getId();
            /* Dùng SQL thẳng lên bảng mentoring_sessions — đảm bảo lấy đủ id (derived findByLecturer_Id có thể không khớp). */
            List<Integer> sessionIds = new ArrayList<>(bookingRepository.findSessionIdsByLecturerId(lecturerPk));
            for (Integer sid : sessionIds) {
                cleanupMentoringSession(sid);
            }
            entityManager.flush();
            if (!bookingRepository.findSessionIdsByLecturerId(lecturerPk).isEmpty()) {
                throw new IllegalStateException(
                        "Không xóa hết buổi tư vấn của giảng viên (còn ràng buộc). Hãy thử lại hoặc xóa tay trong DB.");
            }
            lecturerRepository.delete(lec);
        }
        deleteProfileAndUser(lecturerUser);
    }

    /**
     * Xóa đánh giá, phiếu mượn + chi tiết, rồi buổi tư vấn.
     */
    private void cleanupMentoringSession(Integer sessionId) {
        academicEvaluationRepository.deleteByMentoringSession_Id(sessionId);
        List<BorrowingRecord> records = borrowingRecordRepository.findByMentoringSession_Id(sessionId);
        for (BorrowingRecord br : records) {
            borrowingDetailRepository.deleteByBorrowingRecord_Id(br.getId());
            borrowingRecordRepository.delete(br);
        }
        bookingRepository.deleteById(sessionId);
    }

    private void deleteProfileAndUser(User user) {
        userProfileRepository.findByUserId(user.getId()).ifPresent(userProfileRepository::delete);
        userRepository.delete(user);
    }
}
