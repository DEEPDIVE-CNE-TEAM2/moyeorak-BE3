package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.*;
import com.example.moyeorak.entity.*;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.FacilityRepository;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.security.AdminAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminFacilityService {

    private final FacilityRepository facilityRepository;
    private final RegionRepository regionRepository;
    private final AdminAuthHelper adminAuthHelper;


    @Transactional
    public AdminFacilityCreateResponse createFacility(AdminFacilityCreateRequest request, HttpServletRequest httpRequest) {
        User admin = adminAuthHelper.getAdminFromRequest(httpRequest);
        Region region = admin.getRegion();

        Facility facility = Facility.builder()
                .name(request.getName())
                .address(request.getAddress())
                .location(request.getLocation())
                .contact(request.getContact())
                .imageUrl(request.getImageUrl())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .region(region)
                .area(request.getArea())
                .usageStartTime(LocalTime.parse(request.getUsageStartTime()))
                .usageEndTime(LocalTime.parse(request.getUsageEndTime()))
                .build();

        Facility saved = facilityRepository.save(facility);
        return AdminFacilityCreateResponse.from(saved);
    }

    // 시설 리스트 조회
    @Transactional(readOnly = true)
    public List<AdminFacilityListResponse> getFacilityList(HttpServletRequest httpRequest) {
        User admin = adminAuthHelper.getAdminFromRequest(httpRequest);
        Region region = admin.getRegion();

        List<Facility> facilities = facilityRepository.findByRegion(region);

        return facilities.stream()
                .map(facility -> AdminFacilityListResponse.builder()
                        .id(facility.getId())
                        .name(facility.getName())
                        .address(facility.getAddress())
                        .contact(facility.getContact())
                        .capacity(facility.getCapacity())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminFacilityDetailResponse getFacilityDetail(Long facilityId, HttpServletRequest httpRequest) {
        User admin = adminAuthHelper.getAdminFromRequest(httpRequest);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

        if (!facility.getRegion().equals(admin.getRegion())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }

        return AdminFacilityDetailResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .usageStartTime(facility.getUsageStartTime().toString())
                .usageEndTime(facility.getUsageEndTime().toString())
                .contact(facility.getContact())
                .capacity(facility.getCapacity())
                .description(facility.getDescription())
                .imageUrl(facility.getImageUrl())
                .build();
    }

    // 수정
    @Transactional
    public AdminFacilityDetailResponse updateFacility(Long facilityId, AdminFacilityUpdateRequest request, HttpServletRequest httpRequest) {
        User admin = adminAuthHelper.getAdminFromRequest(httpRequest);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

        if (!facility.getRegion().equals(admin.getRegion())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }

        facility.setName(request.getName());
        facility.setAddress(request.getAddress());
        facility.setUsageStartTime(LocalTime.parse(request.getUsageStartTime()));
        facility.setUsageEndTime(LocalTime.parse(request.getUsageEndTime()));
        facility.setContact(request.getContact());
        facility.setCapacity(request.getCapacity());
        facility.setDescription(request.getDescription());
        facility.setImageUrl(request.getImageUrl());

        return AdminFacilityDetailResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .usageStartTime(facility.getUsageStartTime().toString())
                .usageEndTime(facility.getUsageEndTime().toString())
                .contact(facility.getContact())
                .capacity(facility.getCapacity())
                .description(facility.getDescription())
                .imageUrl(facility.getImageUrl())
                .build();
    }

    @Transactional
    public void deleteFacility(Long facilityId, HttpServletRequest httpRequest) {
        User admin = adminAuthHelper.getAdminFromRequest(httpRequest);
        Facility facility = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

        if (!facility.getRegion().equals(admin.getRegion())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }
        facilityRepository.delete(facility);
    }

}