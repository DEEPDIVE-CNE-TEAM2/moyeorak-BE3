package com.example.moyeorak.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class MetricQueryResponse {
    private String id;
    private List<Double> values;
    private List<Instant> timestamps;
}
