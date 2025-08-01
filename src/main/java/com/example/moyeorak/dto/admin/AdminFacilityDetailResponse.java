package com.example.moyeorak.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFacilityDetailResponse {
    private Long id;
    private String name;
    private String address;
    private String usageStartTime;
    private String usageEndTime;
    private String contact;
    private Integer capacity;
    private String description;
    private String imageUrl;
}