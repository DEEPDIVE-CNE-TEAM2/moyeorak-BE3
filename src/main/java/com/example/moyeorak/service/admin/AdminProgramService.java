package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.dto.admin.AdminProgramCreateRequest;
import com.example.moyeorak.dto.admin.AdminProgramDetailResponse;
import com.example.moyeorak.dto.admin.AdminProgramListResponse;
import com.example.moyeorak.dto.admin.AdminProgramUpdateRequest;
import com.example.moyeorak.entity.Facility;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.FacilityRepository;
import com.example.moyeorak.repository.ProgramRepository;
import com.example.moyeorak.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProgramService {

    private final ProgramRepository programRepository;
    private final RegionRepository regionRepository;
    private final FacilityRepository facilityRepository;
    // private final EnrollmentRepository enrollmentRepository; // 분리 서비스 연동 시 사용

    // ───────────────────────── 목록 ─────────────────────────

    /**
     * 특정 지역의 프로그램 목록(제목 검색 포함)
     * @param userId   JWT에서 추출한 관리자 사용자 ID
     * @param regionId 조회할 지역 ID
     * @param title    부분 검색어 (nullable)
     */
    @Transactional(readOnly = true)
    public List<AdminProgramListResponse> getProgramsByRegionAndTitle(Long userId, Long regionId, String title) {
        assertRegionManagerByRegionId(userId, regionId);

        List<Program> programs = (title == null || title.trim().isEmpty())
                ? programRepository.findByRegionId(regionId)
                : programRepository.findByRegionIdAndTitleContainingIgnoreCase(regionId, title.trim());

        return programs.stream().map(this::toAdminListDto).toList();
    }

    /** Admin 목록용 DTO 변환 (facilityId로 이름 조회) */
    private AdminProgramListResponse toAdminListDto(Program program) {
        int currentEnrollment = 0; // TODO: enrollmentRepository.countByProgramId(program.getId())
        String facilityName = facilityRepository.findById(program.getFacilityId())
                .map(Facility::getName)
                .orElse("(시설 정보 없음)");

        return AdminProgramListResponse.builder()
                .id(program.getId())
                .title(program.getTitle())
                .facilityName(facilityName)
                .usagePeriod(formatDateRange(program.getUsageStartDate(), program.getUsageEndDate()))
                .capacity(program.getCapacity())
                .currentEnrollment(currentEnrollment)
                .progressStatus(getProgressStatus(program.getUsageStartDate(), program.getUsageEndDate()))
                .build();
    }

    // ───────────────────────── 생성 ─────────────────────────

    /**
     * 프로그램 생성
     * @param request 생성 요청(지역/시설/기타 정보)
     * @param userId  관리자 사용자 ID
     * @return 생성된 프로그램 ID
     */
    @Transactional
    public Long createProgram(AdminProgramCreateRequest request, Long userId) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        assertRegionManager(userId, region);

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

        // 시설이 같은 지역에 속하는지 FK로 검증
        if (!facility.getRegionId().equals(region.getId())) {
            throw new BusinessException(ErrorCode.FACILITY_REGION_MISMATCH);
        }

        Program program = Program.builder()
                .title(request.getTitle())
                .regionId(region.getId())          // ✅ MAS: 엔티티 대신 FK 저장
                .facilityId(facility.getId())      // ✅ MAS: 엔티티 대신 FK 저장
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

        Program saved = programRepository.save(program);
        return saved.getId();
    }

    // ───────────────────────── 상세 ─────────────────────────

    @Transactional(readOnly = true)
    public AdminProgramDetailResponse getProgramDetail(Long userId, Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROGRAM));

        // 지역 권한 체크 (program.regionId 기준)
        assertRegionManagerByRegionId(userId, program.getRegionId());

        // 응답에 지역/시설 이름이 필요하므로 각각 조회
        Region region = regionRepository.findById(program.getRegionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));
        Facility facility = facilityRepository.findById(program.getFacilityId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_FACILITY));

        return AdminProgramDetailResponse.builder()
                .id(program.getId())
                .title(program.getTitle())

                .regionId(region.getId())
                .regionName(region.getName())
                .facilityId(facility.getId())
                .facilityName(facility.getName())

                .category(program.getCategory())
                .target(program.getTarget())
                .instructorName(program.getInstructorName())
                .status(program.getStatus().name())

                .usageStartDate(program.getUsageStartDate())
                .usageEndDate(program.getUsageEndDate())
                .registrationStartDate(program.getRegistrationStartDate())
                .registrationEndDate(program.getRegistrationEndDate())
                .cancelEndDate(program.getCancelEndDate())

                .classStartTime(program.getClassStartTime())
                .classEndTime(program.getClassEndTime())

                .usagePeriod(formatDateRange(program.getUsageStartDate(), program.getUsageEndDate()))
                .classTime(formatTimeRange(program.getClassStartTime(), program.getClassEndTime()))
                .registrationPeriod(formatDateRange(program.getRegistrationStartDate(), program.getRegistrationEndDate()))

                .inPrice(program.getInPrice())
                .outPrice(program.getOutPrice())
                .capacity(program.getCapacity())
                .contact(program.getContact())
                .imageUrl(program.getImageUrl())
                .description(program.getDescription())
                .build();
    }

    // ───────────────────────── 수정 ─────────────────────────

    @Transactional
    public Long patchProgram(Long programId, AdminProgramUpdateRequest request, Long userId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROGRAM));

        assertRegionManagerByRegionId(userId, program.getRegionId());

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

            // 기존 프로그램의 지역 FK와 새 시설의 지역 FK 비교
            if (!facility.getRegionId().equals(program.getRegionId())) {
                throw new BusinessException(ErrorCode.FACILITY_REGION_MISMATCH);
            }
            program.setFacilityId(facility.getId()); // ✅ MAS: FK 업데이트
        }

        return program.getId();
    }

    // ───────────────────────── 삭제 ─────────────────────────

    @Transactional
    public MessageResponse deleteProgram(Long programId, Long userId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROGRAM));

        assertRegionManagerByRegionId(userId, program.getRegionId());

        programRepository.delete(program);
        return new MessageResponse("프로그램이 삭제되었습니다.");
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
            // TODO: ErrorCode에 UNAUTHORIZED_PROGRAM_ACCESS 추가 후 교체
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }
    }

    private String formatTimeRange(LocalTime start, LocalTime end) {
        return start + " ~ " + end;
    }

    private String formatDateRange(LocalDate start, LocalDate end) {
        return start + " ~ " + end;
    }

    /** 오늘 기준으로 수업 상태 판단: 수업 예정 / 진행중 / 수업 종료 */
    private String getProgressStatus(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(start)) return "수업 예정";
        else if (!today.isAfter(end)) return "진행중";
        else return "수업 종료";
    }
}
