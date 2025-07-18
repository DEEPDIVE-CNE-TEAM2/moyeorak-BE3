package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.security.CustomUserDetails;
import com.example.moyeorak.service.RentalApplicationService;
import com.example.moyeorak.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rental-applications")
@RequiredArgsConstructor
public class RentalApplicationController {

    private final RentalApplicationService rentalApplicationService;
    private final JwtProvider jwtProvider;
    private final UserService userService;

    // ✅ 대관 신청 생성
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RentalApplicationResponse> createRentalApplication(
            @Valid @RequestBody RentalApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String email = userDetails.getEmail();
        log.info("[POST] 대관 신청 요청 by {}", email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalApplicationService.createRentalApplication(request, email));
    }

    // ✅ 내 대관 신청 목록 조회
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        String email = userDetails.getEmail();
        log.info("[GET] 내 신청 목록 - email: {}, userId: {}", email, userId);
        return ResponseEntity.ok(rentalApplicationService.getApplicationsByUser(userId));
    }

    // ✅ 대관 신청 취소 (사용자 본인)
    @DeleteMapping("/{applicationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> cancelRentalApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("[DELETE] 신청 취소 요청 - ID: {}, by userId: {}", applicationId, userId);
        String result = rentalApplicationService.deleteRentalApplication(applicationId, userId);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    // ✅ 대관 신청 상태 변경 (ADMIN)
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody RentalApplicationStatusUpdateRequest request
    ) {
        log.info("[PUT] 상태 변경 요청 - ID: {}, status: {}", applicationId, request.getStatus());
        return ResponseEntity.ok(
                rentalApplicationService.updateApplicationStatus(applicationId, request.getStatus()));
    }

    // ✅ 관리자 전용 전체 대관 신청 목록
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationAdminResponse>> getAllForAdmin() {
        log.info("[ADMIN] 전체 대관 신청 목록 조회");
        return ResponseEntity.ok(rentalApplicationService.getAllApplicationsForAdmin());
    }

    // 대관신청 AWS SQS 비동기처리 
    @PostMapping("/async")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> asyncRentalApplication(
        @Valid @RequestBody RentalApplicationRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
	String email = userDetails.getEmail();
    	log.info("[ASYNC POST] 대관 신청 요청 비동기 처리 by {}", email);

    	asyncRentalApplicationService.sendRentalApplication(request, email);

    	return ResponseEntity.accepted().body(new MessageResponse("대관 신청 요청이 접수되었습니다."));
    }

}
