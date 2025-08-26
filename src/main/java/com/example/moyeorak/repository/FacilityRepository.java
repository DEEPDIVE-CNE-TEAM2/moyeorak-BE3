package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacilityRepository extends JpaRepository<Facility, Long> {

    List<Facility> findByRegionId(Long regionId);

    Optional<Facility> findByRegionIdAndName(Long regionId, String name);

    List<Facility> findByRegionIdAndNameContainingIgnoreCase(Long regionId, String keyword);

    boolean existsByRegionIdAndName(Long regionId, String name);
}
