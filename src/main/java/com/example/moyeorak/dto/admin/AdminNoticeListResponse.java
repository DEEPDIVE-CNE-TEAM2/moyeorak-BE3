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
    private Long authorId;       // ⬅️ MAS: 이름 대신 식별자만 보유
    private LocalDate createdDate;
    private int viewCount;

    public static AdminNoticeListResponse from(Notice notice) {
        LocalDate createdDate = null;
        if (notice.getCreatedAt() != null) {
            // createdAt이 OffsetDateTime 이라면 toLocalDate()로 날짜만 추출
            createdDate = notice.getCreatedAt().toLocalDate();
            // 필요 시: notice.getCreatedAt().toLocalDateTime().toLocalDate();
        }

        return AdminNoticeListResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .authorId(notice.getAuthorId())
                .createdDate(createdDate)
                .viewCount(notice.getViewCount())
                .build();
    }
}
