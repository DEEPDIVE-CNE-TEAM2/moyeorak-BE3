package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.service.admin.AdminMainImageService;
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
        log.info("홍보물 생성 완료: id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홍보물 리스트 조회")
    @GetMapping
    public ResponseEntity<List<AdminMainImageResponse>> getMainImages(HttpServletRequest request) {
        log.info("홍보물 리스트 조회 요청");
        List<AdminMainImageResponse> list = adminMainImageService.getMainImages(request);
        log.info("홍보물 리스트 조회 완료: {}건", list.size());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "홍보물 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMainImage(@PathVariable Long id) {
        log.info("홍보물 삭제 요청: id={}", id);
        adminMainImageService.deleteById(id);
        log.info("홍보물 삭제 완료: id={}", id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "홍보물 순서 및 표시여부 전체 수정")
    @PatchMapping
    public ResponseEntity<Void> updateMainImages(@RequestBody List<AdminMainImageUpdateRequest> requestList) {
        log.info("홍보물 순서/표시여부 전체 수정 요청: {}건", requestList.size());
        adminMainImageService.updateMainImages(requestList);
        log.info("홍보물 순서/표시여부 전체 수정 완료");
        return ResponseEntity.ok().build();
    }
}
