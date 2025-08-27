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
    private Long regionId;     // ✅ MAS: FK 그대로 노출
    private String regionName; // 옵션: 서비스에서 넘겨줄 때만 세팅

    /** Facility만으로 생성 (regionName은 null) */
    public static AdminFacilityCreateResponse from(Facility facility) {
        return AdminFacilityCreateResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .regionId(facility.getRegionId())
                .build();
    }

    /** 지역명을 함께 내려주고 싶을 때 사용하는 오버로드 */
    public static AdminFacilityCreateResponse from(Facility facility, String regionName) {
        return AdminFacilityCreateResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .regionId(facility.getRegionId())
                .regionName(regionName)
                .build();
    }
}
