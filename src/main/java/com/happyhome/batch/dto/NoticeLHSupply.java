package com.happyhome.batch.dto;

import com.happyhome.rental.dto.RentalSupply;

public record NoticeLHSupply(
        String noticeId,
        String usage,
        String address,
        String lotNumber,
        String area,
        String expectedAmount,
        String houseType,
        String householdCount,
        String internetApplyStatus,
        String mapAddress,
        String mapUrl,
        Double latitude,
        Double longitude
		) {

	public static NoticeLHSupply from(String noticeId, RentalSupply supply) {
		return new NoticeLHSupply(
				noticeId,
                supply.usage(),
                supply.address(),
                supply.lotNumber(),
                supply.area(),
                supply.expectedAmountRaw(),
                supply.houseType(),
                supply.householdCount(),
                supply.internetApplyStatus(),
                supply.mapAddress(),
                supply.mapUrl(),
                supply.latitude(),
                supply.longitude()
				);
	}
}
