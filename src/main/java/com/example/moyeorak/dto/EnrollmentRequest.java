package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {

    @NotNull(message = "프로그램 ID는 필수입니다.")
    private Long programId;

    @NotNull(message = "수강료는 필수입니다.")
    private Integer paidAmount;
}
