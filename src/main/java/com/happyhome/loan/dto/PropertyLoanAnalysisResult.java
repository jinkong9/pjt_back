package com.happyhome.loan.dto;

import java.math.BigDecimal;
import java.util.List;

public record PropertyLoanAnalysisResult(
        CostSummary cost,
        LimitSummary limits,
        ReadinessSummary readiness,
        RepaymentSummary repayment,
        List<RecommendedProduct> recommendations,
        String recommendationWarning,
        Assumptions assumptions,
        String disclaimer
) {
    public record CostSummary(
            BigDecimal propertyPrice,
            BigDecimal incidentalCost,
            BigDecimal totalRequiredFunds,
            BigDecimal availableAssets,
            BigDecimal existingLoanBalance
    ) {
    }

    public record LimitSummary(
            BigDecimal fundingNeed,
            BigDecimal ltvLimit,
            BigDecimal dsrLoanLimit,
            BigDecimal expectedMaximumLoan
    ) {
    }

    public record ReadinessSummary(
            BigDecimal minimumRequiredEquity,
            BigDecimal additionalAssetsNeeded,
            Integer monthsToTarget,
            boolean feasibleNow
    ) {
    }

    public record RepaymentSummary(
            BigDecimal monthlyPayment,
            BigDecimal totalInterest,
            BigDecimal totalPayment,
            BigDecimal monthlyCashAfterPayment
    ) {
    }

    public record RecommendedProduct(
            String companyName,
            String productName,
            BigDecimal rate,
            String repaymentTypeName,
            String loanLimit
    ) {
    }

    public record Assumptions(
            BigDecimal ltvRatio,
            BigDecimal dsrRatio,
            BigDecimal incidentalCostRatio,
            int years,
            BigDecimal annualRate
    ) {
    }
}
