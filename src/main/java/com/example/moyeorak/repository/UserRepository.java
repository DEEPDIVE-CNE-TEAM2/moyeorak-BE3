package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    List<User> findByRegion(Region region);

    // 회원조회할때 user만 보이게
    List<User> findByRegionAndRole(Region region, User.Role role);

    // region, role, 이름(검색어) 포함(대소문자 무시) 조건으로 유저 필터링
    List<User> findByRegionAndRoleAndNameContainingIgnoreCase(Region region, User.Role role, String name);
}
