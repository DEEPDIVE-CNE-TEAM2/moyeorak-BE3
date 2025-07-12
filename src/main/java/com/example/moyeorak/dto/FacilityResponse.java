package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityResponse {
    private Integer id;
    private String location;
    private String address;
    private Integer area;
    private String usageTime;
    private String imageUrl;
    private String contact; // ✅ 추가
}
