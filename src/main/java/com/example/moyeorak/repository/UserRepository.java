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
}
