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

    /* ========================= Read ========================= */
    @Transactional(readOnly = true)
    public List<AdminMainImageResponse> getMainImages(HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region.");
        }
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
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region.");
        }
        Long regionId = admin.getRegion().getId();

        if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "imageUrl is required.");
        }

        Integer maxOrder = mainImageRepository.findMaxDisplayOrderByRegionId(regionId);
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        MainImage image = MainImage.builder()
                .imageUrl(dto.getImageUrl())
                .title("")                 // use dto.getTitle() if needed
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
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region.");
        }
        Long regionId = admin.getRegion().getId();

        for (AdminMainImageUpdateRequest req : requestList) {
            MainImage image = mainImageRepository.findById(req.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Main image not found."));

            // prevent editing resources of other regions
            if (image.getRegion() == null || !image.getRegion().getId().equals(regionId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region resource.");
            }

            // If you want to enable order change, open the entity method.
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
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region.");
        }
        Long regionId = admin.getRegion().getId();

        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Main image not found."));

        if (image.getRegion() == null || !image.getRegion().getId().equals(regionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region resource.");
        }

        mainImageRepository.delete(image);
    }

    /* ============== S3 Presigned PUT URL ============== */
    @Transactional(readOnly = true)
    public String createPresignedPutUrl(String filename, String filetype, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission for this region.");
        }

        if (bucketName == null || bucketName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 bucket is not configured.");
        }
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filename is required.");
        }
        if (filetype == null || filetype.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filetype is required.");
        }

        String key = "main-images/" + admin.getRegion().getId() + "/" + filename;

        PutObjectRequest putObject = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(filetype)
                .build();

        PutObjectPresignRequest presignReq = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObject)
                .build();

        // Use injected presigner (no per-call create/close).
        return s3Presigner.presignPutObject(presignReq).url().toString();
    }
}
