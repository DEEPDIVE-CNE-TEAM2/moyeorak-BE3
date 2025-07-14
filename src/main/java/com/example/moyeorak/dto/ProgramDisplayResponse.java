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
    private String location;
    private String target;
    private String usagePeriod;
    private String classTime;
    private String registrationPeriod;
    private String cancelEndDate;
    private Integer inPrice;
    private Integer outPrice;
    private Integer appliedPrice;  // ✅ 사용자 기준 실제 납부 가격
    private Boolean inRegion;      // ✅ 관내 여부
    private Integer capacity;
    private String contact;
    private String description;
    private String imageUrl;
    private Long regionId;
    private String instructorName;
}
