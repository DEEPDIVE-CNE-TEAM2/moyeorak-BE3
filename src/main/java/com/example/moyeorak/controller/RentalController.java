package com.example.moyeorak.controller;

import com.example.moyeorak.dto.*;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.service.RentalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final JwtProvider jwtProvider;

    // ✅ 대관 등록 (전체 데이터 + 지역명 + 담당자 이메일 포함)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalCreateResponse> createRental(
            @RequestBody @Valid RentalRequest request,
            HttpServletRequest servletRequest
    ) {
        String token = servletRequest.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String email = jwtProvider.getEmail(token.substring(7));
        log.info("[POST] 대관 등록 요청 by 관리자: {}", email);

        RentalCreateResponse created = rentalService.createRental(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ✅ 대관 목록 조회 (이미지, 장소명, 주소, 운영시간, 정원)
    @GetMapping
    public ResponseEntity<List<RentalListResponse>> getAllRentals() {
        log.info("[GET] 전체 대관 목록 조회");
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    // ✅ 대관 상세 조회 (종목, 장소, 주소, 운영시간, 접수기간, 취소기한, 정원, 문의)
    @GetMapping("/{id}")
    public ResponseEntity<RentalDetailResponse> getRentalById(@PathVariable Long id) {
        log.info("[GET] 대관 상세 조회 요청 - ID: {}", id);
        return ResponseEntity.ok(rentalService.getRentalById(id));
    }

    // ✅ 대관 수정 (관리자만 가능) - 응답은 생성 형식
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalCreateResponse> patchRental(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        log.info("[PATCH] 대관 수정 요청 - ID: {}, 업데이트 필드: {}", id, updates);
        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RentalCreateResponse updated = rentalService.partialUpdateRental(id, updates);
        return ResponseEntity.ok(updated);
    }

    // ✅ 대관 삭제
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteRental(@PathVariable Long id) {
        log.info("[DELETE] 대관 삭제 요청 - ID: {}", id);
        rentalService.deleteRental(id);
        return ResponseEntity.ok(new MessageResponse("삭제되었습니다."));
    }
}
