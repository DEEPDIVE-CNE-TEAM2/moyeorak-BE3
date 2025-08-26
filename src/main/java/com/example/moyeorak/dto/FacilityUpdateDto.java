package com.example.moyeorak.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 시설 수정 요청 DTO
 * - 부분 업데이트에 사용됨
 */
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
    private Long regionId;
    private Integer area;
    private String usageStartTime;
    private String usageEndTime;
}