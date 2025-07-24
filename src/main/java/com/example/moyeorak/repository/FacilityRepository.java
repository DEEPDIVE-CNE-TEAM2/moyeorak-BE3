package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    // 지역별 시설 목록 조회
    List<Facility> findByRegionId(Long regionId);

    // 이름으로 시설 조회
    Optional<Facility> findByName(String name);

    // 이름 일부로 검색 (like 검색)
    List<Facility> findByNameContaining(String keyword);
}
