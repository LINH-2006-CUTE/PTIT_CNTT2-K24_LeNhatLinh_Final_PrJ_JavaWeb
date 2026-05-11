package com.btvn.projectfinal.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcademicEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_session_id", nullable = false, unique = true)
    private Appointment mentoringSession;

    @Column(name = "evaluation_content", columnDefinition = "TEXT")
    private String evaluationContent;
}
