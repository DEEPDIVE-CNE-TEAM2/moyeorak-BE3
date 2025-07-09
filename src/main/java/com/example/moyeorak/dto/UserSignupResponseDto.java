package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupResponseDto {
    private String email;
    private String name;
    private String phone;
    private String regionName;
}
