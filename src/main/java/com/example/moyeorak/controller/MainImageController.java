package com.example.moyeorak.controller;

import com.example.moyeorak.dto.MainImageRequest;
import com.example.moyeorak.dto.MainImageResponse;
import com.example.moyeorak.service.MainImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/main-images")
@RequiredArgsConstructor
@Slf4j
public class MainImageController {

    private final MainImageService mainImageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainImageResponse> create(@Valid @RequestBody MainImageRequest request) {
        log.info("[POST] 메인 이미지 등록 요청");
        return ResponseEntity.status(HttpStatus.CREATED).body(mainImageService.create(request));
    }

    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<MainImageResponse>> getByRegion(@PathVariable Long regionId) {
        log.info("[GET] 지역별 메인 이미지 목록 요청 - regionId: {}", regionId);
        return ResponseEntity.ok(mainImageService.getByRegion(regionId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MainImageResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MainImageRequest request
    ) {
        log.info("[PATCH] 메인 이미지 수정 요청 - id: {}", id);
        return ResponseEntity.ok(mainImageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("[DELETE] 메인 이미지 삭제 요청 - id: {}", id);
        mainImageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
