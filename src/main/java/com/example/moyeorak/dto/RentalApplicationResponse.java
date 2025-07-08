package com.example.moyeorak.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationResponse {

    private Long id;
    private String location;
    private String address;
    private LocalDate requestedDate;
    private String requestedTime;
    private Integer capacity;

    private String status;       // ✅ 추가: 내부 코드 ("approved")
    private String statusLabel;  // ✅ 추가: 한글 라벨 ("승인")
}
