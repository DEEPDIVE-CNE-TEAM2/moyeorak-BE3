package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalCreateResponse {

    private Integer id;

    private String category;
    private String location;
    private String imageUrl;
    private String description;
    private String target;

    private LocalDate usageStartDate;
    private LocalDate usageEndDate;
    private LocalTime usageStartTime;
    private LocalTime usageEndTime;

    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private LocalDate cancelEndDate;

    private Integer fee;
    private Integer capacity;

    private String contact;
    private String address;

    private String regionName;       // 지역 이름
    private String managerName;      // 관리자 이름 ← 추가
    private String managerEmail;     // 관리자 이메일
}

