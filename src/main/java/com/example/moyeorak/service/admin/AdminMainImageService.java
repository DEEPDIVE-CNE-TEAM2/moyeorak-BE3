package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.entity.MainImage;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.MainImageRepository;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMainImageService {

    private final MainImageRepository mainImageRepository;
    private final RegionRepository regionRepository;

    // ───────────────────────── 조회 ─────────────────────────
    /** 지역별 홍보물 리스트 */
    public List<AdminMainImageResponse> getMainImages(Long userId, Long regionId) {
        assertRegionManagerByRegionId(userId, regionId);

        return mainImageRepository.findByRegionIdOrderByDisplayOrderAsc(regionId)
                .stream()
                .map(AdminMainImageResponse::from)
                .toList();
    }

    // ───────────────────────── 생성 ─────────────────────────
    /** 홍보물 생성 */
    @Transactional
    public AdminMainImageResponse createMainImage(AdminMainImageCreateRequest dto, Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        assertRegionManager(userId, region);

        Integer maxOrder = mainImageRepository.findMaxDisplayOrderByRegionId(regionId);
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        MainImage image = MainImage.builder()
                .imageUrl(dto.getImageUrl())
                // title은 DTO에 없으므로 엔티티 기본값("") 사용
                .displayOrder(nextOrder)
                .isActive(true)
                .regionId(regionId) // ✅ MAS: FK만 저장
                .build();

        return AdminMainImageResponse.from(mainImageRepository.save(image));
    }

    // ───────────────────────── 일괄 수정 ─────────────────────────
    /** 홍보물 일괄 수정 (표시여부/정렬순서) */
    @Transactional
    public void updateMainImages(Long userId, Long regionId, List<AdminMainImageUpdateRequest> requestList) {
        if (requestList == null || requestList.isEmpty()) return;

        // 요청 region 권한 확인
        assertRegionManagerByRegionId(userId, regionId);

        for (AdminMainImageUpdateRequest req : requestList) {
            MainImage image = mainImageRepository.findById(req.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MAIN_IMAGE_ID));

            // 이미지가 해당 region에 속하는지 검증
            if (image.getRegionId() != null && !image.getRegionId().equals(regionId)) {
                // 기존에 없던 코드 대신 기존에 존재하는 권한 오류 코드 사용
                throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
            }

            if (req.getDisplayOrder() != null) {
                image.setDisplayOrder(req.getDisplayOrder());
            }
            if (req.getIsActive() != null) {
                image.changeActiveStatus(req.getIsActive());
            }
        }
        // JPA dirty checking 자동 반영
    }

    // ───────────────────────── 삭제 ─────────────────────────
    @Transactional
    public void deleteById(Long id, Long userId) {
        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MAIN_IMAGE_ID));

        assertRegionManagerByRegionId(userId, image.getRegionId());
        mainImageRepository.delete(image);
    }

    // ───────────────────────── helpers ─────────────────────────
    private void assertRegionManagerByRegionId(Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        assertRegionManager(userId, region);
    }

    private void assertRegionManager(Long userId, Region region) {
        Long managerId = region.getManagerId();
        if (managerId == null || !managerId.equals(userId)) {
            // 기존에 없던 MAIN_IMAGE 전용 코드 대신 공통 권한 코드 사용
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }
    }
}
