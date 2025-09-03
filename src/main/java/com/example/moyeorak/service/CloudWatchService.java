package com.example.moyeorak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executor;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.StartQueryRequest;

import com.example.moyeorak.dto.MetricQueryRequest;
import com.example.moyeorak.dto.MetricQueryResponse;
import com.example.moyeorak.dto.LogsQueryRequest;

@Service
@RequiredArgsConstructor
public class CloudWatchService {

    private final CloudWatchClient cw;
    private final CloudWatchLogsClient logs;

    public MetricQueryResponse getMetricData(MetricQueryRequest req) {
        // MetricStat 구성
        List<Dimension> dims = req.getDimensions() == null ? List.of() :
                req.getDimensions().entrySet().stream()
                        .map(e -> Dimension.builder().name(e.getKey()).value(e.getValue()).build())
                        .toList();

        Metric metric = Metric.builder()
                .namespace(req.getNamespace())
                .metricName(req.getMetricName())
                .dimensions(dims)
                .build();

        MetricStat metricStat = MetricStat.builder()
                .metric(metric)
                .stat(req.getStat() == null ? "Average" : req.getStat())
                .period(req.getPeriod() == null ? 60 : req.getPeriod())
                .build();

        MetricDataQuery query = MetricDataQuery.builder()
                .id("q1")
                .metricStat(metricStat)
                .returnData(true)
                .build();

        GetMetricDataRequest request = GetMetricDataRequest.builder()
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .metricDataQueries(List.of(query))
                .build();

        GetMetricDataResponse resp = cw.getMetricData(request);
        List<MetricDataResult> results = resp.metricDataResults();

        // ✅ 빈 결과 처리
        if (results.isEmpty()) {
            return new MetricQueryResponse(
                    "q1",
                    List.of(),   // values
                    List.of()    // timestamps
            );
        }

        MetricDataResult r = results.get(0);
        return new MetricQueryResponse(
                r.id(),
                r.values(),
                r.timestamps().stream().map(Instant::from).toList()
        );
    }

    /**
     * CloudWatch Logs Insights 비동기 실행
     */
    public CompletableFuture<List<Map<String, Object>>> runLogsInsightsAsync(LogsQueryRequest req) {
        StartQueryRequest start = StartQueryRequest.builder()
                .logGroupNames(req.getLogGroupNames())
                .startTime(req.getStartTime().getEpochSecond())
                .endTime(req.getEndTime().getEpochSecond())
                .queryString(req.getQueryString().trim())
                .limit(1000)
                .build();

        String queryId = logs.startQuery(start).queryId();

        // ✅ 비동기 폴링 시작 (타임아웃 60초)
        return pollLogsInsights(queryId, 0, 60_000);
    }

    /**
     * Logs Insights 결과를 비동기/재귀적으로 폴링
     */
    private CompletableFuture<List<Map<String, Object>>> pollLogsInsights(String queryId,
                                                                          long elapsedMillis,
                                                                          long timeoutMillis) {
        return CompletableFuture
                .supplyAsync(() -> logs.getQueryResults(b -> b.queryId(queryId)))
                .thenCompose(results -> {
                    String status = results.statusAsString();
                    if ("Complete".equals(status) || "Failed".equals(status) || "Cancelled".equals(status)) {
                        // 결과 매핑
                        List<Map<String, Object>> mapped = results.results().stream().map(row -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            row.forEach(f -> m.put(f.field(), f.value()));
                            return m;
                        }).toList();
                        return CompletableFuture.completedFuture(mapped);
                    }

                    if (elapsedMillis >= timeoutMillis) {
                        return CompletableFuture.failedFuture(new RuntimeException("Logs Insights query timed out"));
                    }

                    // ⏳ 800ms 지연 후 재귀 폴링
                    Executor delayed = CompletableFuture.delayedExecutor(800, TimeUnit.MILLISECONDS);
                    return CompletableFuture
                            .runAsync(() -> { /* just delay */ }, delayed)
                            .thenCompose(v -> pollLogsInsights(queryId, elapsedMillis + 800, timeoutMillis));
                });
    }
}
