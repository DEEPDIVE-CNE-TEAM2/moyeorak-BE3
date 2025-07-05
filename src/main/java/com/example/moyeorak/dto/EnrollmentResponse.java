package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private Long programId;
    private Long regionId;
    private LocalDateTime enrolledAt;
    private String status;
    private Integer paidAmount;
    private String cancelReason;
}
