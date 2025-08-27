package com.example.moyeorak.controller;

import com.example.moyeorak.dto.NoticeDto;
import com.example.moyeorak.dto.NoticeRequest;
import com.example.moyeorak.dto.NoticeResponse;
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
@RequestMapping("/api/content/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ✅ 공지 생성
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("[POST] 공지 생성 요청 by userId={}", userId);
        NoticeDto created = noticeService.create(userId, request);
        return ResponseEntity.ok(NoticeResponse.from(created));
    }

    // ✅ 공지 단건 조회 + 조회수 증가
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long id) {
        log.info("[GET] 공지 단건 조회 + 조회수 증가 - ID: {}", id);
        NoticeDto notice = noticeService.getNoticeAndIncreaseViewCount(id);
        return ResponseEntity.ok(NoticeResponse.from(notice));
    }

    // ✅ 지역별 공지 목록 조회
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<NoticeDto>> getByRegion(@PathVariable Long regionId) {
        log.info("[GET] 지역별 공지 조회 - Region ID: {}", regionId);
        return ResponseEntity.ok(noticeService.getNoticesByRegionId(regionId));
    }

    // ✅ 지역별 공지 단건 조회 (조회수 증가 없음)
    @GetMapping("/region/{regionId}/{noticeId}")
    public ResponseEntity<NoticeDto> getNoticeByRegion(
            @PathVariable Long regionId,
            @PathVariable Long noticeId
    ) {
        log.info("[GET] 지역별 공지 단건 조회 - regionId: {}, noticeId: {}", regionId, noticeId);
        return ResponseEntity.ok(noticeService.getNoticeByRegion(noticeId, regionId));
    }

    // ✅ 공지 수정
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

    // ✅ 공지 삭제
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
