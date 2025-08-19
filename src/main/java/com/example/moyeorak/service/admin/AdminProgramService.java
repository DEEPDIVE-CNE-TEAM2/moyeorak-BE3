package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.admin.AdminProgramCreateRequest;
import com.example.moyeorak.dto.admin.AdminProgramDetailResponse;
import com.example.moyeorak.dto.admin.AdminProgramListResponse;
import com.example.moyeorak.dto.admin.AdminProgramUpdateRequest;
import com.example.moyeorak.entity.Facility;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.*;
import com.example.moyeorak.security.AdminAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProgramService {

    private final ProgramRepository programRepository;         // 프로그램 전체 목록
    private final EnrollmentRepository enrollmentRepository;   // 신청 인원 카운트
    private final RegionRepository regionRepository;
    private final FacilityRepository facilityRepository;
    private final AdminAuthHelper adminAuthHelper;




    // 프로그램 리스트 조회
    public List<AdminProgramListResponse> getProgramsByRegionAndTitle(HttpServletRequest request, Long regionId, String title) {
        // 관리자 검증
        Region targetRegion = (regionId == null)
                ? adminAuthHelper.getAdminRegion(request)
                : regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        // 지역 결정
        List<Program> programs = (title == null || title.trim().isEmpty())
                ? programRepository.findByRegion(targetRegion)
                : programRepository.findByRegionAndTitleContainingIgnoreCase(targetRegion, title.trim());

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


    // 프로그램 생성
    @Transactional
    public Long createProgram(AdminProgramCreateRequest request, HttpServletRequest httpRequest) {
        // 지역과 시설 조회 + 일치 여부 검증
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

        if (!facility.getRegion().getId().equals(region.getId())) {
            throw new BusinessException(ErrorCode.FACILITY_REGION_MISMATCH);
        }

        // 프로그램 엔티티 생성
        Program program = Program.builder()
                .title(request.getTitle())
                .region(region)
                .facility(facility)
                .category(request.getCategory())
                .target(request.getTarget())
                .instructorName(request.getInstructorName())
                .status("CLOSED".equalsIgnoreCase(request.getStatus()) ? Program.Status.CLOSED : Program.Status.OPEN)
                .usageStartDate(request.getUsageStartDate())
                .usageEndDate(request.getUsageEndDate())
                .classStartTime(request.getClassStartTime())
                .classEndTime(request.getClassEndTime())
                .registrationStartDate(request.getRegistrationStartDate())
                .registrationEndDate(request.getRegistrationEndDate())
                .cancelEndDate(request.getCancelEndDate())
                .inPrice(request.getInPrice())
                .outPrice(request.getOutPrice())
                .capacity(request.getCapacity())
                .contact(request.getContact())
                .imageUrl(request.getImageUrl())
                .description(request.getDescription())
                .build();

        // 4. 저장 후 ID 반환
        Program saved = programRepository.save(program);
        return saved.getId();
    }

    // 프로그램 상세 조회
    public AdminProgramDetailResponse getProgramDetail(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROGRAM));

        return AdminProgramDetailResponse.builder()
                .id(program.getId())
                .title(program.getTitle())

                // region, facility
                .regionId(program.getRegion().getId())
                .regionName(program.getRegion().getName())
                .facilityId(program.getFacility().getId())
                .facilityName(program.getFacility().getName())

                // 기본 필드
                .category(program.getCategory())
                .target(program.getTarget())
                .instructorName(program.getInstructorName())
                .status(program.getStatus().name())

                // 날짜 원본
                .usageStartDate(program.getUsageStartDate())
                .usageEndDate(program.getUsageEndDate())
                .registrationStartDate(program.getRegistrationStartDate())
                .registrationEndDate(program.getRegistrationEndDate())
                .cancelEndDate(program.getCancelEndDate())

                // 시간 원본
                .classStartTime(program.getClassStartTime())
                .classEndTime(program.getClassEndTime())

                // 상세정보 보기용 포맷
                .usagePeriod(formatDateRange(program.getUsageStartDate(), program.getUsageEndDate()))
                .classTime(formatTimeRange(program.getClassStartTime(), program.getClassEndTime()))
                .registrationPeriod(formatDateRange(program.getRegistrationStartDate(), program.getRegistrationEndDate()))

                // 가격/기타
                .inPrice(program.getInPrice())
                .outPrice(program.getOutPrice())
                .capacity(program.getCapacity())
                .contact(program.getContact())
                .imageUrl(program.getImageUrl())
                .description(program.getDescription())
                .build();
    }


    // 프로그램 수정
    @Transactional
    public Long patchProgram(Long programId, AdminProgramUpdateRequest request, HttpServletRequest httpRequest) {
        User admin = adminAuthHelper.getAdminFromRequest(httpRequest);

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROGRAM));

        // 값 있는 것만 덮어쓰기
        if (request.getTitle() != null) program.setTitle(request.getTitle());
        if (request.getCategory() != null) program.setCategory(request.getCategory());
        if (request.getTarget() != null) program.setTarget(request.getTarget());
        if (request.getInstructorName() != null) program.setInstructorName(request.getInstructorName());
        if (request.getStatus() != null)
            program.setStatus("CLOSED".equalsIgnoreCase(request.getStatus()) ? Program.Status.CLOSED : Program.Status.OPEN);
        if (request.getUsageStartDate() != null) program.setUsageStartDate(request.getUsageStartDate());
        if (request.getUsageEndDate() != null) program.setUsageEndDate(request.getUsageEndDate());
        if (request.getClassStartTime() != null) program.setClassStartTime(request.getClassStartTime());
        if (request.getClassEndTime() != null) program.setClassEndTime(request.getClassEndTime());
        if (request.getRegistrationStartDate() != null) program.setRegistrationStartDate(request.getRegistrationStartDate());
        if (request.getRegistrationEndDate() != null) program.setRegistrationEndDate(request.getRegistrationEndDate());
        if (request.getCancelEndDate() != null) program.setCancelEndDate(request.getCancelEndDate());
        if (request.getInPrice() != null) program.setInPrice(request.getInPrice());
        if (request.getOutPrice() != null) program.setOutPrice(request.getOutPrice());
        if (request.getCapacity() != null) program.setCapacity(request.getCapacity());
        if (request.getContact() != null) program.setContact(request.getContact());
        if (request.getImageUrl() != null) program.setImageUrl(request.getImageUrl());
        if (request.getDescription() != null) program.setDescription(request.getDescription());

        if (request.getFacilityId() != null) {
            Facility facility = facilityRepository.findById(request.getFacilityId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

            if (!facility.getRegion().getId().equals(program.getRegion().getId())) {
                throw new BusinessException(ErrorCode.FACILITY_REGION_MISMATCH);
            }

            program.setFacility(facility);
        }

        return program.getId();
    }

    // 프로그램 삭제
    @Transactional
    public MessageResponse deleteProgram(Long programId, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROGRAM));

        programRepository.delete(program);

        return new MessageResponse("프로그램이 삭제되었습니다.");
    }

    // 시간 포매팅
    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
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