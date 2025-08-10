package com.example.moyeorak.service.admin;

import com.example.moyeorak.dto.NoticeResponse;
import com.example.moyeorak.dto.admin.AdminNoticeListResponse;
import com.example.moyeorak.dto.admin.AdminNoticeRequest;
import com.example.moyeorak.dto.admin.AdminNoticeResponse;
import com.example.moyeorak.entity.Notice;
import com.example.moyeorak.entity.Region;
import com.example.moyeorak.entity.User;
import com.example.moyeorak.jwt.JwtProvider;
import com.example.moyeorak.repository.NoticeRepository;
import com.example.moyeorak.repository.UserRepository;
import com.example.moyeorak.security.AdminAuthHelper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;
    private final AdminAuthHelper adminAuthHelper;


    // 공지사항 생성
    @Transactional
    public AdminNoticeResponse createNotice(AdminNoticeRequest dto, HttpServletRequest request) {
        // 1. 관리자 인증
        User admin = adminAuthHelper.getAdminFromRequest(request);

        // 2. 지역 조회
        Region region = admin.getRegion();

        // 3. 공지사항 엔티티 생성
        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(admin)
                .region(region)
                .build();

        return AdminNoticeResponse.from(noticeRepository.save(notice));
    }

    // 관리자 공지사항 리스트 조회
    @Transactional(readOnly = true)
    public List<AdminNoticeListResponse> getNoticeList(HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        Region region = admin.getRegion();

        return noticeRepository.findByRegion(region).stream()
                .map(AdminNoticeListResponse::from)
                .collect(Collectors.toList());
    }

    // 관리자 공지사항 상세보기
    @Transactional(readOnly = true)
    public AdminNoticeResponse getNoticeDetail(Long noticeId, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        return AdminNoticeResponse.from(notice);
    }

    // 공지사항 수정하기
    @Transactional
    public AdminNoticeResponse updateNotice(Long noticeId, AdminNoticeRequest dto, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());

        return AdminNoticeResponse.from(notice);
    }

    @Transactional
    public void deleteNotice(Long noticeId, HttpServletRequest request) {
        User admin = adminAuthHelper.getAdminFromRequest(request);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항이 존재하지 않습니다."));

        noticeRepository.delete(notice);
    }

}