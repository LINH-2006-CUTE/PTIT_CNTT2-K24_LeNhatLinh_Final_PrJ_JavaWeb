package com.btvn.projectfinal.service;

import com.btvn.projectfinal.model.dto.LecturerMentoringStatsProjection;
import com.btvn.projectfinal.model.entity.Equipment;
import com.btvn.projectfinal.repository.AdminDashboardRepository;
import com.btvn.projectfinal.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;
    private final EquipmentRepository equipmentRepository;
    private final int lowStockThreshold;

    public AdminDashboardService(
            AdminDashboardRepository adminDashboardRepository,
            EquipmentRepository equipmentRepository,
            @Value("${app.admin.low-stock-threshold:5}") int lowStockThreshold) {
        this.adminDashboardRepository = adminDashboardRepository;
        this.equipmentRepository = equipmentRepository;
        this.lowStockThreshold = lowStockThreshold;
    }

    @Transactional(readOnly = true)
    public List<LecturerMentoringStatsProjection> loadTop5LecturersByMentoringSessions() {
        return adminDashboardRepository.findTop5LecturersByMentoringSessionCount();
    }

    @Transactional(readOnly = true)
    public List<Equipment> loadLowStockEquipments() {
        return equipmentRepository.findByQuantityLessThanOrderByQuantityAsc(lowStockThreshold);
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }
}
