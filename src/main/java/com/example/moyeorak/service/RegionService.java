package com.example.moyeorak.service;

import com.example.moyeorak.dto.RegionRequest;
import com.example.moyeorak.dto.RegionResponse;
import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public RegionResponse createRegion(RegionRequest request) {
        log.info("[CREATE] 지역 생성 요청: {}", request);

        if (regionRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("이미 존재하는 지역명입니다.");
        }

        Region region = Region.builder()
                .name(request.getName())
                .manager(findManagerById(request.getManagerId()))
                .build();

        return toResponse(regionRepository.save(region));
    }

    public List<RegionResponse> getAllRegions() {
        log.info("[GET] 전체 지역 목록 조회");
        return regionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RegionResponse getRegion(Long id) {
        log.info("[GET] 지역 상세 조회 - ID: {}", id);
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지역(ID: " + id + ")이 존재하지 않습니다."));
        return toResponse(region);
    }

    @Transactional
    public RegionResponse updateRegion(Long id, RegionRequest request) {
        log.info("[PUT] 지역 수정 요청 - ID: {}", id);

        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지역(ID: " + id + ")이 존재하지 않습니다."));

        region.setName(request.getName());
        region.setManager(findManagerById(request.getManagerId()));

        return toResponse(region);
    }

    @Transactional
    public MessageResponse deleteRegion(Long id) {
        log.info("[DELETE] 지역 삭제 요청 - ID: {}", id);

        if (!regionRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 지역(ID: " + id + ")이 존재하지 않습니다.");
        }

        regionRepository.deleteById(id);
        return new MessageResponse("지역이 삭제되었습니다.");
    }

    private RegionResponse toResponse(Region region) {
        Long managerId = region.getManager() != null ? region.getManager().getId() : null;
        return RegionResponse.builder()
                .id(region.getId())
                .name(region.getName())
                .managerId(Math.toIntExact(managerId))
                .build();
    }

    private User findManagerById(Long managerId) {
        if (managerId == null) return null;
        return userRepository.findById(Math.toIntExact(managerId))
                .orElseThrow(() -> new IllegalArgumentException("관리자 ID(" + managerId + ")가 존재하지 않습니다."));
    }
}
