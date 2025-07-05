package com.example.moyeorak.service;

import com.example.moyeorak.dto.EnrollmentRequest;
import com.example.moyeorak.dto.EnrollmentResponse;
import com.example.moyeorak.entity.*;
import com.example.moyeorak.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final RegionRepository regionRepository;

    @Transactional
    public EnrollmentResponse enroll(Long userId, EnrollmentRequest request) {
        log.info("[CREATE] 수강 신청 요청: userId={}, programId={}", userId, request.getProgramId());

        if (enrollmentRepository.existsByUserIdAndProgramId(userId, request.getProgramId())) {
            throw new IllegalArgumentException("이미 신청한 프로그램입니다.");
        }

        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("프로그램 정보가 존재하지 않습니다."));

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역 정보가 존재하지 않습니다."));

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .program(program)
                .region(region)
                .paidAmount(request.getPaidAmount())
                .status(Enrollment.Status.ENROLLED)
                .build();

        return toResponse(enrollmentRepository.save(enrollment));
    }

    public List<EnrollmentResponse> getMyEnrollments(Long userId) {
        return enrollmentRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
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
                .build();
    }

    @Transactional
    public void cancelEnrollmentByUser(Long id, Long userId) {
        Enrollment enrollment = getEnrollment(id);

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인만 수강 신청을 취소할 수 있습니다.");
        }

        LocalDate today = LocalDate.now();
        if (today.isAfter(enrollment.getProgram().getCancelEndDate())) {
            throw new IllegalStateException("취소 마감일이 지나 취소할 수 없습니다.");
        }

        enrollment.setStatus(Enrollment.Status.CANCELLED);
    }

    private Enrollment getEnrollment(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수강 신청이 존재하지 않습니다."));
    }

    @Transactional
    public void cancelEnrollmentByAdmin(Long id, String reason) {
        Enrollment enrollment = getEnrollment(id);

        enrollment.setStatus(Enrollment.Status.CANCELLED);
        enrollment.setCancelReason(reason);
    }
}