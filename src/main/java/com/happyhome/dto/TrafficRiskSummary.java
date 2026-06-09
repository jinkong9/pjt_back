package com.happyhome.dto;

public record TrafficRiskSummary(
        int eventCount,
        int roadworkCount,
        String riskLevel
) {
}

