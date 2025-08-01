package com.example.moyeorak.dto.admin;

import com.example.moyeorak.entity.MainImage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMainImageResponse {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private boolean isActive;

    public static AdminMainImageResponse from(MainImage image) {
        return AdminMainImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .isActive(image.isActive())
                .build();
    }
}