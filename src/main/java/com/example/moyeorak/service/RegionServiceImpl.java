package com.example.moyeorak.service;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.RegionRequest;
import com.example.moyeorak.dto.RegionResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    // ======================== 생성 ========================
    @Override
    @Transactional
    public RegionResponse createRegion(RegionRequest request) {
        log.info("[CREATE] 지역 생성 요청: {}", request);

        // UX 차원 사전 검사 (최종 보장은 DB 유니크 제약 권장)
        validateRegionNameUnique(request.getName());

        Region region = Region.builder()
                .name(request.getName())
                .managerId(request.getManagerId()) // User 연관 대신 managerId만 저장
                .build();

        try {
            return toResponse(regionRepository.save(region));
        } catch (DataIntegrityViolationException e) {
            // name 유니크 제약 위반 시
            throw new IllegalArgumentException("이미 존재하는 지역명입니다.", e);
        }
    }

    // ======================== 조회 ========================
    @Override
    public List<RegionResponse> getAllRegions() {
        log.info("[GET] 전체 지역 목록 조회");

        return regionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public RegionResponse getRegion(Long id) {
        log.info("[GET] 지역 상세 조회 - ID: {}", id);
        Region region = findRegionById(id);
        return toResponse(region);
    }

    // ======================== 수정 ========================
    @Override
    @Transactional
    public RegionResponse updateRegion(Long id, RegionRequest request) {
        log.info("[PUT] 지역 수정 요청 - ID: {}", id);

        Region region = findRegionById(id);

        // 이름 변경 시에만 중복 검사
        if (!region.getName().equals(request.getName())) {
            validateRegionNameUnique(request.getName());
        }

        region.setName(request.getName());
        region.setManagerId(request.getManagerId());

        try {
            return toResponse(region);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 존재하는 지역명입니다.", e);
        }
    }

    // ======================== 삭제 ========================
    @Override
    @Transactional
    public MessageResponse deleteRegion(Long id) {
        log.info("[DELETE] 지역 삭제 요청 - ID: {}", id);

        if (!regionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 지역(ID: " + id + ")이 존재하지 않습니다.");
        }

        regionRepository.deleteById(id);
        return new MessageResponse("지역이 삭제되었습니다.");
    }

    // ======================== 유틸/검증 ========================
    @Override
    public boolean isRegionNameDuplicated(String name) {
        return regionRepository.existsByName(name);
    }

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
                .managerId(region.getManagerId())
                .build();
    }
}
