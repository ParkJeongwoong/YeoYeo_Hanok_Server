package com.yeoyeo.application.reservation.dto;

import java.util.ArrayList;
import java.util.List;

public class MonthlyStatisticDto {

	final int year;
	final int month;
	final long roomId;
	final int dayCount;
	int totalReservationCount;
	List<MonthlyStatisticOriginDto> monthlyStatisticOriginDtoList = new ArrayList<>();

	public MonthlyStatisticDto(int year, int month, long roomId) {
		this.year = year;
		this.month = month;
		this.roomId = roomId;
		this.dayCount = getDayCount(year, month);
	}

	public void addMonthlyStatisticOriginDto(MonthlyStatisticOriginDto monthlyStatisticOriginDto) {
		this.monthlyStatisticOriginDtoList.add(monthlyStatisticOriginDto);
		this.totalReservationCount += monthlyStatisticOriginDto.getReservationCount();
	}

	public int getDayCount(int year, int month) {
		int dayCount = 0;
		switch (month) {
		case 1: case 3: case 5: case 7: case 8: case 10: case 12:
			dayCount = 31;
			break;
		case 4: case 6: case 9: case 11:
			dayCount = 30;
			break;
		case 2:
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
				dayCount = 29;
			} else {
				dayCount = 28;
			}
			break;
		}
		return dayCount;
	}

}
