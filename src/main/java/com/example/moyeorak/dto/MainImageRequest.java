package com.example.moyeorak.dto;

public record MainImageRequest(
        String title,
        String imageUrl,
        Integer displayOrder,
        Boolean isActive,
        Long regionId
) {}
