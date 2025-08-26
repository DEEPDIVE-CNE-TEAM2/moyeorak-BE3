package com.example.moyeorak.service;

import com.example.moyeorak.dto.RegionRequest;
import com.example.moyeorak.dto.RegionResponse;
import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    // ======================== 생성 ========================
    @Transactional
    public RegionResponse createRegion(RegionRequest request) {
        log.info("[CREATE] 지역 생성 요청: {}", request);

        validateRegionNameUnique(request.getName());

        Region region = Region.builder()
                .name(request.getName())
                .managerId(request.getManagerId()) // ✅ User 참조 대신 managerId 값만 저장
                .build();

        return toResponse(regionRepository.save(region));
    }

    // ======================== 조회 ========================
    public List<RegionResponse> getAllRegions() {
        log.info("[GET] 전체 지역 목록 조회");

        return regionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RegionResponse getRegion(Long id) {
        log.info("[GET] 지역 상세 조회 - ID: {}", id);
        Region region = findRegionById(id);
        return toResponse(region);
    }

    // ======================== 수정 ========================
    @Transactional
    public RegionResponse updateRegion(Long id, RegionRequest request) {
        log.info("[PUT] 지역 수정 요청 - ID: {}", id);

        Region region = findRegionById(id);

        if (!region.getName().equals(request.getName())) {
            validateRegionNameUnique(request.getName());
        }

        region.setName(request.getName());
        region.setManagerId(request.getManagerId());

        return toResponse(region);
    }

    // ======================== 삭제 ========================
    @Transactional
    public MessageResponse deleteRegion(Long id) {
        log.info("[DELETE] 지역 삭제 요청 - ID: {}", id);

        if (!regionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 지역(ID: " + id + ")이 존재하지 않습니다.");
        }

        regionRepository.deleteById(id);
        return new MessageResponse("지역이 삭제되었습니다.");
    }

    // ======================== 내부 유틸 ========================
    private Region findRegionById(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지역(ID: " + id + ")이 존재하지 않습니다."));
    }

    private void validateRegionNameUnique(String name) {
        if (regionRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 지역명입니다.");
        }
    }

    private RegionResponse toResponse(Region region) {
        return RegionResponse.builder()
                .id(region.getId())
                .name(region.getName())
                .managerId(region.getManagerId()) // ✅ managerId만 내려줌
                .build();
    }
}
