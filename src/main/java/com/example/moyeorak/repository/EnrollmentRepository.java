package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByUserIdAndProgramId(Long userId, Long programId);
    boolean existsByUserIdAndProgramIdAndStatusNot(Long userId, Long programId, Enrollment.Status status);
    List<Enrollment> findByUserId(Long userId);
    Optional<Enrollment> findByIdAndUserId(Long id, Long userId);
}