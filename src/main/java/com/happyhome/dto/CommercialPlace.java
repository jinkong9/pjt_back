package com.happyhome.dto;

public record CommercialPlace(
        String name,
        String largeCategory,
        String middleCategory,
        String address,
        double longitude,
        double latitude
) {
}

