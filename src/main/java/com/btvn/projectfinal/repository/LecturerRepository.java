package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {
    List<Lecturer> findByDepartmentId(Integer deptId);
}