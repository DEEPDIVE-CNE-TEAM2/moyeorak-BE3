package com.example.moyeorak.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacilityUpdateDto {
    private String name;
    private String address;
    private String contact;
    private String imageUrl;
    private Integer capacity;
    private String description;
    private String location;
    private Integer regionId;
    private Integer area;
    private String usageStartTime;
    private String usageEndTime;
}