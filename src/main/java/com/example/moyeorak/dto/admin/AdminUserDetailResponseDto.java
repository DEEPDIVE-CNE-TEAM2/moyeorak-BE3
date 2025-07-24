package com.example.moyeorak.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminUserDetailResponseDto {
    private Long id;
    private String name;         // 읽기 전용
    private String gender;       // 읽기 전용 ("남"/"여")
    private String createdAt;    // 가입일 (yyyy.MM.dd)

    private String email;        // 수정 가능
    private String phone;        // 수정 가능
    private Long regionId;       // 현재 지역 ID (수정 가능)
    private String regionName;   // 지역 이름 (예: "송파구") → 화면 표시용
}