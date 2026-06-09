package com.happyhome.dto;

import com.happyhome.dto.CommercialPlace;
import com.happyhome.dto.CommercialSummary;
import com.happyhome.dto.TrafficEvent;
import com.happyhome.dto.TrafficRiskSummary;
import java.util.List;

public record HousingAnalysis(
        String label,
        double latitude,
        double longitude,
        int radiusMeters,
        CommercialSummary commercialSummary,
        TrafficRiskSummary trafficRiskSummary,
        AnalysisScore score,
        List<CommercialPlace> places,
        List<TrafficEvent> events,
        String source
) {
}

