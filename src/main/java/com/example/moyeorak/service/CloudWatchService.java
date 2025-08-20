package com.example.moyeorak.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

// DTO 패키지 경로는 실제 위치에 맞게 변경하세요
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
        MetricDataResult r = resp.metricDataResults().get(0);

        return new MetricQueryResponse(
                r.id(),
                r.values(),
                r.timestamps().stream().map(Instant::from).toList()
        );
    }

    public List<Map<String, Object>> runLogsInsights(LogsQueryRequest req) throws InterruptedException {
        StartQueryRequest start = StartQueryRequest.builder()
                .logGroupNames(req.getLogGroupNames())
                .startTime(req.getStartTime().getEpochSecond())
                .endTime(req.getEndTime().getEpochSecond())
                .queryString(req.getQueryString())
                .limit(1000)
                .build();

        String queryId = logs.startQuery(start).queryId();

        // 간단 폴링 (실서비스면 비동기 or 웹훅/프론트 재시도 권장)
        while (true) {
            GetQueryResultsResponse results = logs.getQueryResults(b -> b.queryId(queryId));
            String status = results.statusAsString();
            if ("Complete".equals(status) || "Failed".equals(status) || "Cancelled".equals(status)) {
                return results.results().stream().map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    row.forEach(f -> m.put(f.field(), f.value()));
                    return m;
                }).toList();
            }
            Thread.sleep(800);
        }
    }
}