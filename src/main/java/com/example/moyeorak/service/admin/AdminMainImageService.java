package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;

import java.util.List;

public interface AdminMainImageService {

    // 조회
    List<AdminMainImageResponse> getMainImages(Long userId, Long regionId);

    // 생성
    AdminMainImageResponse createMainImage(AdminMainImageCreateRequest dto, Long userId, Long regionId);

    // 일괄 수정
    void updateMainImages(Long userId, Long regionId, List<AdminMainImageUpdateRequest> requestList);

    // 삭제
    void deleteById(Long id, Long userId);
}
