package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.service.admin.AdminProgramService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/programs")
public class AdminProgramController {

    private final AdminProgramService adminProgramService;

    @GetMapping
    @Operation(summary = "관리자 프로그램 리스트 조회 (지역/제목 필터링 포함)",
            description = "관리자가 프로그램 조회, ?regionId=지역id&title=이름"
    )
    public ResponseEntity<List<AdminProgramListResponse>> getProgramList(
            HttpServletRequest request,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String title
    ) {
        List<AdminProgramListResponse> programs = adminProgramService.getProgramsByRegionAndTitle(request, regionId, title);
        return ResponseEntity.ok(programs);
    }


    @PostMapping
    @Operation(summary = "관리자 프로그램 등록", description = "응답값 programid")
    public ResponseEntity<Long> createProgram(
            @RequestBody AdminProgramCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long programId = adminProgramService.createProgram(request, httpRequest);
        return ResponseEntity.status(201).body(programId);
    }

    @GetMapping("/{programId}")
    @Operation(summary = "관리자 프로그램 상세 조회")
    public ResponseEntity<AdminProgramDetailResponse> getProgramDetail(
            @PathVariable Long programId,
            HttpServletRequest request
    ) {
        AdminProgramDetailResponse programDetail = adminProgramService.getProgramDetail(programId);
        return ResponseEntity.ok(programDetail);
    }

    @PatchMapping("/{programId}")
    @Operation(summary = "관리자 프로그램 수정", description = "변경하고 싶은 필드만 보내도됨 지역 변경 불가")
    public ResponseEntity<Long> patchProgram(
            @PathVariable Long programId,
            @RequestBody AdminProgramUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long updatedId = adminProgramService.patchProgram(programId, request, httpRequest);
        return ResponseEntity.ok(updatedId);
    }

    @DeleteMapping("/{programId}")
    @Operation(summary = "관리자 프로그램 삭제", description = "programId 기준 삭제")
    public ResponseEntity<MessageResponse> deleteProgram(
            @PathVariable Long programId,
            HttpServletRequest request
    ) {
        MessageResponse response = adminProgramService.deleteProgram(programId, request);
        return ResponseEntity.ok(response);
    }
}