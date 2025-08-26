package com.example.moyeorak.controller;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.ProgramDisplayResponse;
import com.example.moyeorak.dto.ProgramRequest;
import com.example.moyeorak.security.CustomUserDetails;
import com.example.moyeorak.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content/programs")
@RequiredArgsConstructor
@Slf4j
public class ProgramController {

    private final ProgramService programService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDisplayResponse> createProgram(
            @Valid @RequestBody ProgramRequest request
    ) {
        log.info("[POST] 프로그램 등록 요청");
        return ResponseEntity.ok(programService.createProgram(request));
    }

    @GetMapping
    public ResponseEntity<List<ProgramDisplayResponse>> getPrograms(
            @RequestParam(value = "regionId", required = false) Long regionId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userRegionId = (user != null) ? user.getRegionId() : null;
        log.info("[GET] 전체 또는 지역별 프로그램 목록 조회 - regionId: {}, userRegionId: {}", regionId, userRegionId);

        if (regionId != null) {
            return ResponseEntity.ok(programService.getProgramsByRegion(regionId, userRegionId));
        } else {
            return ResponseEntity.ok(programService.getAllPrograms(userRegionId));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramDisplayResponse> getProgramById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userRegionId = (user != null) ? user.getRegionId() : null;
        log.info("[GET] 프로그램 상세 조회 - id: {}, userRegionId: {}", id, userRegionId);
        return ResponseEntity.ok(programService.getProgramById(id, userRegionId));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramDisplayResponse> patchProgram(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        log.info("[PATCH] 프로그램 수정 요청 - id: {}", id);
        return ResponseEntity.ok(programService.partialUpdateProgram(id, updates));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProgram(@PathVariable Long id) {
        log.info("[DELETE] 프로그램 삭제 요청 - id: {}", id);
        programService.deleteProgram(id);
        return ResponseEntity.ok(new MessageResponse("프로그램이 삭제되었습니다."));
    }
}
