// 파일 위치: com/example/moyeorak/dto/NoticeResponse.java
package com.example.moyeorak.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private String regionName;
    private String authorName;

    public static NoticeResponse from(NoticeDto notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt().toLocalDate().atStartOfDay())
                .updatedAt(notice.getUpdatedAt().toLocalDate().atStartOfDay())
                .viewCount(notice.getViewCount())
                .regionName(notice.getRegionName())
                .authorName(notice.getAuthorName())
                .build();
    }
}
