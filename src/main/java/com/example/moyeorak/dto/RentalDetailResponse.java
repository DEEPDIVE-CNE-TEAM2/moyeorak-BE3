package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalDetailResponse {

    private Integer id;

    private String category;             // 종목
    private String location;             // 대관시설명
    private String address;              // 주소

    private String usageTime;            // 운영 시간 (예: "09:00 ~ 18:00")
    private String registrationPeriod;   // 접수 기간 (예: "2025-07-01 ~ 2025-07-10")
    private String cancelEndDate;        // 취소 마감일 (예: "2025-07-10")

    private Integer capacity;            // 정원
    private String contact;              // 문의처
}
