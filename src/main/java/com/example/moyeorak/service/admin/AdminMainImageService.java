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
    private final S3Presigner s3Presigner;

    /** 프로퍼티가 비어 있어도 부팅되게 하고, 런타임에 검사 */
    @Value("${app.s3.bucket:}")
    private String bucketName;

    /* ========================= 조회 ========================= */
    @Transactional(readOnly = true)
    public List<AdminMainImageResponse> getMainImages(HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        Long regionId = admin.getRegion().getId();

        return mainImageRepository.findByRegionIdOrderByDisplayOrderAsc(regionId)
                .stream()
                .map(AdminMainImageResponse::from)
                .toList();
    }

    /* ========================= 생성 ========================= */
    @Transactional
    public AdminMainImageResponse createMainImage(AdminMainImageCreateRequest dto, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        Long regionId = admin.getRegion().getId();

        if (dto.getImageUrl() == null || dto.getImageUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 URL은 필수입니다.");
        }

        Integer maxOrder = mainImageRepository.findMaxDisplayOrderByRegionId(regionId);
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        MainImage image = MainImage.builder()
                .imageUrl(dto.getImageUrl())
                .title("") // 필요 시 dto.getTitle() 사용
                .displayOrder(nextOrder)
                .isActive(true)
                .region(admin.getRegion())
                .build();

        return AdminMainImageResponse.from(mainImageRepository.save(image));
    }

    /* ============== 일괄 수정 (표시여부/순서) ============== */
    @Transactional
    public void updateMainImages(List<AdminMainImageUpdateRequest> requestList, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        Long regionId = admin.getRegion().getId();

        for (AdminMainImageUpdateRequest req : requestList) {
            MainImage image = mainImageRepository.findById(req.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "홍보물을 찾을 수 없습니다."));

            if (image.getRegion() == null || !image.getRegion().getId().equals(regionId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 지역 리소스에 대한 권한이 없습니다.");
            }

            // if (req.getDisplayOrder() != null) image.changeDisplayOrder(req.getDisplayOrder());
            if (req.getIsActive() != null) image.changeActiveStatus(req.getIsActive());
        }
    }

    /* ========================= 삭제 ========================= */
    @Transactional
    public void deleteById(Long id, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        Long regionId = admin.getRegion().getId();

        MainImage image = mainImageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "홍보물을 찾을 수 없습니다."));

        if (image.getRegion() == null || !image.getRegion().getId().equals(regionId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 지역 리소스에 대한 권한이 없습니다.");
        }

        mainImageRepository.delete(image);
    }

    /* ============== S3 Presigned PUT URL 생성 ============== */
    @Transactional(readOnly = true)
    public String createPresignedPutUrl(String filename, String filetype, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        if (admin.getRegion() == null || admin.getRegion().getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        if (filename == null || filename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filename은 필수입니다.");
        }
        if (filetype == null || filetype.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "filetype은 필수입니다.");
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "S3 버킷(app.s3.bucket) 설정이 누락되었습니다.");
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

        return s3Presigner.presignPutObject(presignReq).url().toString();
    }
}
