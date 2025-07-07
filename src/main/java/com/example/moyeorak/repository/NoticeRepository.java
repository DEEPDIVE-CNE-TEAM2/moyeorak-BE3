package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByRegionId(Long regionId);
}
