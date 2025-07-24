package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Long> {

    // ✅ 프로그램 중복 체크용 - Facility의 name을 기준으로 검색
    Optional<Program> findByTitleAndFacility_NameAndUsageStartDateAndUsageEndDateAndClassStartTimeAndClassEndTime(
            String title,
            String facilityName,
            LocalDate usageStartDate,
            LocalDate usageEndDate,
            LocalTime classStartTime,
            LocalTime classEndTime
    );

    // ✅ 지역별 프로그램 목록 조회
    List<Program> findByRegion_Id(Long regionId);
}
