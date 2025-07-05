package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnrollmentCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String cancelReason;
}