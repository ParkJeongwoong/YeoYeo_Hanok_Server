package com.yeoyeo.application.dateroom.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class DateRoomPriceInfoDto {

    private final int totalPrice;
    private final int originalPrice;
    private final int period;
    private final int discountedPrice;
    private final List<DateRoomIdPriceInfoDto> infoDtoList;

    public DateRoomPriceInfoDto(int totalPrice, int originalPrice, int discountedPrice, int period, List<DateRoomIdPriceInfoDto> infoDtoList) {
        this.totalPrice = totalPrice;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.period = period;
        this.infoDtoList = infoDtoList;
    }

}
