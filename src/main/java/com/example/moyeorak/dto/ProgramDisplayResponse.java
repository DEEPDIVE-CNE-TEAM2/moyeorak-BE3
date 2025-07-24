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
    private Integer appliedPrice;
    private Boolean inRegion;
    private Integer capacity;
    private String contact;
    private String description;
    private String imageUrl;
    private Long regionId;
    private String instructorName;
}
