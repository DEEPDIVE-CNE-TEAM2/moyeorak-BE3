package com.example.moyeorak.dto.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserUpdateRequestDto {
    private String email;
    private String phone;
    private Long regionId; // 거주지 region은 ID로 받아서 처리
}