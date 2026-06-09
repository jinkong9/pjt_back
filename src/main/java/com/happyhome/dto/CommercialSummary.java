package com.happyhome.dto;

public record CommercialSummary(
        int totalCount,
        int foodCount,
        int cafeCount,
        int medicalCount,
        int convenienceCount,
        int lifeCount
) {
}

