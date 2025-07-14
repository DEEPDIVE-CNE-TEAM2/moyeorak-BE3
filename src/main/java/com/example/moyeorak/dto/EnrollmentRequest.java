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

    @NotBlank(message = "프로그램 제목은 필수입니다.")
    private String programTitle;

    @NotBlank(message = "시설 위치는 필수입니다.")
    private String place;

    @NotBlank(message = "이용 기간은 필수입니다. 예: '2025-08-01 ~ 2025-08-31'")
    private String usagePeriod;

    @NotBlank(message = "수업 시간은 필수입니다. 예: '09:00 ~ 11:00'")
    private String usageTime;

    @NotNull(message = "수강료는 필수입니다.")
    private Integer paidAmount;
}
