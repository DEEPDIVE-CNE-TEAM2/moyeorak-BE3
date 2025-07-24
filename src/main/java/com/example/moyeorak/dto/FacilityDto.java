package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDto {
    private Long id;
    private String name;
    private String address;
    private String contact;
    private String imageUrl;
    private Integer capacity;
    private String description;
    private Long regionId;
    private String location;
    private LocalTime usageStartTime;
    private LocalTime usageEndTime;
    private Integer area;
}

