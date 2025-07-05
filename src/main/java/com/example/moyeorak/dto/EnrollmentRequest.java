package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {

    @NotNull
    private Long programId;

    @NotNull
    private Long regionId;

    private Integer paidAmount;  // 선택 값 (null 허용)

    private String cancelReason;  // 취소 시 사용
}
