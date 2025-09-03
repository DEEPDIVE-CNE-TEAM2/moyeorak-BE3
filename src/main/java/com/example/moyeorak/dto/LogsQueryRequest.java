package com.example.moyeorak.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class LogsQueryRequest {
    private List<String> logGroupNames; // 조회할 로그 그룹들

    @NotBlank
    private String queryString;         // 예: fields @timestamp, @message | sort @timestamp desc | limit 50

    private Instant startTime;

    private Instant endTime;
}
