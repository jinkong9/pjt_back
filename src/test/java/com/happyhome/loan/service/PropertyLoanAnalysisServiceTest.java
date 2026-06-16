package com.happyhome.loan.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.loan.dto.LoanRepaymentType;
import com.happyhome.loan.dto.PropertyLoanAnalysisRequest;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult;
import com.happyhome.member.dto.FinancialProfile;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PropertyLoanAnalysisServiceTest {

    private final PropertyLoanAnalysisService service = new PropertyLoanAnalysisService(
            new LoanCalculator(),
            null,
            new BigDecimal("0.60"),
            new BigDecimal("0.40"),
            new BigDecimal("0.04"),
            3
    );

    @Test
    void calculatesTotalFundsAndLtvLimitedEquity() {
        PropertyLoanAnalysisResult result = service.analyze(
                deal("100000"),
                profile("200000000", "200000000", "5000000", "0"),
                request()
        );

        assertThat(result.cost().propertyPrice()).isEqualByComparingTo("1000000000");
        assertThat(result.cost().incidentalCost()).isEqualByComparingTo("40000000");
        assertThat(result.cost().totalRequiredFunds()).isEqualByComparingTo("1040000000");
        assertThat(result.limits().ltvLimit()).isEqualByComparingTo("600000000");
        assertThat(result.readiness().minimumRequiredEquity()).isEqualByComparingTo("440000000");
        assertThat(result.readiness().additionalAssetsNeeded()).isEqualByComparingTo("240000000");
        assertThat(result.readiness().monthsToTarget()).isEqualTo(48);
    }

    @Test
    void existingMonthlyDebtReducesDsrLoanLimit() {
        PropertyLoanAnalysisResult withoutDebt = service.analyze(
                deal("100000"),
                profile("100000000", "100000000", "3000000", "0"),
                request()
        );
        PropertyLoanAnalysisResult withDebt = service.analyze(
                deal("100000"),
                profile("100000000", "100000000", "3000000", "2000000"),
                request()
        );

        assertThat(withDebt.limits().dsrLoanLimit())
                .isLessThan(withoutDebt.limits().dsrLoanLimit());
    }

    @Test
    void reportsFeasibleWhenAssetsCoverRequiredEquity() {
        PropertyLoanAnalysisResult result = service.analyze(
                deal("100000"),
                profile("500000000", "200000000", "0", "0"),
                request()
        );

        assertThat(result.readiness().additionalAssetsNeeded()).isZero();
        assertThat(result.readiness().monthsToTarget()).isZero();
        assertThat(result.readiness().feasibleNow()).isTrue();
    }

    @Test
    void leavesTargetDurationUnknownWhenSavingsAreZero() {
        PropertyLoanAnalysisResult result = service.analyze(
                deal("100000"),
                profile("100000000", "100000000", "0", "0"),
                request()
        );

        assertThat(result.readiness().additionalAssetsNeeded()).isPositive();
        assertThat(result.readiness().monthsToTarget()).isNull();
    }

    private PropertyLoanAnalysisRequest request() {
        return new PropertyLoanAnalysisRequest(1, 30, new BigDecimal("4.2"), LoanRepaymentType.EQUAL_PAYMENT);
    }

    private HouseDeal deal(String amountInTenThousands) {
        HouseDeal deal = new HouseDeal();
        deal.setNo(1);
        deal.setAptName("테스트 아파트");
        deal.setDealAmount(amountInTenThousands);
        return deal;
    }

    private FinancialProfile profile(String assets, String income, String savings, String existingPayment) {
        return new FinancialProfile(
                "ssafy",
                new BigDecimal(assets),
                new BigDecimal(income),
                new BigDecimal(savings),
                BigDecimal.ZERO,
                new BigDecimal(existingPayment)
        );
    }
}
