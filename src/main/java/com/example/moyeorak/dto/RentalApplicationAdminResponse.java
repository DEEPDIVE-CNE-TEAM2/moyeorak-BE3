package com.example.moyeorak.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationAdminResponse {

    private Long id;

    private String regionName;
    private String location;
    private String requestedDate;
    private String requestedTime;

    private String applicantName;
    private String status;         // 예: "approved"
    private String statusLabel;    // 예: "승인" ← ✅ 추가
}

