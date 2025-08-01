package com.example.moyeorak.repository;

import com.example.moyeorak.entity.MainImage;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MainImageRepository extends JpaRepository<MainImage, Long> {
    List<MainImage> findByRegionIdOrderByDisplayOrderAsc(Long regionId);
    boolean existsByRegionIdAndDisplayOrder(Long regionId, Integer displayOrder);

    @Query("SELECT MAX(m.displayOrder) FROM MainImage m WHERE m.region.id = :regionId")
    Integer findMaxDisplayOrderByRegionId(Long regionId);
}
