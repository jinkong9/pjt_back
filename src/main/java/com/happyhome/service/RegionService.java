package com.happyhome.service;

import com.happyhome.dao.RegionDao;
import com.happyhome.dto.RegionOptionDto;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RegionService {

    private final RegionDao regionDao;

    public RegionService(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    public List<RegionOptionDto> findSidos() {
        return regionDao.findSidos();
    }

    public List<RegionOptionDto> findGuguns(String sidoName) {
        if (!StringUtils.hasText(sidoName)) {
            return Collections.emptyList();
        }
        return regionDao.findGuguns(sidoName);
    }

    public List<RegionOptionDto> findDongs(String sidoName, String gugunName) {
        if (!StringUtils.hasText(sidoName) || !StringUtils.hasText(gugunName)) {
            return Collections.emptyList();
        }
        return regionDao.findDongs(sidoName, gugunName);
    }
}
