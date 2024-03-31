package com.yeoyeo.application.reservation.dto;

import lombok.Getter;

@Getter
public class MonthlyStatisticOriginDto {

	final String reservedFrom;
	int reservationCount = 0;

	public MonthlyStatisticOriginDto(String reservedFrom) {
		this.reservedFrom = reservedFrom;
	}

	public void addReservationCount() {
		this.reservationCount++;
	}

}
