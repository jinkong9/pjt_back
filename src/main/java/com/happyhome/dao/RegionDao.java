package com.happyhome.dao;

import com.happyhome.dto.RegionOptionDto;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RegionDao {

    private final RegionMapper mapper;

    public RegionDao(RegionMapper mapper) {
        this.mapper = mapper;
    }

    public List<RegionOptionDto> findSidos() {
        return mapper.findSidos();
    }

    public List<RegionOptionDto> findGuguns(String sidoName) {
        return mapper.findGuguns(sidoName);
    }

    public List<RegionOptionDto> findDongs(String sidoName, String gugunName) {
        return mapper.findDongs(sidoName, gugunName);
    }
}
