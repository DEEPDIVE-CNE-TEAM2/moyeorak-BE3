package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.service.admin.AdminFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/facility")
@RequiredArgsConstructor
public class AdminFacilityController {

    private final AdminFacilityService adminFacilityService;

    @Operation(summary = "시설 등록")
    @PostMapping
    public ResponseEntity<AdminFacilityCreateResponse> createFacility(
            @Valid @RequestBody AdminFacilityCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        AdminFacilityCreateResponse response = adminFacilityService.createFacility(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 목록 조회")
    @GetMapping
    public ResponseEntity<List<AdminFacilityListResponse>> getFacilityList(
            HttpServletRequest request
    ) {
        List<AdminFacilityListResponse> response = adminFacilityService.getFacilityList(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 상세 조회")
    @GetMapping("/{facilityId}")
    public ResponseEntity<AdminFacilityDetailResponse> getFacilityDetail(
            @PathVariable Long facilityId,
            HttpServletRequest request
    ) {
        AdminFacilityDetailResponse response = adminFacilityService.getFacilityDetail(facilityId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 수정")
    @PutMapping("/{facilityId}")
    public ResponseEntity<AdminFacilityDetailResponse> updateFacility(
            @PathVariable Long facilityId,
            @Valid @RequestBody AdminFacilityUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        AdminFacilityDetailResponse response =
                adminFacilityService.updateFacility(facilityId, request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 삭제")
    @DeleteMapping("/{facilityId}")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable Long facilityId,
            HttpServletRequest httpRequest
    ) {
        adminFacilityService.deleteFacility(facilityId, httpRequest);
        return ResponseEntity.noContent().build();
    }
}