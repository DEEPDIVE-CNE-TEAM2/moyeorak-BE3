package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.admin.AdminNoticeListResponse;
import com.example.moyeorak.dto.admin.AdminNoticeRequest;
import com.example.moyeorak.dto.admin.AdminNoticeResponse;

import java.util.List;

public interface AdminNoticeService {

    // 생성
    AdminNoticeResponse createNotice(AdminNoticeRequest request, Long userId, Long regionId);

    // 목록
    List<AdminNoticeListResponse> getNoticeList(Long userId, Long regionId);

    // 상세
    AdminNoticeResponse getNoticeDetail(Long noticeId, Long userId);

    // 수정
    AdminNoticeResponse updateNotice(Long noticeId, AdminNoticeRequest request, Long userId);

    // 삭제
    void deleteNotice(Long noticeId, Long userId);
}
