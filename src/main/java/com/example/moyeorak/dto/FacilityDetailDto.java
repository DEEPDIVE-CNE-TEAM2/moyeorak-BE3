package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityDetailDto {
    private Long id;
    private String location;
    private String address;
    private String usageTime;
    private Integer capacity;
    private String imageUrl;
    private String description;
    private String contact;
}
