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

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));

        // 기간, 시간 파싱
        String[] dateParts = request.getUsagePeriod().split(" ~ ");
        String[] timeParts = request.getClassTime().split(" ~ ");  // ✅ 변경된 부분

        if (dateParts.length != 2 || timeParts.length != 2) {
            throw new IllegalArgumentException("기간 또는 시간이 올바르지 않습니다.");
        }

        LocalDate usageStartDate = LocalDate.parse(dateParts[0].trim());
        LocalDate usageEndDate = LocalDate.parse(dateParts[1].trim());
        LocalTime classStartTime = LocalTime.parse(timeParts[0].trim());
        LocalTime classEndTime = LocalTime.parse(timeParts[1].trim());

        // 프로그램 조회
        Program program = programRepository
                .findByTitleAndFacility_LocationAndUsageStartDateAndUsageEndDateAndClassStartTimeAndClassEndTime(
                        request.getProgramTitle(),
                        request.getLocation(),
                        usageStartDate,
                        usageEndDate,
                        classStartTime,
                        classEndTime
                ).orElseThrow(() -> new IllegalArgumentException("프로그램 정보를 찾을 수 없습니다."));

        // 중복 신청 확인
        if (enrollmentRepository.existsByUserIdAndProgramId(user.getId(), program.getId())) {
            throw new IllegalArgumentException("이미 신청한 프로그램입니다.");
        }

        // 수강 신청 생성
        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .program(program)
                .region(program.getRegion())
                .status(Enrollment.Status.ENROLLED)
                .paidAmount(request.getPaidAmount())
                .classStartTime(program.getClassStartTime())   // ✅ 수업 시간 저장
                .classEndTime(program.getClassEndTime())
                .build();

        return toResponse(enrollmentRepository.save(enrollment));
    }

    public List<EnrollmentResponse> getMyEnrollments(Long userId) {
        log.info("[GET] 사용자 수강 목록 조회 - userId: {}", userId);
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EnrollmentResponse> getAllEnrollments() {
        log.info("[GET] 전체 수강 목록 조회");
        return enrollmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EnrollmentResponse> getEnrollmentsByProgram(Long programId) {
        log.info("[GET] 특정 프로그램 수강자 조회 - programId: {}", programId);
        return enrollmentRepository.findAll().stream()
                .filter(e -> e.getProgram().getId().equals(programId))
                .map(this::toResponse)
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

    private EnrollmentResponse toResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .userId(e.getUser().getId())
                .programId(e.getProgram().getId())
                .regionId(e.getRegion().getId())
                .enrolledAt(e.getEnrolledAt())
                .status(e.getStatus().name().toLowerCase())
                .paidAmount(e.getPaidAmount())
                .cancelReason(e.getCancelReason())
                .classStartTime(e.getClassStartTime())     // ✅ 수강신청에 저장된 수업 시간 응답
                .classEndTime(e.getClassEndTime())
                .build();
    }
}
