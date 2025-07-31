package com.example.moyeorak.service;

import com.example.moyeorak.dto.FacilityDto;
import com.example.moyeorak.dto.FacilitySimpleDto;
import com.example.moyeorak.dto.FacilityDetailDto;
import com.example.moyeorak.dto.FacilityUpdateDto;
import com.example.moyeorak.entity.Facility;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.repository.FacilityRepository;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final RegionRepository regionRepository;

    private static final String S3_BASE_URL = "https://s3-goorm-frontend.s3.ap-northeast-2.amazonaws.com/img/";

    private String formatUsageTime(Facility facility) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return facility.getUsageStartTime().format(formatter) + " ~ " + facility.getUsageEndTime().format(formatter);
    }

    private String fullImageUrl(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        return S3_BASE_URL + fileName;
    }

    @Transactional
    public FacilityDto createFacility(FacilityDto dto) {
        Region region = regionRepository.findById(dto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        Facility facility = Facility.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .location(dto.getLocation())
                .contact(dto.getContact())
                .imageUrl(dto.getImageUrl())  // 저장은 파일명만
                .capacity(dto.getCapacity())
                .description(dto.getDescription())
                .area(dto.getArea())
                .region(region)
                .usageStartTime(dto.getUsageStartTime())
                .usageEndTime(dto.getUsageEndTime())
                .build();

        Facility saved = facilityRepository.save(facility);
        dto.setId(saved.getId());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<FacilitySimpleDto> getFacilitiesByRegion(Long regionId) {
        return facilityRepository.findByRegionId(regionId).stream()
                .map(facility -> FacilitySimpleDto.builder()
                        .id(facility.getId())
                        .location(facility.getName())
                        .address(facility.getAddress())
                        .usageTime(formatUsageTime(facility))
                        .contact(facility.getContact())
                        .imageUrl(fullImageUrl(facility.getImageUrl()))  // 절대경로 적용
                        .area(facility.getArea())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacilityDetailDto getFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        return FacilityDetailDto.builder()
                .id(facility.getId())
                .location(facility.getName())
                .address(facility.getAddress())
                .usageTime(formatUsageTime(facility))
                .capacity(facility.getCapacity())
                .imageUrl(fullImageUrl(facility.getImageUrl()))  // 절대경로 적용
                .description(facility.getDescription())
                .contact(facility.getContact())
                .build();
    }

    @Transactional
    public FacilityDto updateFacility(Long id, FacilityUpdateDto dto) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        if (dto.getName() != null) facility.setName(dto.getName());
        if (dto.getAddress() != null) facility.setAddress(dto.getAddress());
        if (dto.getContact() != null) facility.setContact(dto.getContact());
        if (dto.getImageUrl() != null) facility.setImageUrl(dto.getImageUrl());
        if (dto.getCapacity() != null) facility.setCapacity(dto.getCapacity());
        if (dto.getDescription() != null) facility.setDescription(dto.getDescription());
        if (dto.getLocation() != null) facility.setLocation(dto.getLocation());
        if (dto.getArea() != null) facility.setArea(dto.getArea());

        if (dto.getUsageStartTime() != null)
            facility.setUsageStartTime(LocalTime.parse(dto.getUsageStartTime()));
        if (dto.getUsageEndTime() != null)
            facility.setUsageEndTime(LocalTime.parse(dto.getUsageEndTime()));

        if (dto.getRegionId() != null) {
            Region region = regionRepository.findById(Long.valueOf(dto.getRegionId()))
                    .orElseThrow(() -> new IllegalArgumentException("Region not found"));
            facility.setRegion(region);
        }

        return FacilityDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .contact(facility.getContact())
                .imageUrl(fullImageUrl(facility.getImageUrl()))  // 절대경로 적용
                .capacity(facility.getCapacity())
                .description(facility.getDescription())
                .regionId(facility.getRegion().getId())
                .location(facility.getLocation())
                .usageStartTime(facility.getUsageStartTime())
                .usageEndTime(facility.getUsageEndTime())
                .area(facility.getArea())
                .build();
    }

    @Transactional
    public void deleteFacility(Long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
        facilityRepository.delete(facility);
    }
}
