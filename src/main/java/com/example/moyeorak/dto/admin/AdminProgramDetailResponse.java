package com.example.moyeorak.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;


@Builder
@Getter
public class AdminProgramDetailResponse {
    private Long id;
    private String title;

    private Long regionId;
    private String regionName;

    private Long facilityId;
    private String facilityName;

    private String category;
    private String target;
    private String instructorName;
    private String status;

    private LocalDate usageStartDate;
    private LocalDate usageEndDate;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private LocalDate cancelEndDate;

    private LocalTime classStartTime;
    private LocalTime classEndTime;

    private String usagePeriod;
    private String classTime;
    private String registrationPeriod;

    private Integer inPrice;
    private Integer outPrice;
    private Integer capacity;
    private String contact;
    private String imageUrl;
    private String description;
}