package com.example.moyeorak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "비밀번호 검증 응답")
@Getter
@AllArgsConstructor
public class PasswordVerifyResponseDto {
    @Schema(description = "비밀번호 일치 여부", example = "true")
    private boolean matched;
}

