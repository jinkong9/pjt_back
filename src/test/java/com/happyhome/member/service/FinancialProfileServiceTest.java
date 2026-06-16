package com.happyhome.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.happyhome.member.dao.FinancialProfileDao;
import com.happyhome.member.dto.FinancialProfile;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FinancialProfileServiceTest {

    @Test
    void returnsEmptyWhenMemberHasNoProfile() {
        FinancialProfileService service = new FinancialProfileService(new FakeFinancialProfileDao());

        assertThat(service.findByUserId("ssafy")).isEmpty();
    }

    @Test
    void savesAndUpdatesProfileForMember() {
        FakeFinancialProfileDao dao = new FakeFinancialProfileDao();
        FinancialProfileService service = new FinancialProfileService(dao);

        FinancialProfile saved = service.save("ssafy", profile("100000000"));
        FinancialProfile updated = service.save("ssafy", profile("120000000"));

        assertThat(saved.userId()).isEqualTo("ssafy");
        assertThat(updated.availableAssets()).isEqualByComparingTo("120000000");
        assertThat(dao.saved).hasSize(1);
    }

    @Test
    void rejectsNegativeMoneyValues() {
        FinancialProfileService service = new FinancialProfileService(new FakeFinancialProfileDao());
        FinancialProfile invalid = new FinancialProfile(
                null,
                BigDecimal.valueOf(-1),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        assertThatThrownBy(() -> service.save("ssafy", invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("0 이상");
    }

    private FinancialProfile profile(String assets) {
        return new FinancialProfile(
                null,
                new BigDecimal(assets),
                new BigDecimal("80000000"),
                new BigDecimal("4000000"),
                new BigDecimal("20000000"),
                new BigDecimal("500000")
        );
    }

    private static class FakeFinancialProfileDao extends FinancialProfileDao {

        private final Map<String, FinancialProfile> saved = new HashMap<>();

        FakeFinancialProfileDao() {
            super(null);
        }

        @Override
        public Optional<FinancialProfile> findByUserId(String userId) {
            return Optional.ofNullable(saved.get(userId));
        }

        @Override
        public void upsert(FinancialProfile profile) {
            saved.put(profile.userId(), profile);
        }
    }
}
