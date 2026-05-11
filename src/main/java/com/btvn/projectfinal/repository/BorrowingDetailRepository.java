package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.BorrowingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowingDetailRepository extends JpaRepository<BorrowingDetail, Long> {
}
