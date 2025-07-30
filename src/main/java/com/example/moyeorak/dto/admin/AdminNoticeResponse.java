package com.example.moyeorak.dto.admin;

import com.example.moyeorak.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminNoticeResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private int viewCount;
    private String authorName;

    public static AdminNoticeResponse from(Notice notice) {
        return AdminNoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .viewCount(notice.getViewCount())
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : "알 수 없음")
                .build();
    }
}