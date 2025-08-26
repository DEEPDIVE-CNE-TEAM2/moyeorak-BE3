package com.example.moyeorak.service;

import org.springframework.transaction.annotation.Transactional;
import com.example.moyeorak.dto.MainImageRequest;
import com.example.moyeorak.dto.MainImageResponse;
import com.example.moyeorak.entity.MainImage;
import com.example.moyeorak.repository.MainImageRepository;
import com.example.moyeorak.repository.RegionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainImageService {

    private final MainImageRepository mainImageRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public MainImageResponse create(@Valid MainImageRequest request) {
        Long regionId = request.regionId();

        // 지역 존재 여부 검증
        if (!regionRepository.existsById(regionId)) {
            throw new IllegalArgumentException("존재하지 않는 지역입니다.");
        }

        // 순서 중복 체크
        if (mainImageRepository.existsByRegionIdAndDisplayOrder(regionId, request.displayOrder())) {
            throw new IllegalArgumentException("해당 지역에 이미 사용 중인 노출 순서입니다.");
        }

        MainImage image = MainImage.builder()
                .title(request.title())
                .imageUrl(request.imageUrl())
                .displayOrder(request.displayOrder())
                .isActive(request.isActive() != null && request.isActive())
                .regionId(regionId) // ✅ Region 엔티티 대신 ID 값만 저장
                .build();

        return MainImageResponse.from(mainImageRepository.save(image));
    }

    @Transactional(readOnly = true)
    public List<MainImageResponse> getByRegion(Long regionId) {
        return mainImageRepository.findByRegionIdOrderByDisplayOrderAsc(regionId).stream()
                .map(MainImageResponse::from)
                .toList();
    }

    @Transactional
    public MainImageResponse update(Long id, MainImageRequest request) {
        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다."));

        // ✅ 순서 변경 시 중복 체크
        if (request.displayOrder() != null &&
                !image.getDisplayOrder().equals(request.displayOrder()) &&
                mainImageRepository.existsByRegionIdAndDisplayOrder(image.getRegionId(), request.displayOrder())) {
            throw new IllegalArgumentException("해당 지역에 이미 사용 중인 노출 순서입니다.");
        }

        image.update(
                request.title() != null ? request.title() : image.getTitle(),
                request.imageUrl() != null ? request.imageUrl() : image.getImageUrl(),
                request.displayOrder() != null ? request.displayOrder() : image.getDisplayOrder(),
                request.isActive() != null ? request.isActive() : image.isActive()
        );

        return MainImageResponse.from(image);
    }

    @Transactional
    public void delete(Long id) {
        mainImageRepository.deleteById(id);
    }
}
