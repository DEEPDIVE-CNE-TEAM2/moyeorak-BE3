package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminFacilityCreateRequest;
import com.example.moyeorak.dto.admin.AdminFacilityCreateResponse;
import com.example.moyeorak.dto.admin.AdminFacilityDetailResponse;
import com.example.moyeorak.dto.admin.AdminFacilityListResponse;
import com.example.moyeorak.dto.admin.AdminFacilityUpdateRequest;

import java.util.List;

public interface AdminFacilityService {

    AdminFacilityCreateResponse createFacility(AdminFacilityCreateRequest request, Long userId, Long regionId);

    List<AdminFacilityListResponse> getFacilityList(Long userId, Long regionId);

    AdminFacilityDetailResponse getFacilityDetail(Long facilityId, Long userId);

    AdminFacilityDetailResponse updateFacility(Long facilityId, AdminFacilityUpdateRequest request, Long userId);

    void deleteFacility(Long facilityId, Long userId);
}
