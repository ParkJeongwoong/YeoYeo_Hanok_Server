package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class DateRoom2MonthDto {

    private final List<DateRoomInfoByDateDto> month;
    private final List<DateRoomInfoByDateDto> nextMonth;

    public DateRoom2MonthDto(List<DateRoomInfoByDateDto> month, List<DateRoomInfoByDateDto> nextMonth) {
        this.month = month;
        this.nextMonth = nextMonth;
    }

}
