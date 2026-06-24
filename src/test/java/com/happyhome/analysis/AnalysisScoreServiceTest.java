package com.happyhome.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.analysis.service.AnalysisScoreService;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import org.junit.jupiter.api.Test;

class AnalysisScoreServiceTest {

    private final AnalysisScoreService scoreService = new AnalysisScoreService();

    @Test
    void rewardsCommercialTransitAndSafeTrafficWithBasePoints() {
        CommercialSummary commercial = new CommercialSummary(42, 12, 10, 7, 8, 5);
        TrafficRiskSummary traffic = new TrafficRiskSummary(0, 0, "low");
        TransitSummary transit = new TransitSummary(1, 1, 3, 5, 9);

        AnalysisScore score = scoreService.calculate(commercial, traffic, transit);

        assertThat(score.total()).isEqualTo(100);
        assertThat(score.commercialScore()).isEqualTo(25);
        assertThat(score.transitScore()).isEqualTo(30);
        assertThat(score.trafficSafetyScore()).isEqualTo(5);
    }

    @Test
    void strongCommercialAndTransitAreaScoresAroundNinetyDespiteTrafficRisk() {
        CommercialSummary commercial = new CommercialSummary(20, 5, 5, 3, 2, 5);
        TrafficRiskSummary traffic = new TrafficRiskSummary(10, 10, "high");
        TransitSummary transit = new TransitSummary(1, 1, 0, 0, 0);

        AnalysisScore score = scoreService.calculate(commercial, traffic, transit);

        assertThat(score.commercialScore()).isEqualTo(17);
        assertThat(score.transitScore()).isEqualTo(28);
        assertThat(score.trafficSafetyScore()).isZero();
        assertThat(score.total()).isEqualTo(90);
    }

    @Test
    void keepsBasePointsWhenNoInfrastructureExists() {
        CommercialSummary commercial = new CommercialSummary(0, 0, 0, 0, 0, 0);
        TrafficRiskSummary traffic = new TrafficRiskSummary(3, 0, "medium");
        TransitSummary transit = new TransitSummary(0, 0, 0, 0, 0);

        AnalysisScore score = scoreService.calculate(commercial, traffic, transit);

        assertThat(score.commercialScore()).isZero();
        assertThat(score.transitScore()).isZero();
        assertThat(score.trafficSafetyScore()).isZero();
        assertThat(score.total()).isEqualTo(45);
    }
}
