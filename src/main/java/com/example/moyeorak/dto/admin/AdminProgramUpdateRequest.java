package com.example.moyeorak.dto.admin;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProgramUpdateRequest {
    private String title;
    private Long facilityId;
    private String category;
    private String target;
    private String instructorName;
    private String status; // "OPEN" or "CLOSED"
    private LocalDate usageStartDate;
    private LocalDate usageEndDate;
    private LocalTime classStartTime;
    private LocalTime classEndTime;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private LocalDate cancelEndDate;
    private Integer inPrice;
    private Integer outPrice;
    private Integer capacity;
    private String contact;
    private String imageUrl;
    private String description;

}