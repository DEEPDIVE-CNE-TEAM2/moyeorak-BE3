package com.example.moyeorak.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "휴대폰 번호 형식은 010-0000-0000 입니다.")
    private String phone;

    private String address;

    private String name;
}
