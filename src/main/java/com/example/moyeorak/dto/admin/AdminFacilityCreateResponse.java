package com.example.moyeorak.dto.admin;

import com.example.moyeorak.entity.Facility;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminFacilityCreateResponse {
    private Long id;
    private String name;
    private String regionName;

    public static AdminFacilityCreateResponse from(Facility facility) {
        return AdminFacilityCreateResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .regionName(facility.getRegion().getName())
                .build();
    }
}