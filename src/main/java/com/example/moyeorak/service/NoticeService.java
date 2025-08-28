package com.example.moyeorak.service;

import com.example.moyeorak.dto.NoticeDto;
import com.example.moyeorak.dto.NoticeRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface NoticeService {

    // 생성
    NoticeDto create(Long authorId, NoticeRequest request);

    // 조회
    NoticeDto getNotice(Long id);
    NoticeDto getNoticeAndIncreaseViewCount(Long id);
    List<NoticeDto> getNoticesByRegionId(Long regionId);
    NoticeDto getNoticeByRegion(Long noticeId, Long regionId);
    List<NoticeDto> getAllNotices();

    // 수정
    NoticeDto updateNotice(Long noticeId, Long userId, NoticeRequest request) throws AccessDeniedException;

    // 삭제
    void deleteNotice(Long id, Long userId) throws AccessDeniedException;
}
