package com.example.moyeorak.repository;

import com.example.moyeorak.entity.MainImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MainImageRepository extends JpaRepository<MainImage, Long> {

    // 특정 지역의 메인 이미지 목록 조회 (표시 순서 기준)
    List<MainImage> findByRegion_IdOrderByDisplayOrderAsc(Long regionId);

    // 특정 지역 내 displayOrder 중복 여부 체크
    boolean existsByRegion_IdAndDisplayOrder(Long regionId, Integer displayOrder);

    // 특정 지역에서 displayOrder의 최대값 조회
    @Query("SELECT MAX(m.displayOrder) FROM MainImage m WHERE m.region.id = :regionId")
    Integer findMaxDisplayOrderByRegionId(Long regionId);
}