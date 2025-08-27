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

    // ✅ MAS: Program.regionId(FK) 기준 조회
    List<Program> findByRegionId(Long regionId);

    // ✅ MAS: Program.regionId + 제목 부분검색
    List<Program> findByRegionIdAndTitleContainingIgnoreCase(Long regionId, String title);
}
