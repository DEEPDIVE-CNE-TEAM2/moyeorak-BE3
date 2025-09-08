package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /* ===== 안전 로딩(Region 미리 로딩) 전용 메서드 ===== */

    @EntityGraph(attributePaths = "region")
    Optional<User> findWithRegionByEmail(String email);

    @EntityGraph(attributePaths = "region")
    Optional<User> findWithRegionByPhone(String phone);

    @EntityGraph(attributePaths = "region")
    List<User> findAllByOrderByIdDesc();

    /* ===== 기존 메서드 (필요시 사용 가능) ===== */

    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findByRegion(Region region);
    List<User> findByRegionAndRole(Region region, User.Role role);
    List<User> findByRegionAndRoleAndNameContainingIgnoreCase(Region region, User.Role role, String name);
}
