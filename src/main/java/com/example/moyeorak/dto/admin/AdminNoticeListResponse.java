package com.example.moyeorak.dto.admin;


import com.example.moyeorak.entity.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdminNoticeListResponse {

    private Long id;
    private String title;
    private String authorName;
    private LocalDate createdDate;
    private int viewCount;

    public static AdminNoticeListResponse from(Notice notice) {
        return AdminNoticeListResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .authorName(notice.getAuthor().getName())
                .createdDate(notice.getCreatedAt().toLocalDate()) // 날짜만
                .viewCount(notice.getViewCount())
                .build();
    }
}