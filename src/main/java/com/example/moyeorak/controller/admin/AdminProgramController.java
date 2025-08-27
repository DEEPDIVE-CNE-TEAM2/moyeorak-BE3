package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.admin.AdminProgramCreateRequest;
import com.example.moyeorak.dto.admin.AdminProgramDetailResponse;
import com.example.moyeorak.dto.admin.AdminProgramListResponse;
import com.example.moyeorak.dto.admin.AdminProgramUpdateRequest;
import com.example.moyeorak.service.admin.AdminProgramService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/programs")
public class AdminProgramController {

    private final AdminProgramService adminProgramService;

    @GetMapping
    @Operation(
            summary = "관리자 프로그램 리스트 조회 (지역/제목 필터링 포함)",
            description = "관리자가 프로그램 조회, ?regionId=지역id&title=이름"
    )
    public ResponseEntity<List<AdminProgramListResponse>> getProgramList(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String title
    ) {
        log.info("프로그램 리스트 조회: userId={}, regionId={}, title={}", userId, regionId, title);
        List<AdminProgramListResponse> programs =
                adminProgramService.getProgramsByRegionAndTitle(userId, regionId, title);
        log.info("프로그램 리스트 조회 완료: 조회된 개수={}", programs.size());
        return ResponseEntity.ok(programs);
    }

    @PostMapping
    @Operation(summary = "관리자 프로그램 등록", description = "응답값 programId")
    public ResponseEntity<Long> createProgram(
            @RequestBody AdminProgramCreateRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("프로그램 등록 요청: userId={}, title={}, regionId={}",
                userId, request.getTitle(), request.getRegionId());
        Long programId = adminProgramService.createProgram(request, userId);
        log.info("프로그램 등록 완료: programId={}", programId);
        return ResponseEntity.status(201).body(programId);
    }

    @GetMapping("/{programId}")
    @Operation(summary = "관리자 프로그램 상세 조회")
    public ResponseEntity<AdminProgramDetailResponse> getProgramDetail(
            @PathVariable Long programId,
            @AuthenticationPrincipal(expression = "id") Long userId   // ⬅️ 추가
    ) {
        log.info("프로그램 상세 조회: userId={}, programId={}", userId, programId);
        AdminProgramDetailResponse programDetail = adminProgramService.getProgramDetail(programId, userId); // ⬅️ 수정
        log.info("프로그램 상세 조회 완료: title={}", programDetail.getTitle());
        return ResponseEntity.ok(programDetail);
    }

    @PatchMapping("/{programId}")
    @Operation(summary = "관리자 프로그램 수정", description = "변경하고 싶은 필드만 보내도 됨 (지역 변경 불가)")
    public ResponseEntity<Long> patchProgram(
            @PathVariable Long programId,
            @RequestBody AdminProgramUpdateRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("프로그램 수정 요청: userId={}, programId={}", userId, programId);
        Long updatedId = adminProgramService.patchProgram(programId, request, userId);
        log.info("프로그램 수정 완료: programId={}", updatedId);
        return ResponseEntity.ok(updatedId);
    }

    @DeleteMapping("/{programId}")
    @Operation(summary = "관리자 프로그램 삭제", description = "programId 기준 삭제")
    public ResponseEntity<MessageResponse> deleteProgram(
            @PathVariable Long programId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("프로그램 삭제 요청: userId={}, programId={}", userId, programId);
        MessageResponse response = adminProgramService.deleteProgram(programId, userId);
        log.info("프로그램 삭제 완료: programId={}", programId);
        return ResponseEntity.ok(response);
    }
}
