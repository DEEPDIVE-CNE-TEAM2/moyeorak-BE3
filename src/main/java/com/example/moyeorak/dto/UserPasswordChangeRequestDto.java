package com.example.moyeorak.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordChangeRequestDto {
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
}