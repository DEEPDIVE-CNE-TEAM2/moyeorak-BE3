package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Enrollment;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndProgramId(Long userId, Long programId);
    boolean existsByUserIdAndProgramIdAndStatusNot(Long userId, Long programId, Enrollment.Status status);
    List<Enrollment> findByUserId(Long userId);
    Optional<Enrollment> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT e FROM Enrollment e " +
            "JOIN FETCH e.program " +
            "JOIN FETCH e.region " +
            "WHERE e.user.id = :userId")
    List<Enrollment> findAllWithProgramAndRegionByUserId(@Param("userId") Long userId);
}
