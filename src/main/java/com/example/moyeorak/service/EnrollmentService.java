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

    @Transactional
    public EnrollmentResponse enrollByEmail(String email, EnrollmentRequest request) {
        log.info("[ENROLL] 수강 신청 요청 by {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 없습니다."));
        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("프로그램 정보가 없습니다."));

        boolean inRegion = isInRegion(user, program);
        int paidAmount = inRegion ? program.getInPrice() : program.getOutPrice();

        Optional<Enrollment> existingOpt = enrollmentRepository.findByUserIdAndProgramId(user.getId(), program.getId());

        if (existingOpt.isPresent()) {
            Enrollment existing = existingOpt.get();
            if (existing.getStatus() != Enrollment.Status.CANCELLED) {
                throw new IllegalArgumentException("이미 신청한 프로그램입니다.");
            }

            // ✅ 삭제 대신 재사용 방식 (unique 제약 조건 회피 + 기록 유지)
            existing.setStatus(Enrollment.Status.ENROLLED);
            existing.setPaidAmount(paidAmount);
            existing.setClassStartTime(program.getClassStartTime());
            existing.setClassEndTime(program.getClassEndTime());
            existing.setRegion(program.getRegion());
            existing.setCancelReason("");  // 취소 사유 초기화 (nullable 방지)

            Enrollment saved = enrollmentRepository.save(existing);
            return toResponse(saved, user);
        }

        Enrollment newEnrollment = Enrollment.builder()
                .user(user)
                .program(program)
                .region(program.getRegion())
                .status(Enrollment.Status.ENROLLED)
                .paidAmount(paidAmount)
                .classStartTime(program.getClassStartTime())
                .classEndTime(program.getClassEndTime())
                .build();

        Enrollment saved = enrollmentRepository.save(newEnrollment);
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

    private boolean isInRegion(User user, Program program) {
        Long userRegionId = Optional.ofNullable(user.getRegion()).map(r -> r.getId()).orElse(null);
        Long programRegionId = Optional.ofNullable(program.getRegion()).map(r -> r.getId()).orElse(null);
        boolean inRegion = Objects.equals(userRegionId, programRegionId);

        log.debug("관내 여부 판단: userRegionId = {}, programRegionId = {}, inRegion = {}",
                userRegionId, programRegionId, inRegion);
        return inRegion;
    }

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
                .cancelEndDate(program.getCancelEndDate())
                .build();
    }
}
