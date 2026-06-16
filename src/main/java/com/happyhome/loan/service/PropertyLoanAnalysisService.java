package com.happyhome.loan.service;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.loan.dto.LoanCalculationRequest;
import com.happyhome.loan.dto.LoanCalculationResult;
import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanRateOption;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.loan.dto.PropertyLoanAnalysisRequest;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult.Assumptions;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult.CostSummary;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult.LimitSummary;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult.ReadinessSummary;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult.RecommendedProduct;
import com.happyhome.loan.dto.PropertyLoanAnalysisResult.RepaymentSummary;
import com.happyhome.member.dto.FinancialProfile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PropertyLoanAnalysisService {

    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String DISCLAIMER =
            "본 결과는 입력한 금융정보와 보수적인 LTV·DSR 기준을 사용한 참고용 시뮬레이션이며 실제 심사와 다를 수 있습니다.";

    private final LoanCalculator loanCalculator;
    private final LoanProductService loanProductService;
    private final BigDecimal ltvRatio;
    private final BigDecimal dsrRatio;
    private final BigDecimal incidentalCostRatio;
    private final int recommendationCount;

    public PropertyLoanAnalysisService(
            LoanCalculator loanCalculator,
            LoanProductService loanProductService,
            @Value("${happyhome.loan.ltv-ratio:0.60}") BigDecimal ltvRatio,
            @Value("${happyhome.loan.dsr-ratio:0.40}") BigDecimal dsrRatio,
            @Value("${happyhome.loan.incidental-cost-ratio:0.04}") BigDecimal incidentalCostRatio,
            @Value("${happyhome.loan.recommendation-count:3}") int recommendationCount
    ) {
        this.loanCalculator = loanCalculator;
        this.loanProductService = loanProductService;
        this.ltvRatio = ltvRatio;
        this.dsrRatio = dsrRatio;
        this.incidentalCostRatio = incidentalCostRatio;
        this.recommendationCount = recommendationCount;
    }

    public PropertyLoanAnalysisResult analyze(
            HouseDeal deal,
            FinancialProfile profile,
            PropertyLoanAnalysisRequest request
    ) {
        BigDecimal propertyPrice = parsePropertyPrice(deal.getDealAmount());
        BigDecimal incidentalCost = money(propertyPrice.multiply(incidentalCostRatio));
        BigDecimal totalRequiredFunds = propertyPrice.add(incidentalCost);
        BigDecimal assets = zero(profile.availableAssets());
        BigDecimal fundingNeed = positive(totalRequiredFunds.subtract(assets));
        BigDecimal ltvLimit = money(propertyPrice.multiply(ltvRatio));
        BigDecimal dsrLoanLimit = calculateDsrLoanLimit(profile, request);
        BigDecimal borrowingCapacity = min(ltvLimit, dsrLoanLimit);
        BigDecimal expectedMaximumLoan = min(fundingNeed, borrowingCapacity);
        BigDecimal minimumRequiredEquity = positive(totalRequiredFunds.subtract(borrowingCapacity));
        BigDecimal additionalAssetsNeeded = positive(minimumRequiredEquity.subtract(assets));
        Integer monthsToTarget = targetMonths(additionalAssetsNeeded, profile.monthlySavings());
        RepaymentSummary repayment = repayment(expectedMaximumLoan, profile.monthlySavings(), request);
        RecommendationResult recommendationResult = recommendations();

        return new PropertyLoanAnalysisResult(
                new CostSummary(
                        propertyPrice,
                        incidentalCost,
                        totalRequiredFunds,
                        assets,
                        zero(profile.existingLoanBalance())
                ),
                new LimitSummary(fundingNeed, ltvLimit, dsrLoanLimit, expectedMaximumLoan),
                new ReadinessSummary(
                        minimumRequiredEquity,
                        additionalAssetsNeeded,
                        monthsToTarget,
                        additionalAssetsNeeded.signum() == 0
                ),
                repayment,
                recommendationResult.products(),
                recommendationResult.warning(),
                new Assumptions(ltvRatio, dsrRatio, incidentalCostRatio, request.years(), request.rate()),
                DISCLAIMER
        );
    }

    private BigDecimal parsePropertyPrice(String dealAmount) {
        if (dealAmount == null || dealAmount.isBlank()) {
            throw new IllegalArgumentException("거래금액이 없습니다.");
        }
        String digits = dealAmount.replaceAll("[^0-9.]", "");
        if (digits.isBlank()) {
            throw new IllegalArgumentException("거래금액을 해석할 수 없습니다.");
        }
        return money(new BigDecimal(digits).multiply(TEN_THOUSAND));
    }

    private BigDecimal calculateDsrLoanLimit(FinancialProfile profile, PropertyLoanAnalysisRequest request) {
        BigDecimal annualCapacity = zero(profile.annualIncome()).multiply(dsrRatio);
        BigDecimal existingAnnualPayment = zero(profile.existingMonthlyDebtPayment()).multiply(TWELVE);
        BigDecimal monthlyCapacity = positive(annualCapacity.subtract(existingAnnualPayment))
                .divide(TWELVE, 12, RoundingMode.HALF_UP);
        if (monthlyCapacity.signum() == 0) {
            return BigDecimal.ZERO;
        }
        int months = request.years() * 12;
        BigDecimal monthlyRate = request.rate()
                .divide(ONE_HUNDRED, 12, RoundingMode.HALF_UP)
                .divide(TWELVE, 12, RoundingMode.HALF_UP);
        if (monthlyRate.signum() == 0) {
            return money(monthlyCapacity.multiply(BigDecimal.valueOf(months)));
        }
        double rate = monthlyRate.doubleValue();
        double factor = Math.pow(1 + rate, months);
        double principal = monthlyCapacity.doubleValue() * (factor - 1) / (rate * factor);
        return money(BigDecimal.valueOf(principal));
    }

    private RepaymentSummary repayment(
            BigDecimal amount,
            BigDecimal monthlySavings,
            PropertyLoanAnalysisRequest request
    ) {
        if (amount.signum() == 0) {
            return new RepaymentSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, zero(monthlySavings));
        }
        LoanCalculationResult result = loanCalculator.calculate(new LoanCalculationRequest(
                amount,
                request.years(),
                request.rate(),
                request.repaymentType()
        ));
        return new RepaymentSummary(
                result.monthlyPayment(),
                result.totalInterest(),
                result.totalPayment(),
                zero(monthlySavings).subtract(result.monthlyPayment())
        );
    }

    private RecommendationResult recommendations() {
        if (loanProductService == null) {
            return new RecommendationResult(List.of(), null);
        }
        try {
            List<RecommendedProduct> products = new ArrayList<>();
            for (LoanProduct product : loanProductService.products(LoanType.MORTGAGE, 1)) {
                product.options().stream()
                        .filter(option -> recommendationRate(option) != null)
                        .min(Comparator.comparing(this::recommendationRate))
                        .ifPresent(option -> products.add(new RecommendedProduct(
                                product.companyName(),
                                product.productName(),
                                recommendationRate(option),
                                option.repaymentTypeName(),
                                product.loanLimit()
                        )));
            }
            return new RecommendationResult(
                    products.stream()
                            .sorted(Comparator.comparing(RecommendedProduct::rate))
                            .limit(recommendationCount)
                            .toList(),
                    null
            );
        } catch (RuntimeException exception) {
            return new RecommendationResult(List.of(), "대출상품 정보를 불러오지 못했습니다.");
        }
    }

    private BigDecimal recommendationRate(LoanRateOption option) {
        if (option.rateAvg() != null) return option.rateAvg();
        if (option.rateMin() != null) return option.rateMin();
        return option.rateMax();
    }

    private Integer targetMonths(BigDecimal needed, BigDecimal monthlySavings) {
        if (needed.signum() == 0) return 0;
        if (zero(monthlySavings).signum() == 0) return null;
        return needed.divide(zero(monthlySavings), 0, RoundingMode.CEILING).intValueExact();
    }

    private BigDecimal min(BigDecimal left, BigDecimal right) {
        return left.min(right);
    }

    private BigDecimal positive(BigDecimal value) {
        return value.max(BigDecimal.ZERO);
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private record RecommendationResult(List<RecommendedProduct> products, String warning) {
    }
}
