package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminMainImageCreateRequest;
import com.example.moyeorak.dto.admin.AdminMainImageResponse;
import com.example.moyeorak.dto.admin.AdminMainImageUpdateRequest;
import com.example.moyeorak.entity.MainImage;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.MainImageRepository;
import com.example.moyeorak.security.AdminAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminMainImageService {

    private final MainImageRepository mainImageRepository;
    private final AdminAuthHelper adminAuthHelper;

    /** Presigner is injected (recommended to define as @Bean). */
    private final S3Presigner s3Presigner;

    /** Allow startup even if property is empty, check at runtime. */
    @Value("${app.s3.bucket:}")
    private String bucketName;

    /** 허용할 이미지 Content-Type 화이트리스트 (선택 입력 시 검증용) */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    /* ========================= Read ========================= */
    @Transactional(readOnly = true)
    public List<AdminMainImageResponse> getMainImages(HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        ensureRegionAccess(admin);

        Long regionId = admin.getRegion().getId();
        return mainImageRepository.findByRegionIdOrderByDisplayOrderAsc(regionId)
                .stream()
                .map(AdminMainImageResponse::from)
                .toList();
    }

    /* ========================= Create ========================= */
    @Transactional
    public AdminMainImageResponse createMainImage(AdminMainImageCreateRequest dto, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        ensureRegionAccess(admin);

        if (isBlank(dto.getImageUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl is required.");
        }

        Long regionId = admin.getRegion().getId();
        Integer maxOrder = mainImageRepository.findMaxDisplayOrderByRegionId(regionId);
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        MainImage image = MainImage.builder()
                .imageUrl(dto.getImageUrl())
                .title("")                 // 필요 시 dto.getTitle() 사용
                .displayOrder(nextOrder)
                .isActive(true)
                .region(admin.getRegion())
                .build();

        return AdminMainImageResponse.from(mainImageRepository.save(image));
    }

    /* ============== Bulk Update (Status/Order) ============== */
    @Transactional
    public void updateMainImages(List<AdminMainImageUpdateRequest> requestList, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        ensureRegionAccess(admin);

        Long regionId = admin.getRegion().getId();

        for (AdminMainImageUpdateRequest req : requestList) {
            MainImage image = mainImageRepository.findById(req.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Main image not found."));

            // prevent editing resources of other regions
            if (image.getRegion() == null || !image.getRegion().getId().equals(regionId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region resource.");
            }

            // 정렬 변경이 필요하면 주석 해제
            // if (req.getDisplayOrder() != null) {
            //     image.changeDisplayOrder(req.getDisplayOrder());
            // }

            if (req.getIsActive() != null) {
                image.changeActiveStatus(req.getIsActive());
            }
        }
    }

    /* ========================= Delete ========================= */
    @Transactional
    public void deleteById(Long id, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        ensureRegionAccess(admin);

        Long regionId = admin.getRegion().getId();

        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Main image not found."));

        if (image.getRegion() == null || !image.getRegion().getId().equals(regionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region resource.");
        }

        mainImageRepository.delete(image);
    }

    /* ============== S3 Presigned PUT URL (Content-Type은 서명에서 제외) ============== */
    @Transactional(readOnly = true)
    public String createPresignedPutUrl(String filename,
                                        String contentType, // optional: 검증/로그용, 서명에는 반영하지 않음
                                        HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        ensureRegionAccess(admin);

        if (isBlank(bucketName)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 bucket is not configured.");
        }
        if (isBlank(filename)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filename is required.");
        }
        // contentType은 선택값: 있으면 화이트리스트 검증만 수행(서명에는 반영하지 않음)
        if (!isBlank(contentType) && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported contentType: " + contentType);
        }

        String safeFilename = sanitizeFilename(filename);
        String key = "main-images/" + admin.getRegion().getId() + "/" + safeFilename;

        // 👇 Content-Type을 설정하지 않습니다 → SignedHeaders에서 제외됨(host만 남음)
        PutObjectRequest putObject = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObject)
                .build();

        // Use injected presigner (no per-call create/close).
        return s3Presigner.presignPutObject(presignReq).url().toString();
    }

    /* ========================= 내부 유틸 ========================= */

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static void ensureRegionAccess(User admin) {
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region.");
        }
    }

    /** 경로 분리자/상대경로 차단 등 간단 위생 처리 */
    private static String sanitizeFilename(String filename) {
        String f = filename.trim();
        if (f.contains("/") || f.contains("\\") || f.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename.");
        }
        return f;
    }
}
