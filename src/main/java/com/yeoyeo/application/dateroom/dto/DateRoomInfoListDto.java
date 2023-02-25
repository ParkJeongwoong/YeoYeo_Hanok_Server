package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DateRoomInfoListDto {
    private final LocalDate date;
    private final List<DateRoomInfoDto> rooms;

    public DateRoomInfoListDto(LocalDate date, DateRoomInfoDto dateRoomInfoDto) {
        this.date = date;
        this.rooms = new ArrayList<>();
        this.rooms.add(dateRoomInfoDto);
    }

    public void addDateRoomInfo(DateRoomInfoDto dateRoomInfoDto) {
        this.rooms.add(dateRoomInfoDto);
    }
}
