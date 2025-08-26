package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacilitySimpleDto {
    private Long id;
    private String location;
    private String address;
    private String usageTime;
    private String contact;
    private String imageUrl;
    private Integer area;
}