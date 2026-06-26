package com.happyhome.rental.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.member.dto.FinancialProfile;
import com.happyhome.member.service.FinancialProfileService;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import com.happyhome.rental.recommendation.dto.RentalRecommendation;
import com.happyhome.rental.service.RentalService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RentalRecommendationServiceTest {

    private final RentalService rentalService = Mockito.mock(RentalService.class);
    private final FinancialProfileService financialProfileService = Mockito.mock(FinancialProfileService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final RentalRecommendationService service = new RentalRecommendationService(
            rentalService,
            financialProfileService,
            clock
    );

    @Test
    void recommendsAffordableActiveNoticesBeforeUnaffordableClosedNotices() {
        when(financialProfileService.findByUserId("ssafy")).thenReturn(Optional.of(profile("100000000")));
        RentalNotice affordable = notice("LH-001", "서울 행복주택", "공고중");
        RentalNotice expensive = notice("LH-002", "부산 국민임대", "마감");
        when(rentalService.notices(any(RentalSearchCondition.class))).thenReturn(List.of(expensive, affordable));
        when(rentalService.cachedDetail("LH-001")).thenReturn(detail(affordable, "2026.06.20", "2026.06.24", "보증금 5,000만원"));
        when(rentalService.cachedDetail("LH-002")).thenReturn(detail(expensive, "2026.06.01", "2026.06.10", "보증금 3억원"));

        List<RentalRecommendation> recommendations = service.recommend("ssafy", 10);

        assertThat(recommendations).extracting(item -> item.notice().noticeId())
                .containsExactly("LH-001", "LH-002");
        assertThat(recommendations.get(0).score()).isGreaterThan(recommendations.get(1).score());
        assertThat(recommendations.get(0).reasons()).contains("자산 범위 안의 예상 보증금입니다.");
    }

    @Test
    void prioritizesPreferredRegionsAndRentalTypesFromMyData() {
        when(financialProfileService.findByUserId("ssafy")).thenReturn(Optional.of(profile("100000000")));
        RentalNotice preferred = notice("LH-SEOUL", "서울 행복주택 입주자 모집", "공고중", "서울특별시", "행복주택");
        RentalNotice other = notice("LH-BUSAN", "부산 국민임대 입주자 모집", "공고중", "부산광역시", "국민임대");
        when(rentalService.notices(any(RentalSearchCondition.class))).thenReturn(List.of(other, preferred));
        when(rentalService.cachedDetail("LH-SEOUL")).thenReturn(detail(preferred, "2026.06.20", "2026.06.24", "보증금 5,000만원"));
        when(rentalService.cachedDetail("LH-BUSAN")).thenReturn(detail(other, "2026.06.20", "2026.06.24", "보증금 5,000만원"));

        List<RentalRecommendation> recommendations = service.recommend(
                "ssafy",
                10,
                new RentalRecommendationService.RecommendationCriteria(List.of("서울"), List.of("행복주택"))
        );

        assertThat(recommendations).extracting(item -> item.notice().noticeId())
                .containsExactly("LH-SEOUL", "LH-BUSAN");
        assertThat(recommendations.get(0).reasons())
                .contains("희망 지역과 일치하는 공고입니다.", "관심 임대 유형과 일치합니다.");
    }

    @Test
    void recommendsFromCachedRentalDataWithoutLiveLhDetailFetches() {
        when(financialProfileService.findByUserId("ssafy")).thenReturn(Optional.of(profile("100000000")));
        RentalNotice cached = notice("LH-CACHED", "Seoul public rental", "open");
        when(rentalService.cachedNotices(any(RentalSearchCondition.class))).thenReturn(List.of(cached));
        when(rentalService.cachedDetail("LH-CACHED"))
                .thenReturn(new RentalNoticeDetail(
                        cached,
                        new RentalDetail("", "", "2026.06.20", "2026.06.24", ""),
                        List.of()
                ));

        List<RentalRecommendation> recommendations = service.recommend("ssafy", 10);

        assertThat(recommendations).extracting(item -> item.notice().noticeId())
                .containsExactly("LH-CACHED");
        verify(rentalService, never()).notices(any(RentalSearchCondition.class));
        verify(rentalService, never()).detail("LH-CACHED");
    }

    private FinancialProfile profile(String assets) {
        return new FinancialProfile(
                "ssafy",
                new BigDecimal(assets),
                new BigDecimal("60000000"),
                new BigDecimal("1500000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private RentalNotice notice(String noticeId, String title, String status) {
        return new RentalNotice(noticeId, title, "서울", "임대", "행복주택", status,
                "2026.06.01", "2026.06.30", "https://apply.lh.or.kr",
                "01", "01", "10", "010", "api");
    }

    private RentalNotice notice(String noticeId, String title, String status, String regionName, String detailType) {
        return new RentalNotice(noticeId, title, regionName, "임대", detailType, status,
                "2026.06.01", "2026.06.30", "https://apply.lh.or.kr",
                "01", "01", "10", "010", "api");
    }

    private RentalNoticeDetail detail(RentalNotice notice, String startDate, String endDate, String amount) {
        return new RentalNoticeDetail(
                notice,
                new RentalDetail("서울", "강남구", startDate, endDate, "1600-1004"),
                List.of(new RentalSupply("주택", "서울 강남구", "", "59", amount, amount, "59A", "10", "가능", "서울 강남구", ""))
        );
    }
}
