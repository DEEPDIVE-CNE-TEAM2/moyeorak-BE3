package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalResponse {

    private Integer id;
    private Long regionId;
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
}