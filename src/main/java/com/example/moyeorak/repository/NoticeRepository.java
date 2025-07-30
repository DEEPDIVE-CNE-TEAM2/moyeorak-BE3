package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Notice;
import com.example.moyeorak.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByRegionId(Long regionId);
    Optional<Notice> findByIdAndRegionId(Long id, Long regionId);

    List<Notice> findByRegion(Region region);
}
