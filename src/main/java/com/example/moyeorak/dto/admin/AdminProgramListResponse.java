package com.example.moyeorak.dto.admin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProgramListResponse {

    private Long id;                 // 프로그램 ID
    private String title;            // 프로그램명
    private String facilityName;     // 시설명
    private String usagePeriod;      // 수강기간 ("YYYY-MM-DD ~ YYYY-MM-DD")
    private Integer capacity;        // 총 정원
    private Integer currentEnrollment; // 현재 신청 인원 (지금은 0으로 하드코딩 나중에 실시간 처리 구현할거임)
    private String progressStatus;   // 수업 예정 / 진행중 / 수업 종료
}