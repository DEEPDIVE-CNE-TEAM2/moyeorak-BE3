package com.example.moyeorak.controller;

import com.example.moyeorak.dto.NoticeDto;
import com.example.moyeorak.dto.NoticeRequest;
import com.example.moyeorak.dto.NoticeResponse;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.UserRepository;
import com.example.moyeorak.security.CustomUserDetails;
import com.example.moyeorak.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;
    private final UserRepository userRepository;

    // 공지 생성
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[POST] 공지 생성 요청 by userId={}", userId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));
        NoticeDto created = noticeService.create(author, request);
        return ResponseEntity.ok(NoticeResponse.from(created));
    }

    // 공지 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDto> getNotice(@PathVariable Long id) {
        log.info("[GET] 공지 단건 조회 - ID: {}", id);
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    // 지역별 공지 목록 조회
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<NoticeDto>> getByRegion(@PathVariable Long regionId) {
        log.info("[GET] 지역별 공지 조회 - Region ID: {}", regionId);
        return ResponseEntity.ok(noticeService.getNoticesByRegionId(regionId));
    }

    // 공지 수정
    @PatchMapping("/{id}")
    public ResponseEntity<NoticeDto> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) throws AccessDeniedException {
        log.info("[PATCH] 공지 수정 요청 - noticeId: {}, userId: {}", id, userId);
        NoticeDto updated = noticeService.updateNotice(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    // 공지 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) throws AccessDeniedException {
        log.info("[DELETE] 공지 삭제 요청 - noticeId: {}, userId: {}", id, userId);
        noticeService.deleteNotice(id, userId);
        return ResponseEntity.noContent().build();
    }
}
