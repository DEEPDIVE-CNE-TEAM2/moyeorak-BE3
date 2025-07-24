package com.example.moyeorak.dto.admin;

import lombok.Builder;

@Builder
public record AdminUserEnrollmentDto(
        Long enrollmentId,
        String programTitle,
        String appliedDate,
        String regionName,
        String status,
        boolean canCancel
) {}