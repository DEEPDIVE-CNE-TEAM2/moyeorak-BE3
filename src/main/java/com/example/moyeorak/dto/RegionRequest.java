package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionRequest {

    @Pattern(regexp = "^[가-힣\\s]+구$", message = "지역명은 'oo구' 또는 'oo시 oo구' 형식이어야 합니다.")
    @NotBlank(message = "지역명은 필수입니다.")
    private String name;

    // nullable 허용 (관리자 미지정 가능)
    private Long managerId;
}
