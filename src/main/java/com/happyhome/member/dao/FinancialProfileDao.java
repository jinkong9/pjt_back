package com.happyhome.member.dao;

import com.happyhome.member.dto.FinancialProfile;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FinancialProfileDao {

    private final FinancialProfileMapper mapper;

    public FinancialProfileDao(FinancialProfileMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<FinancialProfile> findByUserId(String userId) {
        return Optional.ofNullable(mapper.findByUserId(userId));
    }

    public void upsert(FinancialProfile profile) {
        mapper.upsert(profile);
    }
}
