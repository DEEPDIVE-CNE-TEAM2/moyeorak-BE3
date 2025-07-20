package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminUserListResponseDto;
import com.example.moyeorak.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users") // 관리자용 유저 API 경로
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 관리자 담당 지역의 유저 리스트 조회
    @GetMapping
    public List<AdminUserListResponseDto> getUsersByRegion(@RequestParam Long adminId) {
        return adminUserService.getUsersByRegion(adminId);
    }
}