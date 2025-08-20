package com.example.moyeorak.controller;

import com.example.moyeorak.dto.LogsQueryRequest;
import com.example.moyeorak.dto.MetricQueryRequest;
import com.example.moyeorak.dto.MetricQueryResponse;
import com.example.moyeorak.service.CloudWatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cloudwatch")
@RequiredArgsConstructor
public class CloudWatchController {

    private final CloudWatchService service;

    @PostMapping("/metrics")
    public MetricQueryResponse getMetrics(@RequestBody @Valid MetricQueryRequest req) {
        return service.getMetricData(req);
    }

    @PostMapping("/logs/query")
    public CompletableFuture<List<Map<String, Object>>> queryLogs(@RequestBody @Valid LogsQueryRequest req) {
        return service.runLogsInsightsAsync(req)
                .exceptionally(ex -> {
                    // 예외 발생 시 처리 (ex.getMessage() 로 클라이언트에 알려주거나 로깅 가능)
                    throw new RuntimeException("Failed to query logs: " + ex.getMessage(), ex);
                });
    }
}
