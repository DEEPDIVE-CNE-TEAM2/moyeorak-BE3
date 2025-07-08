package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    Optional<Program> findByTitleAndFacility_LocationAndUsageStartDateAndUsageEndDateAndClassStartTimeAndClassEndTime(
            String title,
            String facilityLocation,
            LocalDate usageStartDate,
            LocalDate usageEndDate,
            LocalTime classStartTime,
            LocalTime classEndTime
    );
}
