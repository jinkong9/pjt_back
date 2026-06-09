package com.happyhome.service;

import com.happyhome.openapi.LhOpenApiClient;
import com.happyhome.openapi.SampleData;
import com.happyhome.dao.RentalNoticeMapper;
import com.happyhome.dto.RentalNotice;
import com.happyhome.dto.RentalNoticeDetail;
import com.happyhome.dto.RentalSearchCondition;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RentalService {

    private final LhOpenApiClient lhClient;
    private final RentalNoticeMapper mapper;

    public RentalService(LhOpenApiClient lhClient, RentalNoticeMapper mapper) {
        this.lhClient = lhClient;
        this.mapper = mapper;
    }

    public List<RentalNotice> notices(RentalSearchCondition condition) {
        List<RentalNotice> notices = lhClient.notices(condition);
        notices.forEach(this::cacheQuietly);
        return notices;
    }

    public RentalNoticeDetail detail(String noticeId) {
        RentalNotice notice = mapper.findById(noticeId)
                .orElseGet(() -> SampleData.rentalNotices().stream()
                        .filter(item -> item.noticeId().equals(noticeId))
                        .findFirst()
                        .orElse(SampleData.rentalNotices().get(0)));
        return new RentalNoticeDetail(notice, lhClient.detail(notice), lhClient.supplies(notice));
    }

    private void cacheQuietly(RentalNotice notice) {
        try {
            mapper.upsert(notice);
        } catch (Exception ignored) {
            // API results should still be usable when local cache initialization is unavailable.
        }
    }
}

