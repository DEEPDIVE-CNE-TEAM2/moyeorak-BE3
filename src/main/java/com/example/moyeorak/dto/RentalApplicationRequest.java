package com.example.moyeorak.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalApplicationRequest {

    @NotNull(message = "대관 ID는 필수입니다.")
    private Integer rentalId;

    @NotNull(message = "신청 날짜는 필수입니다.")
    private LocalDate requestedDate;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalTime requestedStartTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalTime requestedEndTime;

    @NotNull(message = "신청 사유를 입력해주세요.")
    @Size(min = 1, max = 500, message = "신청 사유는 1자 이상 500자 이하로 입력해야 합니다.")
    private String note;

    @NotNull(message = "신청 인원을 입력해주세요.")
    @Min(value = 1, message = "신청 인원은 최소 1명 이상이어야 합니다.")
    private Integer peopleCount;
}
