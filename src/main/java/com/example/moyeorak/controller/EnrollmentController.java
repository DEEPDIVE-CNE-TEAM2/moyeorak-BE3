package com.example.moyeorak.controller;

import com.example.moyeorak.dto.EnrollmentCancelRequest;
import com.example.moyeorak.dto.EnrollmentRequest;
import com.example.moyeorak.dto.EnrollmentResponse;
import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.security.CustomUserDetails;
import com.example.moyeorak.service.EnrollmentService;
import com.example.moyeorak.service.AsyncEnrollmentService;
import com.example.moyeorak.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final AsyncEnrollmentService asyncEnrollmentService;
    private final ProgramService programService;

    // ✅ 수강 신청 (동기/비동기 분기)
//    @PostMapping
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<EnrollmentResponse> enroll(
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @Valid @RequestBody EnrollmentRequest request
//    ) {
//        String email = userDetails.getEmail();
//        log.info("[POST] 수강 신청 요청 by {}", email);
//        return ResponseEntity.ok(enrollmentService.enrollByEmail(email, request));
//    }
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> enroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EnrollmentRequest request
    ) {
        String email = userDetails.getEmail();
        Long programId = request.getProgramId();
        log.info("[POST] 수강 신청 요청 - email: {}, programId: {}", email, programId);

        boolean async = programService.isAsyncPeriod(programId);
        if (async) {
            log.info("[ASYNC] 비동기 수강신청 처리 시작");
            asyncEnrollmentService.sendEnrollment(request, email);
            return ResponseEntity.accepted()
                    .body(new MessageResponse("비동기 수강신청 요청이 접수되었습니다."));
        } else {
            log.info("[SYNC] 동기 수강신청 처리 시작");
            return ResponseEntity.ok(enrollmentService.enrollByEmail(email, request));
        }
    }
    // ✅ 내 수강 목록 조회
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[GET] 내 수강 목록 조회 - userId: {}", userId);
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(userId));
    }

    // ✅ 사용자 수강 취소
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> cancelEnrollmentByUser(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[DELETE] 수강 취소 요청 - enrollmentId: {}, userId: {}", id, userId);
        enrollmentService.cancelEnrollmentByUser(id, userId);
        return ResponseEntity.ok(new MessageResponse("수강 신청이 취소되었습니다."));
    }

    // ✅ 관리자 수강 취소
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> cancelEnrollmentByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody EnrollmentCancelRequest request
    ) {
        log.info("[ADMIN CANCEL] 관리자 수강 취소 요청 - enrollmentId: {}, 사유: {}", id, request.getCancelReason());
        enrollmentService.cancelEnrollmentByAdmin(id, request.getCancelReason());
        return ResponseEntity.ok(new MessageResponse("수강 신청이 관리자에 의해 취소되었습니다."));
    }

    // ✅ 전체 수강 목록 조회 (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        log.info("[ADMIN] 전체 수강 목록 조회");
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    // ✅ 특정 프로그램 수강자 목록 조회 (ADMIN)
    @GetMapping("/program/{programId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getByProgram(@PathVariable Long programId) {
        log.info("[ADMIN] 특정 프로그램 수강자 목록 조회 - programId: {}", programId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByProgram(programId));
    }

//    @PostMapping("/async")
//    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
//    public ResponseEntity<MessageResponse> asyncEnrollment(
//            @Valid @RequestBody EnrollmentRequest request,
//            @AuthenticationPrincipal CustomUserDetails userDetails
//    ) {
//        String email = userDetails.getEmail();
//        log.info("[ASYNC POST] 수강 신청 요청 비동기 처리 by {}", email);
//
//        asyncEnrollmentService.sendEnrollment(request, email);
//
//        return ResponseEntity.accepted().body(new MessageResponse("수강 신청 요청이 접수되었습니다."));
//    }

}
