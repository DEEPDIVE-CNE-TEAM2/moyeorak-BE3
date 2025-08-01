package com.example.moyeorak.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminFacilityListResponse {
    private Long id;
    private String name;
    private String address;
    private String contact;
    private Integer capacity;
}