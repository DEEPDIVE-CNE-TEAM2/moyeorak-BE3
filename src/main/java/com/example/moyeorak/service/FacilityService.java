package com.example.moyeorak.service;

import com.example.moyeorak.dto.FacilityDto;
import com.example.moyeorak.dto.FacilitySimpleDto;
import com.example.moyeorak.dto.FacilityDetailDto;
import com.example.moyeorak.dto.FacilityUpdateDto;

import java.util.List;

public interface FacilityService {

    FacilityDto createFacility(FacilityDto dto);

    List<FacilitySimpleDto> getFacilitiesByRegion(Long regionId);

    FacilityDetailDto getFacility(Long id);

    FacilityDto updateFacility(Long id, FacilityUpdateDto dto);

    void deleteFacility(Long id);
}
