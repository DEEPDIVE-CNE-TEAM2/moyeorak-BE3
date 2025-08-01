package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.entity.MainImage;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.MainImageRepository;
import com.example.moyeorak.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMainImageService {

    private final UserRepository userRepository;
    private final MainImageRepository mainImageRepository;
    private final JwtProvider jwtProvider;

    // 홍보물 리스트 조회
    @Transactional(readOnly = true)
    public List<AdminMainImageResponse> getMainImages(HttpServletRequest request) {
        User admin = getAuthenticatedAdmin(request);
        Long regionId = admin.getRegion().getId();

        return mainImageRepository.findByRegionIdOrderByDisplayOrderAsc(regionId).stream()
                .map(AdminMainImageResponse::from)
                .toList();
    }

    // 홍보물 생성
    @Transactional
    public AdminMainImageResponse createMainImage(AdminMainImageCreateRequest dto, HttpServletRequest request) {
        User admin = getAuthenticatedAdmin(request);
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
                    .orElseThrow(() -> new EntityNotFoundException("해당 ID 없음: " + req.getId()));

            //image.changeDisplayOrder(req.getDisplayOrder()); 다시 처리 할 예정
            image.changeActiveStatus(req.getIsActive());
        }
    }

    // 홍보물 삭제
    public void deleteById(Long id) {
        if (!mainImageRepository.existsById(id)) {
            throw new EntityNotFoundException("해당 ID의 홍보물이 존재하지 않음");
        }
        mainImageRepository.deleteById(id);
    }


    // 관리자 인증 메서드
    private User getAuthenticatedAdmin(HttpServletRequest request) {
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        if (user.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return user;
    }
}