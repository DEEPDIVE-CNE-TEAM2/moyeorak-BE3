package com.example.moyeorak.controller;

import com.example.moyeorak.dto.RentalRequest;
import com.example.moyeorak.dto.RentalResponse;
import com.example.moyeorak.dto.MessageResponse;
import com.example.moyeorak.service.RentalService;
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalResponse> createRental(@RequestBody @Valid RentalRequest request) {
        log.info("[POST] 대관 등록 요청: {}", request);
        RentalResponse created = rentalService.createRental(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RentalResponse>> getAllRentals() {
        log.info("[GET] 전체 대관 목록 조회");
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponse> getRentalById(@PathVariable Long id) {
        log.info("[GET] 대관 상세 조회 요청 - ID: {}", id);
        return ResponseEntity.ok(rentalService.getRentalById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalResponse> patchRental(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        log.info("[PATCH] 대관 수정 요청 - ID: {}, 업데이트 필드: {}", id, updates);
        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RentalResponse updated = rentalService.partialUpdateRental(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteRental(@PathVariable Long id) {
        log.info("[DELETE] 대관 삭제 요청 - ID: {}", id);
        rentalService.deleteRental(id);
        return ResponseEntity.ok(new MessageResponse("삭제되었습니다."));
    }
}
