package com.example.moyeorak.dto.admin;

import lombok.Data;

@Data
public class AdminUserCreateRequestDto {
    private String email;
    private String password;
    private String name;
    private String gender; // "남" 또는 "여"
    private String birth;  // yyyy-MM-dd 형식
    private String phone;
}
