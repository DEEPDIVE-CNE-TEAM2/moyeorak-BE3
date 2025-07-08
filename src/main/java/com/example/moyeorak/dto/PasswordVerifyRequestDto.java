package com.example.moyeorak.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "비밀번호 검증 요청")
@Getter @Setter
@NoArgsConstructor
public class PasswordVerifyRequestDto {
    @Schema(description = "확인할 비밀번호", example = "test1234!")
    private String password;
}
