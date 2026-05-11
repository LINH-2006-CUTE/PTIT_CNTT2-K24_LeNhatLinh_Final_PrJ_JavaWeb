package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.AcademicEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {

    boolean existsByMentoringSession_Id(Integer mentoringSessionId);
}
