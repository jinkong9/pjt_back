package com.happyhome.member.dto;

import java.math.BigDecimal;

public record FinancialProfile(
        String userId,
        BigDecimal availableAssets,
        BigDecimal annualIncome,
        BigDecimal monthlySavings,
        BigDecimal existingLoanBalance,
        BigDecimal existingMonthlyDebtPayment
) {
}
