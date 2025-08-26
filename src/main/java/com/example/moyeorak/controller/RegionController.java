package com.example.moyeorak.controller;

import com.example.moyeorak.dto.RegionRequest;
import com.example.moyeorak.dto.RegionResponse;
import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.service.RegionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content/regions")
public class RegionController {

    private final RegionService regionService;

    /**
     * 📌 지역 생성 (ADMIN 전용)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionResponse> createRegion(@Valid @RequestBody RegionRequest request) {
        log.info("[POST] 지역 생성 요청: {}", request);
        RegionResponse created = regionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 📌 전체 지역 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        log.info("[GET] 전체 지역 목록 조회");
        return ResponseEntity.ok(regionService.getAllRegions());
    }

    /**
     * 📌 특정 지역 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getRegionById(@PathVariable Long id) {
        log.info("[GET] 지역 상세 조회 - ID: {}", id);
        return ResponseEntity.ok(regionService.getRegion(id));
    }

    /**
     * 📌 지역 정보 수정 (ADMIN 전용)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionResponse> updateRegion(
            @PathVariable Long id,
            @Valid @RequestBody RegionRequest request
    ) {
        log.info("[PUT] 지역 수정 요청 - ID: {}", id);
        return ResponseEntity.ok(regionService.updateRegion(id, request));
    }

    /**
     * 📌 지역 삭제 (ADMIN 전용)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteRegion(@PathVariable Long id) {
        log.info("[DELETE] 지역 삭제 요청 - ID: {}", id);
        return ResponseEntity.ok(regionService.deleteRegion(id));
    }
}
