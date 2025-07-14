package com.example.moyeorak.dto;

import com.example.moyeorak.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    @Email
    private String email;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$")
    private String phone;

    private String name;

    private User.Gender gender;

    private String confirmPassword;

    private Long regionId;
}
