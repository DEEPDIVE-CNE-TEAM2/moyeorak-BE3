package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentRequest {

    @NotBlank
    private String programTitle;  // 강좌명

    @NotBlank
    private String center;        // 센터명 (Rental location)

    @NotBlank
    private String usagePeriod;   // 예: "2025-08-01 ~ 2025-09-30"

    @NotBlank
    private String usageTime;     // 예: "10:00 ~ 12:00"

    @NotNull
    private Integer paidAmount;   // 수강료

    @NotNull(message = "지역 ID는 필수입니다.")
    private Long regionId;
}
