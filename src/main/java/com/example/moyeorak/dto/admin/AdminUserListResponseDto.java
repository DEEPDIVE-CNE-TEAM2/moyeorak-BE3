package com.example.moyeorak.dto.admin;

import com.example.moyeorak.entity.User;

import java.time.format.DateTimeFormatter;

public record AdminUserListResponseDto(
        Long id,
        String name,
        String gender,
        String email,
        String region,
        String createdAt
) {
    public static AdminUserListResponseDto from(User user) {
        return new AdminUserListResponseDto(
                user.getId(),
                user.getName(),
                user.getGender().name(),  // enum이면 .name()으로 꺼내기
                user.getEmail(),
                user.getRegion().getName(), // Address 엔티티에서 지역 이름 추출
                user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) // 가입일 포맷
        );
    }
}