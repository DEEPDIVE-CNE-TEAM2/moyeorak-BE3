package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramResponse {
    private Long id;
    private String title;
    private String category;
    private String target;
    private String instructorName;
    private String status;
    private LocalDate usageStartDate;
    private LocalDate usageEndDate;
    private LocalTime classStartTime;
    private LocalTime classEndTime;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private LocalDate cancelEndDate;
    private Integer fee;
    private Integer capacity;
    private String contact;
    private String imageUrl;
    private String description;
}