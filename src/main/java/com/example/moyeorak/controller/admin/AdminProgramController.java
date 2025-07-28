package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminProgramCreateRequest;
import com.example.moyeorak.dto.admin.AdminProgramDetailResponse;
import com.example.moyeorak.dto.admin.AdminProgramListResponse;
import com.example.moyeorak.dto.admin.AdminUserDetailResponseDto;
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
        AdminProgramDetailResponse userDetail = adminProgramService.getProgramDetail(programId);
        return ResponseEntity.ok(userDetail);
    }
}