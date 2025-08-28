package com.example.moyeorak.service;

import com.example.moyeorak.dto.ProgramDisplayResponse;
import com.example.moyeorak.dto.ProgramRequest;

import java.util.List;
import java.util.Map;

public interface ProgramService {

    // 생성
    ProgramDisplayResponse createProgram(ProgramRequest dto);

    // 조회
    List<ProgramDisplayResponse> getAllPrograms(Long userRegionId);
    List<ProgramDisplayResponse> getProgramsByRegion(Long regionId, Long userRegionId);
    ProgramDisplayResponse getProgramById(Long id, Long userRegionId);

    // 부분 수정
    ProgramDisplayResponse partialUpdateProgram(Long id, Map<String, Object> updates);

    // 삭제
    void deleteProgram(Long id);

    // 기타
    boolean isAsyncPeriod(Long programId);
}
