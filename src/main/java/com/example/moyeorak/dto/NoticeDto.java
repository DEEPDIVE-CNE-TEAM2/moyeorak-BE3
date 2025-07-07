package com.example.moyeorak.dto;

import com.example.moyeorak.entity.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDto {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int viewCount;
    private String authorName;
    private String regionName;
    private Long regionId;

    public static NoticeDto fromEntity(Notice notice) {
        return NoticeDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .viewCount(notice.getViewCount())
                .authorName(notice.getAuthor() != null ? notice.getAuthor().getName() : null)
                .regionName(notice.getRegion() != null ? notice.getRegion().getName() : null)
                .regionId(notice.getRegion().getId())
                .build();
    }
}
