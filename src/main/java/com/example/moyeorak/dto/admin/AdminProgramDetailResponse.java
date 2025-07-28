package com.example.moyeorak.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminProgramDetailResponse {
    private Long id;
    private String title;
    private String regionName;
    private String facilityName;
    private String category;
    private String target;
    private String instructorName;
    private String status; // OPEN / CLOSED
    private String usagePeriod;         // ex: 2024-09-01 ~ 2024-10-01
    private String classTime;           // ex: 14:00 ~ 16:00
    private String registrationPeriod;  // ex: 2024-08-01 ~ 2024-08-15
    private String cancelEndDate;       // ex: 2024-08-20
    private Integer inPrice;
    private Integer outPrice;
    private Integer capacity;
    private String contact;
    private String imageUrl;
    private String description;
}