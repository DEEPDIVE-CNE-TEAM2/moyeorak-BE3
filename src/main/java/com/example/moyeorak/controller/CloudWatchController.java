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
        return service.runLogsInsightsAsync(req);
    }
}
