package com.yeoyeo.application.reservation.dto;

import lombok.Getter;

@Getter
public class MonthlyStatisticOriginDto {

	private final String reservedFrom;
	private int reservationCount = 0;

	public MonthlyStatisticOriginDto(String reservedFrom) {
		this.reservedFrom = reservedFrom;
	}

	public void addReservationCount() {
		this.reservationCount++;
	}

}
