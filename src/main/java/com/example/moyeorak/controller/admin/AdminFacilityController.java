package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.service.admin.AdminFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminFacilityController {

    private final AdminFacilityService adminFacilityService;

    // ───────────── 지역 단위(목록/생성) ─────────────

    @Operation(summary = "시설 등록")
    @PostMapping("/regions/{regionId}/facilities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminFacilityCreateResponse> createFacility(
            @PathVariable Long regionId,
            @Valid @RequestBody AdminFacilityCreateRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 시설 등록 요청 - regionId={}, userId={}, name={}", regionId, userId, request.getName());
        var response = adminFacilityService.createFacility(request, userId, regionId);
        log.info("[ADMIN] 시설 등록 완료 - facilityId={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 목록 조회")
    @GetMapping("/regions/{regionId}/facilities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminFacilityListResponse>> getFacilityList(
            @PathVariable Long regionId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 시설 목록 조회 요청 - regionId={}, userId={}", regionId, userId);
        var response = adminFacilityService.getFacilityList(userId, regionId);
        log.info("[ADMIN] 시설 목록 조회 완료 - {}건", response.size());
        return ResponseEntity.ok(response);
    }

    // ───────────── 시설 단위(상세/수정/삭제) ─────────────

    @Operation(summary = "시설 상세 조회")
    @GetMapping("/facilities/{facilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminFacilityDetailResponse> getFacilityDetail(
            @PathVariable Long facilityId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 시설 상세 조회 요청 - facilityId={}, userId={}", facilityId, userId);
        var response = adminFacilityService.getFacilityDetail(facilityId, userId);
        log.info("[ADMIN] 시설 상세 조회 완료 - facilityId={}", facilityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 수정")
    @PutMapping("/facilities/{facilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminFacilityDetailResponse> updateFacility(
            @PathVariable Long facilityId,
            @Valid @RequestBody AdminFacilityUpdateRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 시설 수정 요청 - facilityId={}, userId={}", facilityId, userId);
        var response = adminFacilityService.updateFacility(facilityId, request, userId);
        log.info("[ADMIN] 시설 수정 완료 - facilityId={}", facilityId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "시설 삭제")
    @DeleteMapping("/facilities/{facilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFacility(
            @PathVariable Long facilityId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 시설 삭제 요청 - facilityId={}, userId={}", facilityId, userId);
        adminFacilityService.deleteFacility(facilityId, userId);
        log.info("[ADMIN] 시설 삭제 완료 - facilityId={}", facilityId);
        return ResponseEntity.noContent().build();
    }
}
