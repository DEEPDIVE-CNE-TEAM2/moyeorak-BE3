package com.example.moyeorak.repository;


import com.example.moyeorak.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 특정 지역의 공지사항 목록 조회
    List<Notice> findByRegion_Id(Long regionId);

    // 특정 지역 + 특정 ID 공지사항 조회
    Optional<Notice> findByIdAndRegion_Id(Long id, Long regionId);

    // 특정 지역 + 제목 키워드 검색 (추가 예시)
    List<Notice> findByRegion_IdAndTitleContainingIgnoreCase(Long regionId, String keyword);
}