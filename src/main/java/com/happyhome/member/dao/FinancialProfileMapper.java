package com.happyhome.member.dao;

import com.happyhome.member.dto.FinancialProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FinancialProfileMapper {

    FinancialProfile findByUserId(String userId);

    void upsert(FinancialProfile profile);
}
