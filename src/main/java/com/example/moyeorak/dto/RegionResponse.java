package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionResponse {
    private Long id;
    private String name;
    private Long managerId; // ✅ User 연관 대신 managerId만 반환
}
