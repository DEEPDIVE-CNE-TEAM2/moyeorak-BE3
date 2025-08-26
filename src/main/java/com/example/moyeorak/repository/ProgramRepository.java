package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByTitleAndFacilityIdAndUsageStartDateAndUsageEndDateAndClassStartTimeAndClassEndTime(
            String title,
            Long facilityId,
            LocalDate usageStartDate,
            LocalDate usageEndDate,
            LocalTime classStartTime,
            LocalTime classEndTime
    );

    List<Program> findByRegionId(Long regionId);

    List<Program> findByRegionIdAndTitleContainingIgnoreCase(Long regionId, String title);
}
