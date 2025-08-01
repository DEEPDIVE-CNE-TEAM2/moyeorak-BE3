package com.example.moyeorak.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMainImageCreateRequest {

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

}