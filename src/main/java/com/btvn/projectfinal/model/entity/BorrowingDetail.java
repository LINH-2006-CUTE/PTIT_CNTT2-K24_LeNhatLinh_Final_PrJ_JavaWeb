package com.btvn.projectfinal.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "borrowing_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrowing_record_id", nullable = false)
    private BorrowingRecord borrowingRecord;

    /**
     * Trùng với sinh viên trên phiếu mượn — một số schema MySQL yêu cầu {@code student_id} ngay trên dòng chi tiết.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity = 1;
}
