package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminFacilityCreateRequest;
import com.example.moyeorak.dto.admin.AdminFacilityCreateResponse;
import com.example.moyeorak.dto.admin.AdminFacilityDetailResponse;
import com.example.moyeorak.dto.admin.AdminFacilityListResponse;
import com.example.moyeorak.dto.admin.AdminFacilityUpdateRequest;
import com.example.moyeorak.entity.Facility;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.FacilityRepository;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFacilityServiceImpl implements AdminFacilityService {

    private final FacilityRepository facilityRepository;
    private final RegionRepository regionRepository;

    /** 시설 생성 */
    @Override
    @Transactional
    public AdminFacilityCreateResponse createFacility(AdminFacilityCreateRequest request, Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        assertRegionManager(userId, region);

        Facility facility = Facility.builder()
                .name(request.getName())
                .address(request.getAddress())
                .location(request.getLocation())
                .contact(request.getContact())
                .imageUrl(request.getImageUrl())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .area(request.getArea())
                .usageStartTime(parseTime(request.getUsageStartTime()))
                .usageEndTime(parseTime(request.getUsageEndTime()))
                .regionId(region.getId())   // FK만 세팅
                .build();

        Facility saved = facilityRepository.save(facility);
        return AdminFacilityCreateResponse.from(saved);
    }

    /** 시설 목록 조회 (지역 단위) */
    @Override
    public List<AdminFacilityListResponse> getFacilityList(Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        assertRegionManager(userId, region);

        return facilityRepository.findByRegionId(regionId).stream()
                .map(f -> AdminFacilityListResponse.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .address(f.getAddress())
                        .contact(f.getContact())
                        .capacity(f.getCapacity())
                        .build())
                .toList();
    }

    /** 시설 상세 조회 */
    @Override
    public AdminFacilityDetailResponse getFacilityDetail(Long facilityId, Long userId) {
        Facility f = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));
        assertRegionManagerByRegionId(userId, f.getRegionId());

        return AdminFacilityDetailResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .address(f.getAddress())
                .usageStartTime(toStringOrNull(f.getUsageStartTime()))
                .usageEndTime(toStringOrNull(f.getUsageEndTime()))
                .contact(f.getContact())
                .capacity(f.getCapacity())
                .description(f.getDescription())
                .imageUrl(f.getImageUrl())
                .build();
    }

    /** 시설 수정 */
    @Override
    @Transactional
    public AdminFacilityDetailResponse updateFacility(Long facilityId, AdminFacilityUpdateRequest request, Long userId) {
        Facility f = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));
        assertRegionManagerByRegionId(userId, f.getRegionId());

        f.setName(request.getName());
        f.setAddress(request.getAddress());
        f.setUsageStartTime(parseTime(request.getUsageStartTime()));
        f.setUsageEndTime(parseTime(request.getUsageEndTime()));
        f.setContact(request.getContact());
        f.setCapacity(request.getCapacity());
        f.setDescription(request.getDescription());
        f.setImageUrl(request.getImageUrl());

        return AdminFacilityDetailResponse.builder()
                .id(f.getId())
                .name(f.getName())
                .address(f.getAddress())
                .usageStartTime(toStringOrNull(f.getUsageStartTime()))
                .usageEndTime(toStringOrNull(f.getUsageEndTime()))
                .contact(f.getContact())
                .capacity(f.getCapacity())
                .description(f.getDescription())
                .imageUrl(f.getImageUrl())
                .build();
    }

    /** 시설 삭제 */
    @Override
    @Transactional
    public void deleteFacility(Long facilityId, Long userId) {
        Facility f = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));
        assertRegionManagerByRegionId(userId, f.getRegionId());
        facilityRepository.delete(f);
    }

    // ───────── helpers ─────────
    private void assertRegionManager(Long userId, Region region) {
        Long managerId = region.getManagerId();
        if (managerId == null || !managerId.equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }
    }

    private void assertRegionManagerByRegionId(Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        assertRegionManager(userId, region);
    }

    private LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) return null;
        try {
            return LocalTime.parse(time);
        } catch (Exception e) {
            // ErrorCode에 INVALID_INPUT_VALUE가 없으므로 표준 예외로 처리
            throw new IllegalArgumentException("Invalid time format: " + time);
        }
    }

    private String toStringOrNull(LocalTime t) {
        return (t == null) ? null : t.toString();
    }
}
