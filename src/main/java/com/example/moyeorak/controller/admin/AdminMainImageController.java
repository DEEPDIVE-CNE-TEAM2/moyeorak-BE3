package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.service.admin.AdminMainImageService;
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
public class AdminMainImageController {

    private final AdminMainImageService adminMainImageService;

    @Operation(summary = "홍보물 생성")
    @PostMapping("/regions/{regionId}/main-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminMainImageResponse> createMainImage(
            @PathVariable Long regionId,
            @Valid @RequestBody AdminMainImageCreateRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 홍보물 생성 요청 - regionId={}, userId={}", regionId, userId);
        var response = adminMainImageService.createMainImage(request, userId, regionId);
        log.info("[ADMIN] 홍보물 생성 완료 - id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홍보물 리스트 조회")
    @GetMapping("/regions/{regionId}/main-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminMainImageResponse>> getMainImages(
            @PathVariable Long regionId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 홍보물 리스트 조회 요청 - regionId={}, userId={}", regionId, userId);
        var list = adminMainImageService.getMainImages(userId, regionId);
        log.info("[ADMIN] 홍보물 리스트 조회 완료 - {}건", list.size());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "홍보물 순서 및 표시여부 일괄 수정")
    @PatchMapping("/regions/{regionId}/main-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateMainImages(
            @PathVariable Long regionId,
            @RequestBody List<AdminMainImageUpdateRequest> requestList,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 홍보물 일괄 수정 요청 - regionId={}, userId={}, {}건", regionId, userId, requestList.size());
        adminMainImageService.updateMainImages(userId, regionId, requestList);
        log.info("[ADMIN] 홍보물 일괄 수정 완료 - regionId={}", regionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "홍보물 삭제")
    @DeleteMapping("/main-images/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMainImage(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[ADMIN] 홍보물 삭제 요청 - id={}, userId={}", id, userId);
        adminMainImageService.deleteById(id, userId);
        log.info("[ADMIN] 홍보물 삭제 완료 - id={}", id);
        return ResponseEntity.noContent().build();
    }
}
