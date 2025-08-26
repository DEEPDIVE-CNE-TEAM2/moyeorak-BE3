package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    // 지역 이름 중복 체크
    boolean existsByName(String name);

    // 이름으로 조회
    Optional<Region> findByName(String name);

    // 특정 매니저 ID로 관리하는 지역 목록 조회
    List<Region> findAllByManagerId(Long managerId);
}
