package com.example.moyeorak.dto.admin;

import com.example.moyeorak.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNoticeResponse {

    private Long id;
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminNoticeResponse from(Notice notice) {
        return AdminNoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .viewCount(notice.getViewCount())
                .createdAt(notice.getCreatedAt() == null ? null : notice.getCreatedAt().toLocalDateTime())
                .updatedAt(notice.getUpdatedAt() == null ? null : notice.getUpdatedAt().toLocalDateTime())
                .build();
    }
}
