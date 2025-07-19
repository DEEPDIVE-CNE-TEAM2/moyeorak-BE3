package com.example.moyeorak.service;

import com.example.moyeorak.dto.EnrollmentRequest;
import com.example.moyeorak.dto.EnrollmentResponse;
import com.example.moyeorak.entity.Enrollment;
import com.example.moyeorak.entity.Program;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.EnrollmentRepository;
import com.example.moyeorak.repository.ProgramRepository;
import com.example.moyeorak.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;

    // ✅ 수강 신청
    @Transactional
    public EnrollmentResponse enrollByEmail(String email, EnrollmentRequest request) {
        log.info("[ENROLL] 수강 신청 요청 by {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        String[] dateParts = request.getUsagePeriod().split(" ~ ");
        String[] timeParts = request.getClassTime().split(" ~ ");

        if (dateParts.length != 2 || timeParts.length != 2) {
            throw new IllegalArgumentException("기간 또는 시간이 올바르지 않습니다.");
        }

        LocalDate usageStartDate = LocalDate.parse(dateParts[0].trim());
        LocalDate usageEndDate = LocalDate.parse(dateParts[1].trim());
        LocalTime classStartTime = LocalTime.parse(timeParts[0].trim());
        LocalTime classEndTime = LocalTime.parse(timeParts[1].trim());

        Program program = programRepository
                .findByTitleAndFacility_LocationAndUsageStartDateAndUsageEndDateAndClassStartTimeAndClassEndTime(
                        request.getProgramTitle(),
                        request.getLocation(),
                        usageStartDate,
                        usageEndDate,
                        classStartTime,
                        classEndTime
                ).orElseThrow(() -> new IllegalArgumentException("프로그램 정보를 찾을 수 없습니다."));

        if (enrollmentRepository.existsByUserIdAndProgramId(user.getId(), program.getId())) {
            throw new IllegalArgumentException("이미 신청한 프로그램입니다.");
        }

        boolean inRegion = isInRegion(user, program);
        int paidAmount = inRegion ? program.getInPrice() : program.getOutPrice();

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .program(program)
                .region(program.getRegion())
                .status(Enrollment.Status.ENROLLED)
                .paidAmount(paidAmount)
                .classStartTime(program.getClassStartTime())
                .classEndTime(program.getClassEndTime())
                .build();

        // ✅ 저장 후 반환
        Enrollment saved = enrollmentRepository.save(enrollment);
        return toResponse(saved, user);
    }

    public List<EnrollmentResponse> getMyEnrollments(Long userId) {
        log.info("[GET] 사용자 수강 목록 조회 - userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        return enrollmentRepository.findByUserId(userId).stream()
                .map(e -> toResponse(e, user))
                .toList();
    }

    public List<EnrollmentResponse> getAllEnrollments() {
        log.info("[GET] 전체 수강 목록 조회");
        return enrollmentRepository.findAll().stream()
                .map(e -> toResponse(e, e.getUser()))
                .toList();
    }

    public List<EnrollmentResponse> getEnrollmentsByProgram(Long programId) {
        log.info("[GET] 특정 프로그램 수강자 조회 - programId: {}", programId);
        return enrollmentRepository.findAll().stream()
                .filter(e -> e.getProgram().getId().equals(programId))
                .map(e -> toResponse(e, e.getUser()))
                .toList();
    }

    @Transactional
    public void cancelEnrollmentByUser(Long id, Long userId) {
        log.info("[CANCEL] 사용자 수강 취소 요청 - enrollmentId: {}, userId: {}", id, userId);

        Enrollment enrollment = getEnrollment(id);

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인만 수강 신청을 취소할 수 있습니다.");
        }

        if (LocalDate.now().isAfter(enrollment.getProgram().getCancelEndDate())) {
            throw new IllegalStateException("취소 마감일이 지나 취소할 수 없습니다.");
        }

        enrollment.setStatus(Enrollment.Status.CANCELLED);
    }

    @Transactional
    public void cancelEnrollmentByAdmin(Long id, String reason) {
        log.info("[ADMIN CANCEL] 관리자 수강 취소 요청 - id: {}, reason: {}", id, reason);

        Enrollment enrollment = getEnrollment(id);
        enrollment.setStatus(Enrollment.Status.CANCELLED);
        enrollment.setCancelReason(reason);
    }

    private Enrollment getEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수강 신청이 존재하지 않습니다."));
    }

    // ✅ 관내 여부 판단용 헬퍼
    private boolean isInRegion(User user, Program program) {
        Long userRegionId = Optional.ofNullable(user.getRegion()).map(r -> r.getId()).orElse(null);
        Long programRegionId = Optional.ofNullable(program.getRegion()).map(r -> r.getId()).orElse(null);
        boolean inRegion = Objects.equals(userRegionId, programRegionId);

        log.debug("관내 여부 판단: userRegionId = {}, programRegionId = {}, inRegion = {}",
                userRegionId, programRegionId, inRegion);
        return inRegion;
    }

    // ✅ 사용자 포함된 Enrollment → DTO 변환
    private EnrollmentResponse toResponse(Enrollment e, User user) {
        Program program = e.getProgram();
        boolean inRegion = isInRegion(user, program);
        int appliedPrice = inRegion ? program.getInPrice() : program.getOutPrice();
        String regionLabel = inRegion ? "관내" : "관외";

        return EnrollmentResponse.builder()
                .id(e.getId())
                .userId(user.getId())
                .programId(program.getId())
                .regionId(program.getRegion().getId())
                .enrolledAt(e.getEnrolledAt())
                .status(e.getStatus().name().toLowerCase())
                .paidAmount(appliedPrice)
                .cancelReason(e.getCancelReason())
                .classStartTime(program.getClassStartTime())
                .classEndTime(program.getClassEndTime())
                .instructorName(program.getInstructorName())
                .regionLabel(regionLabel)
                .build();
    }
}
