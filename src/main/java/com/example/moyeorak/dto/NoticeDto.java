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
                .createdAt(notice.getCreatedAt() != null ? notice.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(notice.getUpdatedAt() != null ? notice.getUpdatedAt().toLocalDateTime() : null)
                .viewCount(notice.getViewCount())
                // 엔티티에 작성자/지역 이름 필드가 없으면 null 유지
                .authorName(null)
                .regionName(null)
                // 현재는 연관으로부터 ID 추출
                .regionId(notice.getRegion() != null ? notice.getRegion().getId() : null)
                // 엔티티를 MAS(FK 전용)로 바꾸면 아래로 교체
                // .regionId(notice.getRegionId())
                .build();
    }
}
