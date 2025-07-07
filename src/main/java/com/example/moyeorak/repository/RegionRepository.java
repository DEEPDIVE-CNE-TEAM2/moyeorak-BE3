package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {

    boolean existsByName(String name);
    Optional<Region> findByName(String name);
    List<Region> findAllByManagerId(Long managerId);
}