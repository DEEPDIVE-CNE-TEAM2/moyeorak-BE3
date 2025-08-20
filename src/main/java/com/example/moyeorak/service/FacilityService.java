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
import org.springframework.beans.factory.annotation.Value;
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

    // CloudFront 도메인 (application.properties에서 주입)
    @Value("${cloudfront.base-url}")
    private String cloudFrontBaseUrl;

    private String formatUsageTime(Facility facility) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return facility.getUsageStartTime().format(formatter) + " ~ " + facility.getUsageEndTime().format(formatter);
    }

    /**
     * 들어온 값(파일명/오브젝트 키 혹은 절대 URL)을 '파일명/오브젝트 키'만 남기도록 정규화
     * - blob:/data: URL은 거부
     * - http(s) URL은 마지막 '/' 뒤만 추출(쿼리/프래그먼트 제거)
     * - ':' 문자가 남아 있으면 거부(스킴 혼입 방지)
     * - 'img/2025/08/foo.jpg' 같은 오브젝트 키는 허용
     */
    private String normalizeToFileName(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String val = raw.trim();

        // 프런트 전용/인라인 데이터는 즉시 거부
        if (val.startsWith("blob:")) {
            throw new IllegalArgumentException("blob: URL은 허용되지 않습니다. S3 업로드 후 파일명(또는 오브젝트 키)만 전달하세요.");
        }
        if (val.startsWith("data:")) {
            throw new IllegalArgumentException("data: URL은 허용되지 않습니다. S3 업로드 후 파일명(또는 오브젝트 키)만 전달하세요.");
        }

        // 절대 URL 처리
        if (val.startsWith("http://") || val.startsWith("https://")) {
            int qm = val.indexOf('?');
            int hash = val.indexOf('#');
            int cut = val.length();
            if (qm >= 0) cut = Math.min(cut, qm);
            if (hash >= 0) cut = Math.min(cut, hash);

            String withoutQuery = val.substring(0, cut);
            String fileName = withoutQuery.substring(withoutQuery.lastIndexOf('/') + 1);

            if (fileName.isBlank()) {
                throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다(파일명이 비어 있음).");
            }
            if (fileName.contains(":")) {
                throw new IllegalArgumentException("이미지 파일명에 ':' 문자는 허용되지 않습니다.");
            }
            return fileName;
        }

        // 이미 파일명(또는 오브젝트 키)인 경우: 간단 검증
        if (val.contains(":")) {
            throw new IllegalArgumentException("이미지 파일명에 ':' 문자는 허용되지 않습니다.");
        }
        // 앞/뒤 불필요한 '/' 제거는 하지 않음(키의 일부일 수 있으므로), 공백만 제거
        return val;
    }

    /**
     * 응답용 절대경로: CloudFront 베이스 + 파일명/키
     * cloudFrontBaseUrl의 슬래시 유무와 상관없이 안전하게 결합
     */
    private String toCloudFrontUrl(String fileNameOrUrl) {
        if (fileNameOrUrl == null || fileNameOrUrl.isBlank()) return null;
        String fileNameOrKey = normalizeToFileName(fileNameOrUrl); // blob/data 차단 포함
        return joinUrl(cloudFrontBaseUrl, fileNameOrKey);
    }

    private String joinUrl(String base, String path) {
        if (base.endsWith("/")) {
            if (path.startsWith("/")) return base + path.substring(1);
            return base + path;
        } else {
            if (path.startsWith("/")) return base + path;
            return base + "/" + path;
        }
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
                // 저장은 파일명/오브젝트 키만 (절대 URL이 들어와도 정규화)
                .imageUrl(normalizeToFileName(dto.getImageUrl()))
                .capacity(dto.getCapacity())
                .description(dto.getDescription())
                .area(dto.getArea())
                .region(region)
                .usageStartTime(dto.getUsageStartTime())
                .usageEndTime(dto.getUsageEndTime())
                .build();

        Facility saved = facilityRepository.save(facility);

        // 응답은 CloudFront 절대경로로 통일
        return FacilityDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .address(saved.getAddress())
                .location(saved.getLocation())
                .contact(saved.getContact())
                .imageUrl(toCloudFrontUrl(saved.getImageUrl()))
                .capacity(saved.getCapacity())
                .description(saved.getDescription())
                .regionId(saved.getRegion().getId())
                .usageStartTime(saved.getUsageStartTime())
                .usageEndTime(saved.getUsageEndTime())
                .area(saved.getArea())
                .build();
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
                        // 항상 CloudFront 절대경로로 응답
                        .imageUrl(toCloudFrontUrl(facility.getImageUrl()))
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
                .imageUrl(toCloudFrontUrl(facility.getImageUrl()))
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
        if (dto.getImageUrl() != null) {
            // 업데이트 시에도 DB에는 파일명/오브젝트 키만 저장
            facility.setImageUrl(normalizeToFileName(dto.getImageUrl()));
        }
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

        // 응답은 CloudFront 절대경로로 통일
        return FacilityDto.builder()
                .id(facility.getId())
                .name(facility.getName())
                .address(facility.getAddress())
                .contact(facility.getContact())
                .imageUrl(toCloudFrontUrl(facility.getImageUrl()))
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
