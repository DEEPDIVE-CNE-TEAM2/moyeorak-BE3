package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.service.RentalApplicationService;
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

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<RentalApplicationResponse> createRentalApplication(
            @Valid @RequestBody RentalApplicationRequest request) {
        log.info("[POST] 대관 신청 요청: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rentalApplicationService.createRentalApplication(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal(expression = "id") Long userId) {
        log.info("[GET] 내 신청 목록 - userId: {}", userId);
        return ResponseEntity.ok(rentalApplicationService.getUserApplications(userId));
    }

    @DeleteMapping("/{applicationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageResponse> cancelRentalApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal(expression = "id") Long userId) {
        log.info("[DELETE] 신청 취소 요청 - ID: {}", applicationId);
        String result = rentalApplicationService.deleteRentalApplication(applicationId, userId);
        return ResponseEntity.ok(new MessageResponse(result));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(rentalApplicationService.getAllApplications());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(rentalApplicationService.getApplicationsByUser(userId));
    }

    @GetMapping("/rental/{rentalId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RentalApplicationResponse>> getByRental(@PathVariable Long rentalId) {
        return ResponseEntity.ok(rentalApplicationService.getApplicationsByRental(rentalId));
    }

    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalApplicationResponse> updateStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody RentalApplicationStatusUpdateRequest request) {
        log.info("[PUT] 상태 변경 요청 - ID: {}, status: {}", applicationId, request.getStatus());
        return ResponseEntity.ok(
                rentalApplicationService.updateApplicationStatus(applicationId, request.getStatus()));
    }
}
