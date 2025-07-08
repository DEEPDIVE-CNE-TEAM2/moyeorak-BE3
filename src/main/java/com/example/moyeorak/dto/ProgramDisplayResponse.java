package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramDisplayResponse {

    private Long id;
    private String title;
    private String location;          // 강의 장소 (Rental.location)
    private String target;            // 수강 대상
    private String usagePeriod;       // 이용 기간 (ex. 2025-08-01 ~ 2025-09-30)
    private String classTime;         // 수업 시간 (ex. 10:00 ~ 12:00)
    private String registrationPeriod;// 접수 기간
    private String cancelEndDate;     // 취소 마감일
    private Integer fee;              // 요금
    private Integer capacity;         // 정원
    private String contact;           // 문의
    private String description;       // 상세 설명
    private String imageUrl;          // 이미지 URL
}
