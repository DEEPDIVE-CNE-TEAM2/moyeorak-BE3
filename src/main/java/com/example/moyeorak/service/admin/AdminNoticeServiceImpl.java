package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminNoticeListResponse;
import com.example.moyeorak.dto.admin.AdminNoticeRequest;
import com.example.moyeorak.dto.admin.AdminNoticeResponse;
import com.example.moyeorak.entity.Notice;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.exception.BusinessException;
import com.example.moyeorak.exception.ErrorCode;
import com.example.moyeorak.repository.NoticeRepository;
import com.example.moyeorak.repository.RegionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final NoticeRepository noticeRepository;
    private final RegionRepository regionRepository;

    /** 공지 생성 */
    @Override
    @Transactional
    public AdminNoticeResponse createNotice(AdminNoticeRequest request, Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));

        assertRegionManager(userId, region);

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .region(region)     // Notice가 Region 연관을 보유한다고 가정
                .authorId(userId)
                .build();

        Notice saved = noticeRepository.save(notice);
        return AdminNoticeResponse.from(saved);
    }

    /** 지역별 공지 목록 */
    @Override
    public List<AdminNoticeListResponse> getNoticeList(Long userId, Long regionId) {
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_REGION));

        assertRegionManager(userId, region);

        return noticeRepository.findByRegion_Id(regionId).stream()
                .map(AdminNoticeListResponse::from)
                .toList();
    }

    /** 공지 상세 */
    @Override
    public AdminNoticeResponse getNoticeDetail(Long noticeId, Long userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항이 존재하지 않습니다."));

        assertRegionManager(userId, notice.getRegion());
        return AdminNoticeResponse.from(notice);
    }

    /** 공지 수정 */
    @Override
    @Transactional
    public AdminNoticeResponse updateNotice(Long noticeId, AdminNoticeRequest request, Long userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항이 존재하지 않습니다."));

        assertRegionManager(userId, notice.getRegion());

        if (request.getTitle() != null)   notice.setTitle(request.getTitle());
        if (request.getContent() != null) notice.setContent(request.getContent());
        // dirty checking으로 저장
        return AdminNoticeResponse.from(notice);
    }

    /** 공지 삭제 */
    @Override
    @Transactional
    public void deleteNotice(Long noticeId, Long userId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항이 존재하지 않습니다."));

        assertRegionManager(userId, notice.getRegion());
        noticeRepository.delete(notice);
    }

    // ───────── helpers ─────────
    private void assertRegionManager(Long userId, Region region) {
        Long managerId = region.getManagerId();
        if (managerId == null || !managerId.equals(userId)) {
            // 전용 코드가 없으면 공통 권한 코드 사용
            throw new BusinessException(ErrorCode.UNAUTHORIZED_FACILITY_ACCESS);
        }
    }
}
