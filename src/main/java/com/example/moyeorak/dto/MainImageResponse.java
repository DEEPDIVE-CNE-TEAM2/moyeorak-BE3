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
        String regionName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MainImageResponse from(MainImage image) {
        return MainImageResponse.builder()
                .id(image.getId())
                .title(image.getTitle())
                .imageUrl(image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .isActive(image.isActive())
                .regionName(image.getRegion().getName())
                .createdAt(image.getCreatedAt())
                .updatedAt(image.getUpdatedAt())
                .build();
    }
}
