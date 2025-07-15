package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDateTime;

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
    private String status;
    private String statusLabel;
    private Integer peopleCount;
    private LocalDateTime createdAt;
}

