package com.example.moyeorak.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionResponse {
    private Long id;
    private String name;
    private ManagerDto manager;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerDto {
        private Long id;
        private String name;
        private String email;
    }
}