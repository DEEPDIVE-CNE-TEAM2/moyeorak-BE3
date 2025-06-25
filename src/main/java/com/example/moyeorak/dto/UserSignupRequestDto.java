package com.example.moyeorak.dto;

import com.example.moyeorak.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupRequestDto {

    @Email(message = "유효한 이메일을 입력하세요.")
    @NotBlank
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank
    private String name;

    @NotNull
    private User.Gender gender;

    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-0000-0000 형식이어야 합니다.")
    @NotBlank
    private String phone;

    @NotNull
    private User.Role role;

    @NotBlank
    @Pattern(regexp = "^[가-힣]{2,}시\\s[가-힣]{2,}구$", message = "주소는 'OO시 OO구' 형식이어야 합니다.")
    private String address;
}
