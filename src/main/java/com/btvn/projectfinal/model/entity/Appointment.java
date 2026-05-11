package com.btvn.projectfinal.model.entity;

import com.btvn.projectfinal.model.MentoringSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "mentoring_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    /**
     * {@link MentoringSessionStatus#PENDING}, {@link MentoringSessionStatus#COMPLETED}, {@link MentoringSessionStatus#CANCELLED}.
     */
    @Column(nullable = false, length = 50)
    private String status = MentoringSessionStatus.PENDING;
}