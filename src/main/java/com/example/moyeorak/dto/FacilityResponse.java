package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityResponse {
    private Long id;
    private String location;
    private String address;
    private Integer capacity;
    private String usageTime;
    private String imageUrl;
    private String contact;
}