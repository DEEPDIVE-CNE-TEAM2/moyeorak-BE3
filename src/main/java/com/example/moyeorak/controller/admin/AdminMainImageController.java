package com.example.moyeorak.controller.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.service.admin.AdminMainImageService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/main-img")
@RequiredArgsConstructor
public class AdminMainImageController {

    private final AdminMainImageService adminMainImageService;

    @Operation(summary = "홍보물 생성")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminMainImageResponse> createMainImage(
            @Valid @RequestBody AdminMainImageCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        AdminMainImageResponse response = adminMainImageService.createMainImage(request, httpRequest);
        log.info("홍보물 생성 완료: id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홍보물 리스트 조회")
    @GetMapping
    public ResponseEntity<List<AdminMainImageResponse>> getMainImages(HttpServletRequest httpRequest) {
        log.info("홍보물 리스트 조회 요청");
        List<AdminMainImageResponse> list = adminMainImageService.getMainImages(httpRequest);
        log.info("홍보물 리스트 조회 완료: {}건", list.size());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "홍보물 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMainImage(@PathVariable Long id, HttpServletRequest httpRequest) {
        log.info("홍보물 삭제 요청: id={}", id);
        adminMainImageService.deleteById(id, httpRequest);
        log.info("홍보물 삭제 완료: id={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "홍보물 순서 및 표시여부 전체 수정")
    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateMainImages(
            @RequestBody List<AdminMainImageUpdateRequest> requestList,
            HttpServletRequest httpRequest
    ) {
        log.info("홍보물 순서/표시여부 전체 수정 요청: {}건", requestList.size());
        adminMainImageService.updateMainImages(requestList, httpRequest);
        log.info("홍보물 순서/표시여부 전체 수정 완료");
        return ResponseEntity.ok().build();
    }

    // -------------------------------
    // Presigned URL 발급 (권장) : POST + JSON Body
    // -------------------------------
    @Operation(summary = "S3 업로드용 Presigned URL 발급 (권장: POST + JSON Body)")
    @PostMapping(value = "/presigned-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createPresignedUrl(
            @Valid @RequestBody PresignUrlRequest body,
            HttpServletRequest httpRequest
    ) {
        String filename = body.filename();
        String contentType = body.contentType(); // optional

        if (isBlank(filename)) {
            return ResponseEntity.badRequest().body("filename은 비어 있을 수 없습니다.");
        }

        String url = adminMainImageService.createPresignedPutUrl(filename, contentType, httpRequest);
        return ResponseEntity.ok(url);
    }

    // -------------------------------
    // Presigned URL 발급 (호환) : GET + query
    // - 기존 클라이언트가 보내는 filetype도 지원
    // - contentType이 있으면 contentType 우선
    // -------------------------------
    @Operation(summary = "S3 업로드용 Presigned URL 발급 (호환: GET + query, contentType/filetype 선택)")
    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(
            @RequestParam String filename,
            @RequestParam(name = "contentType", required = false) String contentType,
            @RequestParam(name = "filetype", required = false) String filetype,
            HttpServletRequest httpRequest
    ) {
        String ct = !isBlank(contentType) ? contentType : filetype;

        if (isBlank(filename)) {
            return ResponseEntity.badRequest().body("필수 파라미터 누락: filename");
        }

        String url = adminMainImageService.createPresignedPutUrl(filename, ct, httpRequest);
        return ResponseEntity.ok(url);
    }

    // ------------------------------------
    // 내부 유틸
    // ------------------------------------
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * POST /presigned-url 요청용 DTO
     * contentType은 선택값입니다. (서명에는 사용하지 않음)
     */
    public record PresignUrlRequest(
            @NotBlank String filename,
            String contentType
    ) {}
}
