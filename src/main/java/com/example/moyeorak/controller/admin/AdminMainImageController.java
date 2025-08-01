package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.service.admin.AdminMainImageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/main-img")
@RequiredArgsConstructor
public class AdminMainImageController {

    private final AdminMainImageService adminMainImageService;

    @Operation(summary = "홍보물 생성")
    @PostMapping
    public ResponseEntity<AdminMainImageResponse> createMainImage(
            @Valid @RequestBody AdminMainImageCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        AdminMainImageResponse response = adminMainImageService.createMainImage(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홍보물 리스트 조회")
    @GetMapping
    public ResponseEntity<List<AdminMainImageResponse>> getMainImages(HttpServletRequest request) {
        return ResponseEntity.ok(adminMainImageService.getMainImages(request));
    }

    @Operation(summary = "홍보물 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMainImage(@PathVariable Long id) {
        adminMainImageService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "홍보물 순서 및 표시여부 전체 수정")
    @PatchMapping
    public ResponseEntity<?> updateMainImages(@RequestBody List<AdminMainImageUpdateRequest> requestList) {
        adminMainImageService.updateMainImages(requestList);
        return ResponseEntity.ok().build();
    }
}
