package com.happyhome.dao;

import com.happyhome.dto.AnalysisSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnalysisSnapshotMapper {

    void insert(AnalysisSnapshot snapshot);
}

