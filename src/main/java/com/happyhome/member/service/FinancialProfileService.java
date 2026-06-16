package com.happyhome.member.service;

import com.happyhome.member.dao.FinancialProfileDao;
import com.happyhome.member.dto.FinancialProfile;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FinancialProfileService {

    private final FinancialProfileDao financialProfileDao;

    public FinancialProfileService(FinancialProfileDao financialProfileDao) {
        this.financialProfileDao = financialProfileDao;
    }

    public Optional<FinancialProfile> findByUserId(String userId) {
        return financialProfileDao.findByUserId(userId);
    }

    public FinancialProfile save(String userId, FinancialProfile request) {
        FinancialProfile profile = new FinancialProfile(
                userId,
                nonNegative(request.availableAssets()),
                nonNegative(request.annualIncome()),
                nonNegative(request.monthlySavings()),
                nonNegative(request.existingLoanBalance()),
                nonNegative(request.existingMonthlyDebtPayment())
        );
        financialProfileDao.upsert(profile);
        return profile;
    }

    private BigDecimal nonNegative(BigDecimal value) {
        BigDecimal normalized = value == null ? BigDecimal.ZERO : value;
        if (normalized.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("금융정보는 0 이상이어야 합니다.");
        }
        return normalized;
    }
}
