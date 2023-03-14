package com.yeoyeo.application.dateroom.dto;

import com.yeoyeo.domain.DateRoom;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateRoomIdPriceInfoDto {

    private final LocalDate date;
    private final String dateRoomId;
    private final int price;

    public DateRoomIdPriceInfoDto (DateRoom entity) {
        this.date = entity.getDate();
        this.dateRoomId = entity.getId();
        this.price = entity.getPrice();
    }

}
