package com.btvn.projectfinal.repository;

import com.btvn.projectfinal.model.entity.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @EntityGraph(attributePaths = {"department", "labRoomType"})
    @Query("SELECT e FROM Equipment e")
    Page<Equipment> findAllWithDepartmentAndLab(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "labRoomType"})
    @Query("SELECT e FROM Equipment e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Equipment> findByNameContainingIgnoreCaseWithRefs(
            @Param("keyword") String keyword,
            Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipment e WHERE e.id = :id")
    Optional<Equipment> findByIdForUpdate(@Param("id") Long id);

    /** Cảnh báo tồn kho thấp: lọc + sắp xếp hoàn toàn trong JPQL (không duyệt từng dòng trong Java). */
    @EntityGraph(attributePaths = {"department", "labRoomType"})
    @Query("SELECT e FROM Equipment e WHERE e.quantity < :threshold ORDER BY e.quantity ASC")
    List<Equipment> findByQuantityLessThanOrderByQuantityAsc(@Param("threshold") int threshold);
}

