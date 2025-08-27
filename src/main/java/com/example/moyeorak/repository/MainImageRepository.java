package com.example.moyeorak.repository;

import com.example.moyeorak.entity.MainImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MainImageRepository extends JpaRepository<MainImage, Long> {

    /** 특정 지역의 메인 이미지 목록 조회 (표시 순서 기준) — MAS: regionId(FK) 사용 */
    List<MainImage> findByRegionIdOrderByDisplayOrderAsc(Long regionId);

    /** 특정 지역 내 displayOrder 중복 여부 체크 — MAS: regionId(FK) 사용 */
    boolean existsByRegionIdAndDisplayOrder(Long regionId, Integer displayOrder);

    /** 특정 지역에서 displayOrder의 최대값 조회 — MAS: regionId(FK) 사용 */
    @Query("select coalesce(max(m.displayOrder), 0) from MainImage m where m.regionId = :regionId")
    Integer findMaxDisplayOrderByRegionId(@Param("regionId") Long regionId);
}
