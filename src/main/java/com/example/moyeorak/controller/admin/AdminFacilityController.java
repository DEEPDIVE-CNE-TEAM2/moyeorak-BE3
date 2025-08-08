package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.service.admin.AdminFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        log.info("시설 등록 요청: name={}", request.getName());
        AdminFacilityCreateResponse response = adminFacilityService.createFacility(request, httpRequest);
        log.info("시설 등록 완료: facilityId={}", response.getId());
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "시설 목록 조회")
    @GetMapping
    public ResponseEntity<List<AdminFacilityListResponse>> getFacilityList(HttpServletRequest request) {
        log.info("시설 목록 조회 요청");
        List<AdminFacilityListResponse> response = adminFacilityService.getFacilityList(request);
        log.info("시설 목록 조회 완료: {}건", response.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 상세 조회")
    @GetMapping("/{facilityId}")
    public ResponseEntity<AdminFacilityDetailResponse> getFacilityDetail(
            @PathVariable Long facilityId,
            HttpServletRequest request
    ) {
        log.info("시설 상세 조회 요청: facilityId={}", facilityId);
        AdminFacilityDetailResponse response = adminFacilityService.getFacilityDetail(facilityId, request);
        log.info("시설 상세 조회 완료: facilityId={}", facilityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 수정")
    @PutMapping("/{facilityId}")
    public ResponseEntity<AdminFacilityDetailResponse> updateFacility(
            @PathVariable Long facilityId,
            @Valid @RequestBody AdminFacilityUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("시설 수정 요청: facilityId={}", facilityId);
        AdminFacilityDetailResponse response =
                adminFacilityService.updateFacility(facilityId, request, httpRequest);
        log.info("시설 수정 완료: facilityId={}", facilityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 삭제")
    @DeleteMapping("/{facilityId}")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable Long facilityId,
            HttpServletRequest httpRequest
    ) {
        log.info("시설 삭제 요청: facilityId={}", facilityId);
        adminFacilityService.deleteFacility(facilityId, httpRequest);
        log.info("시설 삭제 완료: facilityId={}", facilityId);
        return ResponseEntity.noContent().build();
    }
}