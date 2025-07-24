package com.example.moyeorak.controller;

import com.example.moyeorak.dto.FacilityDetailDto;
import com.example.moyeorak.dto.FacilityDto;
import com.example.moyeorak.dto.FacilitySimpleDto;
import com.example.moyeorak.dto.FacilityUpdateDto;
import com.example.moyeorak.service.FacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시설(Facility) CRUD 및 지역별 조회 컨트롤러
 */
@RestController
@RequestMapping("/api/facilities")
@RequiredArgsConstructor
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * 시설 생성
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FacilityDto> createFacility(@RequestBody FacilityDto dto) {
        FacilityDto created = facilityService.createFacility(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * 지역별 시설 목록 조회
     */
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<FacilitySimpleDto>> getFacilitiesByRegion(@PathVariable Long regionId) {
        List<FacilitySimpleDto> facilities = facilityService.getFacilitiesByRegion(regionId);
        return ResponseEntity.ok(facilities);
    }

    /**
     * 단일 시설 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacilityDetailDto> getFacility(@PathVariable Long id) {
        return ResponseEntity.ok(facilityService.getFacility(id));
    }

    /**
     * 시설 정보 수정
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<FacilityDto> updateFacility(@PathVariable Long id, @RequestBody FacilityUpdateDto dto) {
        FacilityDto updated = facilityService.updateFacility(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 시설 삭제
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFacility(@PathVariable Long id) {
        facilityService.deleteFacility(id);
        return ResponseEntity.ok("시설이 성공적으로 삭제되었습니다.");
    }
}
