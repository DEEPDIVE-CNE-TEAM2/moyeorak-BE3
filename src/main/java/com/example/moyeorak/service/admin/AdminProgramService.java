package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.admin.AdminProgramCreateRequest;
import com.example.moyeorak.dto.admin.AdminProgramDetailResponse;
import com.example.moyeorak.dto.admin.AdminProgramListResponse;
import com.example.moyeorak.dto.admin.AdminProgramUpdateRequest;

import java.util.List;

public interface AdminProgramService {

    // 목록
    List<AdminProgramListResponse> getProgramsByRegionAndTitle(Long userId, Long regionId, String title);

    // 생성
    Long createProgram(AdminProgramCreateRequest request, Long userId);

    // 상세
    AdminProgramDetailResponse getProgramDetail(Long userId, Long programId);

    // 수정(부분)
    Long patchProgram(Long programId, AdminProgramUpdateRequest request, Long userId);

    // 삭제
    MessageResponse deleteProgram(Long programId, Long userId);
}
