package com.example.moyeorak.service;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.RegionRequest;
import com.example.moyeorak.dto.RegionResponse;

import java.util.List;

public interface RegionService {

    // 생성
    RegionResponse createRegion(RegionRequest request);

    // 조회
    List<RegionResponse> getAllRegions();
    RegionResponse getRegion(Long id);

    // 수정
    RegionResponse updateRegion(Long id, RegionRequest request);

    // 삭제
    MessageResponse deleteRegion(Long id);

    // 중복 체크 (선택)
    boolean isRegionNameDuplicated(String name);
}
