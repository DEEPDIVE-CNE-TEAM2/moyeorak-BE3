package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalListResponse {

    private Integer id;
    private String location;     // 장소명
    private String address;      // 주소
    private String imageUrl;     // 이미지 URL
    private String usageTime;    // 운영 시간 (예: "09:00 ~ 18:00")
    private Integer capacity;    // 정원
}
