package com.yeoyeo.application.reservation.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class MonthlyStatisticDto {

	private final int year;
	private final int month;
	private final long roomId;
	private final int dayCount;
	private int totalReservedCount;
	private final List<MonthlyStatisticOriginDto> monthlyStatisticOriginDtoList = new ArrayList<>();

	public MonthlyStatisticDto(int year, int month, long roomId) {
		this.year = year;
		this.month = month;
		this.roomId = roomId;
		this.dayCount = getDayCount(year, month);
	}

	public void addOrigin(MonthlyStatisticOriginDto monthlyStatisticOriginDto) {
		this.monthlyStatisticOriginDtoList.add(monthlyStatisticOriginDto);
		this.totalReservedCount += monthlyStatisticOriginDto.getReservedCount();
	}

	private int getDayCount(int year, int month) {
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
