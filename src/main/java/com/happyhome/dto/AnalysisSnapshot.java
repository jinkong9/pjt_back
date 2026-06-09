package com.happyhome.dto;

public record AnalysisSnapshot(
        String label,
        double latitude,
        double longitude,
        int radiusMeters,
        int commercialCount,
        int trafficEventCount,
        int score,
        String riskLevel,
        String source
) {
}

