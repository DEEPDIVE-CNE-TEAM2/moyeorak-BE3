package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminNoticeListResponse;
import com.example.moyeorak.dto.admin.AdminNoticeRequest;
import com.example.moyeorak.dto.admin.AdminNoticeResponse;
import com.example.moyeorak.service.admin.AdminNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    // 공지사항 생성
    @Operation(
            summary = "공지사항 생성"
    )
    @PostMapping
    public ResponseEntity<AdminNoticeResponse> createNotice(
            @Valid @RequestBody AdminNoticeRequest request,
            HttpServletRequest httpRequest
    ) {

        AdminNoticeResponse response = adminNoticeService.createNotice(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공지사항 조회")
    @GetMapping
    public List<AdminNoticeListResponse> getNoticeList(
            HttpServletRequest request
    ) {
        return adminNoticeService.getNoticeList(request);
    }
}