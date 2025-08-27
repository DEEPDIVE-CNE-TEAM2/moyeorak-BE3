package com.example.moyeorak.dto;

import com.example.moyeorak.entity.MainImage;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MainImageResponse(
        Long id,
        String title,
        String imageUrl,
        Integer displayOrder,
        Boolean isActive,
        Long regionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MainImageResponse from(MainImage image) {
        return MainImageResponse.builder()
                .id(image.getId())
                .title(image.getTitle())
                .imageUrl(image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .isActive(image.getIsActive()) // Boolean 게터 사용
                .regionId(image.getRegionId()) // 엔티티 참조 대신 FK
                .createdAt(image.getCreatedAt() != null ? image.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(image.getUpdatedAt() != null ? image.getUpdatedAt().toLocalDateTime() : null)
                .build();
    }
}
