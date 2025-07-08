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

    // ✅ 공지 생성
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(@RequestBody @Valid NoticeRequest request,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        User author = userRepository.findById((long) Math.toIntExact(userDetails.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));

        NoticeDto created = noticeService.create(author, request);
        return ResponseEntity.ok(NoticeResponse.from(created));
    }

    // ✅ 공지 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDto> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNotice(id));
    }

    // ✅ 지역별 조회
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<NoticeDto>> getByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(noticeService.getNoticesByRegionId(regionId));
    }

    // ✅ 공지 수정
    @PatchMapping("/{id}")
    public ResponseEntity<NoticeDto> updateNotice(@PathVariable Long id,
                                                  @RequestBody @Valid NoticeRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {
        log.info(">> PATCH 요청: userId={}, noticeId={}", userDetails.getUserId(), id);  // 👈 로그 찍기
        NoticeDto updated = noticeService.updateNotice(id, userDetails.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    // ✅ 공지 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {
        noticeService.deleteNotice(id, userDetails.getUserId());
        return ResponseEntity.noContent().build();
    }
}
