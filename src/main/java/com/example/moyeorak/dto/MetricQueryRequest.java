package com.example.moyeorak.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class MetricQueryRequest {
    private String namespace;          // 예: AWS/EC2
    private String metricName;         // 예: CPUUtilization
    private Map<String, String> dimensions; // { "InstanceId": "i-0123..." }
    private String stat;               // Average | Sum | Maximum ...
    private Integer period;            // 초 단위 (예: 60)
    private Instant startTime;         // ISO8601
    private Instant endTime;           // ISO8601
}
