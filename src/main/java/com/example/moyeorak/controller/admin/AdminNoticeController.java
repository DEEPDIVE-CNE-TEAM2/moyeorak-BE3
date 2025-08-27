package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminNoticeListResponse;
import com.example.moyeorak.dto.admin.AdminNoticeRequest;
import com.example.moyeorak.dto.admin.AdminNoticeResponse;
import com.example.moyeorak.service.admin.AdminNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    /** 공지사항 생성 (MAS: userId + regionId로 권한 검증) */
    @Operation(summary = "공지사항 생성")
    @PostMapping
    public ResponseEntity<AdminNoticeResponse> createNotice(
            @Valid @RequestBody AdminNoticeRequest request,
            @RequestParam("regionId") Long regionId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("공지사항 생성 요청: title={}, regionId={}, userId={}", request.getTitle(), regionId, userId);
        AdminNoticeResponse response = adminNoticeService.createNotice(request, userId, regionId);
        log.info("공지사항 생성 완료: noticeId={}", response.getId());
        return ResponseEntity.ok(response);
    }

    /** 공지사항 목록 (특정 지역 기준) */
    @Operation(summary = "공지사항 조회")
    @GetMapping
    public ResponseEntity<List<AdminNoticeListResponse>> getNoticeList(
            @RequestParam("regionId") Long regionId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("공지사항 목록 조회 요청: regionId={}, userId={}", regionId, userId);
        List<AdminNoticeListResponse> list = adminNoticeService.getNoticeList(userId, regionId);
        log.info("공지사항 목록 조회 완료: {}건", list.size());
        return ResponseEntity.ok(list);
    }

    /** 공지사항 상세 (공지의 regionId로 권한 검증) */
    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{noticeId}")
    public ResponseEntity<AdminNoticeResponse> getNoticeDetail(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("공지사항 상세 조회 요청: noticeId={}, userId={}", noticeId, userId);
        AdminNoticeResponse response = adminNoticeService.getNoticeDetail(noticeId, userId);
        log.info("공지사항 상세 조회 완료: noticeId={}", noticeId);
        return ResponseEntity.ok(response);
    }

    /** 공지사항 수정 */
    @Operation(summary = "공지사항 수정")
    @PutMapping("/{noticeId}")
    public ResponseEntity<AdminNoticeResponse> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody AdminNoticeRequest request,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("공지사항 수정 요청: noticeId={}, userId={}", noticeId, userId);
        AdminNoticeResponse response = adminNoticeService.updateNotice(noticeId, request, userId);
        log.info("공지사항 수정 완료: noticeId={}", noticeId);
        return ResponseEntity.ok(response);
    }

    /** 공지사항 삭제 */
    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable Long noticeId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        log.info("공지사항 삭제 요청: noticeId={}, userId={}", noticeId, userId);
        adminNoticeService.deleteNotice(noticeId, userId);
        log.info("공지사항 삭제 완료: noticeId={}", noticeId);
        return ResponseEntity.noContent().build();
    }
}
