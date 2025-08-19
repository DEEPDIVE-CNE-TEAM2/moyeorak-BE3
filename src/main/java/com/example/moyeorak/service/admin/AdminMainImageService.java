package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.entity.MainImage;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.MainImageRepository;
import com.example.moyeorak.security.AdminAuthHelper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMainImageService {

    private final MainImageRepository mainImageRepository;
    private final AdminAuthHelper adminAuthHelper;


    // 홍보물 리스트 조회
    @Transactional(readOnly = true)
    public List<AdminMainImageResponse> getMainImages(HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        Long regionId = admin.getRegion().getId();

        return mainImageRepository.findByRegionIdOrderByDisplayOrderAsc(regionId).stream()
                .map(AdminMainImageResponse::from)
                .toList();
    }

    // 홍보물 생성
    @Transactional
    public AdminMainImageResponse createMainImage(AdminMainImageCreateRequest dto, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        Long regionId = admin.getRegion().getId();

        Integer maxOrder = mainImageRepository.findMaxDisplayOrderByRegionId(regionId);
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        MainImage image = MainImage.builder()
                .imageUrl(dto.getImageUrl())
                .title("")
                .displayOrder(nextOrder)
                .isActive(true)
                .region(admin.getRegion())
                .build();

        return AdminMainImageResponse.from(mainImageRepository.save(image));
    }

    // 홍보물 일괄 수정 (표시여부 + 순서)
    @Transactional
    public void updateMainImages(List<AdminMainImageUpdateRequest> requestList) {
        for (AdminMainImageUpdateRequest req : requestList) {
            MainImage image = mainImageRepository.findById(req.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MAIN_IMAGE_ID));

            //image.changeDisplayOrder(req.getDisplayOrder()); 다시 처리 할 예정
            image.changeActiveStatus(req.getIsActive());
        }
    }

    // 홍보물 삭제
    public void deleteById(Long id) {
        if (!mainImageRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_MAIN_IMAGE_ID);
        }
        mainImageRepository.deleteById(id);
    }

}