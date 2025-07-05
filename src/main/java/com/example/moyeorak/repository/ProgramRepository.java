package com.example.moyeorak.repository;

import com.example.moyeorak.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {
}