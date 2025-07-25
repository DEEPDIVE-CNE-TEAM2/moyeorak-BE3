package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminProgramListResponse;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.EnrollmentRepository;
import com.example.moyeorak.repository.ProgramRepository;
import com.example.moyeorak.repository.RegionRepository;
import com.example.moyeorak.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProgramService {

    private final ProgramRepository programRepository;         // 프로그램 전체 목록
    private final EnrollmentRepository enrollmentRepository;   // 신청 인원 카운트
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;



    public List<AdminProgramListResponse> getProgramsByRegionAndTitle(HttpServletRequest request, Long regionId, String title) {
        // 1. 토큰 → 관리자 이메일
        String token = jwtProvider.resolveToken(request);
        String email = jwtProvider.getEmail(token);

        // 2. 관리자 유저 확인
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보가 없습니다."));
        if (admin.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }

        // 3. 조회할 지역 결정
        Region targetRegion;
        if (regionId == null) {
            targetRegion = admin.getRegion();
            if (targetRegion == null) {
                throw new IllegalStateException("관리자에게 지역 정보가 없습니다.");
            }
        } else {
            targetRegion = regionRepository.findById(regionId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 지역이 존재하지 않습니다."));
        }

        // 4. 조건에 맞는 프로그램 조회
        List<Program> programs;
        if (title == null || title.trim().isEmpty()) {
            programs = programRepository.findByRegion(targetRegion);
        } else {
            programs = programRepository.findByRegionAndTitleContainingIgnoreCase(targetRegion, title.trim());
        }

        // 5. DTO 변환
        return programs.stream()
                .map(this::toAdminListDto)
                .toList();
    }

    // 각 프로그램을 Admin용 DTO로 변환하는 메서드
    private AdminProgramListResponse toAdminListDto(Program program) {
        int currentEnrollment = 0; // 일단 0으로 시작. 나중에 enrollmentRepository.countByProgramId()로 바꿀거임

        return AdminProgramListResponse.builder()
                .id(program.getId())
                .title(program.getTitle())
                .facilityName(program.getFacility().getName())
                .usagePeriod(formatDateRange(program.getUsageStartDate(), program.getUsageEndDate()))
                .capacity(program.getCapacity())
                .currentEnrollment(currentEnrollment)
                .progressStatus(getProgressStatus(program.getUsageStartDate(), program.getUsageEndDate()))
                .build();
    }

    // 날짜 "YYYY-MM-DD ~ YYYY-MM-DD" 포맷팅
    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }

    // 오늘 날짜 기준으로 수업 상태 판단: 수업 예정 / 진행중 / 수업 종료
    private String getProgressStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(start)) return "수업 예정";
        else if (!today.isAfter(end)) return "진행중";
        else return "수업 종료";
    }
}