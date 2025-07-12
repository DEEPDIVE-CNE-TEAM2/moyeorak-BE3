package com.example.moyeorak.service;

import com.example.moyeorak.dto.NoticeDto;
import com.example.moyeorak.dto.NoticeRequest;
import com.example.moyeorak.entity.Notice;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.repository.NoticeRepository;
import com.example.moyeorak.repository.RegionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final RegionRepository regionRepository;

    // ======================== 생성 ========================

    @Transactional
    public NoticeDto create(User author, NoticeRequest request) {
        log.info("[CREATE] 공지 생성 요청 - title: {}, regionId: {}, authorId: {}",
                request.getTitle(), request.getRegionId(), author.getId());

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("지역 정보가 존재하지 않습니다."));

        if (!region.getManager().getId().equals(author.getId())) {
            throw new IllegalArgumentException("해당 지역의 관리자만 공지를 작성할 수 있습니다.");
        }

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .region(region)
                .author(author)
                .build();

        return toDto(noticeRepository.save(notice));
    }

    // ======================== 조회 ========================

    public NoticeDto getNotice(Long id) {
        log.info("[GET] 공지 단건 조회 요청 - id: {}", id);
        return toDto(findNotice(id));
    }

    @Transactional
    public NoticeDto getNoticeAndIncreaseViewCount(Long id) {
        log.info("[GET] 공지 조회 및 조회수 증가 요청 - id: {}", id);
        Notice notice = findNotice(id);
        notice.setViewCount(notice.getViewCount() + 1);
        return toDto(notice);
    }

    public List<NoticeDto> getNoticesByRegionId(Long regionId) {
        log.info("[GET] 지역별 공지 목록 조회 - regionId: {}", regionId);
        return noticeRepository.findByRegionId(regionId).stream()
                .map(this::toDto)
                .toList();
    }

    public NoticeDto getNoticeByRegion(Long noticeId, Long regionId) {
        log.info("[GET] 지역별 공지 단건 조회 요청 - regionId: {}, noticeId: {}", regionId, noticeId);

        Notice notice = noticeRepository.findByIdAndRegionId(noticeId, regionId)
                .orElseThrow(() -> new EntityNotFoundException("해당 지역에 공지사항이 존재하지 않습니다."));

        return toDto(notice);
    }

    public List<NoticeDto> getAllNotices() {
        log.info("[GET] 전체 공지 목록 조회");
        return noticeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // ======================== 수정 ========================

    @Transactional
    public NoticeDto updateNotice(Long noticeId, Long userId, NoticeRequest request) throws AccessDeniedException {
        log.info("[UPDATE] 공지 수정 요청 - noticeId: {}, userId: {}", noticeId, userId);

        Notice notice = findNotice(noticeId);

        if (!notice.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());

        return toDto(noticeRepository.save(notice));
    }

    // ======================== 삭제 ========================

    @Transactional
    public void deleteNotice(Long id, Long userId) throws AccessDeniedException {
        log.info("[DELETE] 공지 삭제 요청 - noticeId: {}, userId: {}", id, userId);

        Notice notice = findNotice(id);

        if (!notice.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }

        noticeRepository.delete(notice);
    }

    // ======================== 내부 유틸 ========================

    private Notice findNotice(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항이 존재하지 않습니다."));
    }

    private NoticeDto toDto(Notice notice) {
        return NoticeDto.fromEntity(notice);
    }
}
