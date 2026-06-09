package com.happyhome.dto;

public record AnalysisScore(
        int total,
        int commercialScore,
        int trafficPenalty,
        String level,
        String message
) {
}

