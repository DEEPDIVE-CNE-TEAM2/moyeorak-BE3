package com.example.moyeorak.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPasswordUpdateRequestDto {
    private String newPassword;
    private String confirmPassword;
}