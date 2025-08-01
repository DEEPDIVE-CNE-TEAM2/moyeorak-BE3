package com.example.moyeorak.dto.admin;

import jakarta.validation.constraints.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFacilityCreateRequest {

    @NotBlank(message = "시설명을 입력해주세요.")
    private String name;

    @NotBlank(message = "주소를 입력해주세요.")
    private String address;

    private String location;
    @NotNull(message = "면적을 입력해주세요.")
    private Integer area;

    @NotBlank(message = "운영 시작시간을 입력해주세요. (예: 09:00)")
    private String usageStartTime;

    @NotBlank(message = "운영 종료시간을 입력해주세요. (예: 18:00)")
    private String usageEndTime;

    private String contact;

    @NotNull(message = "수용 인원을 입력해주세요.")
    @Min(value = 1, message = "수용 인원은 최소 1명 이상이어야 합니다.")
    private Integer capacity;

    @NotBlank(message = "시설 설명을 입력해주세요.")
    private String description;

    private String imageUrl;
}