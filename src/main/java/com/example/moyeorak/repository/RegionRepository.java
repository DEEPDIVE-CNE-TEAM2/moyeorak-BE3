package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
    boolean existsByName(String name);
}