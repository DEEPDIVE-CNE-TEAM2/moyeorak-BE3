package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.service.FacilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시설(Facility) CRUD 및 지역별 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/content/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * 시설 생성 (관리자 전용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FacilityDto> createFacility(@RequestBody FacilityDto dto) {
        log.info("[POST] 시설 생성 요청: {}", dto);
        FacilityDto created = facilityService.createFacility(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 지역별 시설 목록 조회
     */
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<FacilitySimpleDto>> getFacilitiesByRegion(@PathVariable Long regionId) {
        log.info("[GET] 지역별 시설 목록 조회 - regionId: {}", regionId);
        List<FacilitySimpleDto> facilities = facilityService.getFacilitiesByRegion(regionId);
        return ResponseEntity.ok(facilities);
    }

    /**
     * 단일 시설 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityDetailDto> getFacility(@PathVariable Long id) {
        log.info("[GET] 시설 상세 조회 - id: {}", id);
        return ResponseEntity.ok(facilityService.getFacility(id));
    }

    /**
     * 시설 정보 수정 (관리자 전용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FacilityDto> updateFacility(
            @PathVariable Long id,
            @RequestBody FacilityUpdateDto dto
    ) {
        log.info("[PUT] 시설 수정 요청 - id: {}", id);
        FacilityDto updated = facilityService.updateFacility(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 시설 삭제 (관리자 전용)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteFacility(@PathVariable Long id) {
        log.info("[DELETE] 시설 삭제 요청 - id: {}", id);
        facilityService.deleteFacility(id);
        return ResponseEntity.ok(new MessageResponse("시설이 성공적으로 삭제되었습니다."));
    }
}
