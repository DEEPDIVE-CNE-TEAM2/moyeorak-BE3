package com.example.moyeorak.controller.admin;
import com.example.moyeorak.dto.admin.AdminUserDetailResponseDto;
import com.example.moyeorak.dto.admin.AdminUserEnrollmentDto;
import com.example.moyeorak.dto.admin.AdminUserCreateRequestDto;
import com.example.moyeorak.dto.admin.AdminUserListResponseDto;
import com.example.moyeorak.dto.admin.AdminUserUpdateRequestDto;
import com.example.moyeorak.dto.admin.AdminPasswordUpdateRequestDto;
import com.example.moyeorak.service.admin.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "회원 조회",
            description = "관리자가 회원 조회, ?regionId=지역id&keyword=이름"
    )
    @GetMapping
    public List<AdminUserListResponseDto> getUsersByRegionAndKeyword(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        log.info("회원 조회: regionId={}, keyword={}", regionId, keyword);
        List<AdminUserListResponseDto> result = adminUserService.getUsersByRegionAndKeyword(request, regionId, keyword);
        log.info("회원 조회: 결과 {}건", result.size());
        return result;
    }

    @Operation(summary = "유저 생성")
    @PostMapping
    public ResponseEntity<Void> createUser(
            @RequestBody AdminUserCreateRequestDto dto,
            HttpServletRequest request
    ) {
        log.info("유저 생성: 요청 이름={}", dto.getName());
        adminUserService.createUser(dto, request);
        log.info("유저 생성: 완료");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 상세 정보 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailResponseDto> getUserDetail(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        log.info("회원 상세 조회: userId={}", userId);
        AdminUserDetailResponseDto userDetail = adminUserService.getUserDetail(userId, request);
        return ResponseEntity.ok(userDetail);
    }

    @Operation(summary = "회원정보 수정")
    @PatchMapping("/{userId}")
    public ResponseEntity<Void> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody AdminUserUpdateRequestDto dto,
            HttpServletRequest request

    ) {
        log.info("회원정보 수정: userId={}", userId);
        adminUserService.updateUserInfo(userId, dto, request);
        log.info("회원정보 수정: 완료 userId={}", userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "회원 비밀번호 수정"
    )
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updateUserPassword(
            @PathVariable Long userId,
            @RequestBody AdminPasswordUpdateRequestDto dto,
            HttpServletRequest request
    ) {
        log.info("회원 비밀번호 수정: userId={}", userId);
        adminUserService.updateUserPassword(userId, dto, request);
        log.info("회원 비밀번호 수정: 완료 userId={}", userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원 수강 이력 조회")
    @GetMapping("/{userId}/enrollments")
    public ResponseEntity<List<AdminUserEnrollmentDto>> getUserEnrollments(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        log.info("수강 이력 조회: userId={}", userId);
        List<AdminUserEnrollmentDto> enrollments = adminUserService.getUserEnrollments(userId, request);
        log.info("수강 이력 조회: 결과 {}건", enrollments.size());
        return ResponseEntity.ok(enrollments);
    }

    @Operation(summary = "회원 수강 삭제")
    @DeleteMapping("/enrollments/{enrollmentId}")
    public ResponseEntity<String> cancelEnrollment(
            @PathVariable Long enrollmentId,
            HttpServletRequest request
    ) {
        log.info("수강 삭제: enrollmentId={}", enrollmentId);
        adminUserService.cancelEnrollment(enrollmentId, request);
        log.info("수강 삭제: 완료 enrollmentId={}", enrollmentId);
        return ResponseEntity.ok("수강 신청이 성공적으로 취소되었습니다.");
    }
}