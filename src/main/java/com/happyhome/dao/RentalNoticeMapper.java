package com.happyhome.dao;

import com.happyhome.dto.RentalNotice;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RentalNoticeMapper {

    void upsert(RentalNotice notice);

    List<RentalNotice> findRecent();

    Optional<RentalNotice> findById(String noticeId);
}

