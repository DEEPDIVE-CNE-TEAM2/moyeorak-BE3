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
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionResponse> createRegion(@RequestBody @Valid RegionRequest request) {
        log.info("[POST] 지역 생성 요청: {}", request);
        RegionResponse created = regionService.createRegion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        log.info("[GET] 전체 지역 목록 조회");
        return ResponseEntity.ok(regionService.getAllRegions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getRegionById(@PathVariable Long id) {
        log.info("[GET] 지역 조회 요청 - ID: {}", id);
        return ResponseEntity.ok(regionService.getRegion(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegionResponse> updateRegion(@PathVariable Long id,
                                                       @RequestBody @Valid RegionRequest request) {
        log.info("[PUT] 지역 수정 요청 - ID: {}", id);
        return ResponseEntity.ok(regionService.updateRegion(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteRegion(@PathVariable Long id) {
        log.info("[DELETE] 지역 삭제 요청 - ID: {}", id);
        MessageResponse response = regionService.deleteRegion(id);
        return ResponseEntity.ok(response);
    }
}
