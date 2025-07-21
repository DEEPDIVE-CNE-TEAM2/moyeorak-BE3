package com.example.moyeorak.controller.admin;
import com.example.moyeorak.dto.admin.AdminUserDetailResponseDto;

import com.example.moyeorak.dto.admin.AdminUserCreateRequestDto;
import com.example.moyeorak.dto.admin.AdminUserListResponseDto;
import com.example.moyeorak.dto.admin.AdminUserUpdateRequestDto;
import com.example.moyeorak.dto.admin.AdminPasswordUpdateRequestDto;
import com.example.moyeorak.service.admin.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // AccessToken 기반으로 관리자 유저 식별, null이면 자기 지역 값 있으면 그 지역 뜨게
    @GetMapping
    public List<AdminUserListResponseDto> getUsersByRegionAndKeyword(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request
    ) {
        return adminUserService.getUsersByRegionAndKeyword(request, regionId, keyword);
    }

    // 유저 생성
    @PostMapping
    public ResponseEntity<Void> createUser(
            @RequestBody AdminUserCreateRequestDto dto,
            HttpServletRequest request
    ) {
        adminUserService.createUser(dto, request);
        return ResponseEntity.ok().build();
    }

    // 회원 상세 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailResponseDto> getUserDetail(
            @PathVariable Long userId,
            HttpServletRequest request
    ) {
        AdminUserDetailResponseDto userDetail = adminUserService.getUserDetail(userId, request);
        return ResponseEntity.ok(userDetail);
    }

    // 회원정보수정
    @PatchMapping("/{userId}")
    public ResponseEntity<Void> updateUserInfo(
            @PathVariable Long userId,
            @RequestBody AdminUserUpdateRequestDto dto
    ) {
        adminUserService.updateUserInfo(userId, dto);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 수정
    @PatchMapping("/{userId}/password")
    public ResponseEntity<Void> updateUserPassword(
            @PathVariable Long userId,
            @RequestBody AdminPasswordUpdateRequestDto dto
    ) {
        adminUserService.updateUserPassword(userId, dto);
        return ResponseEntity.ok().build();
    }
}